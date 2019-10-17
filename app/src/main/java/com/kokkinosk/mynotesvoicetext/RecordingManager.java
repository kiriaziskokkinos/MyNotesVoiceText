package com.kokkinosk.mynotesvoicetext;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.format.Formatter;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileFilter;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class RecordingManager {

    public String recordingsDirPath;
    static File[] recordingArray;
    private WeakReference activityReference;
    private WeakReference activityContext;
    public static ArrayList<Recording> recordingArrayList = new ArrayList<>();
    public static ArrayList<Recording> cloud_recordingArrayList = new ArrayList<>();
    public static ArrayList<CloudRecording> cloudRecordings = new ArrayList<>();
//    public boolean active = false;

    public RecordingManager(Activity activity) {

        if (activity!= null){
            activityReference = new WeakReference<>(activity);
            activityContext = new WeakReference<>(activity.getApplicationContext());
            recordingsDirPath = ((Activity)activityReference.get()).getFilesDir().getAbsolutePath()+"/Recordings";
            File directory = new File(recordingsDirPath);
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    Log.e("ERROR","couldn't create directory " + recordingsDirPath);
                }
            }

            if (User.isLoggedIn()) recordingsDirPath = ((Activity)activityReference.get()).getFilesDir().getAbsolutePath()+"/Recordings/"+md5(User.getUserName());
            else recordingsDirPath = ((Activity)activityReference.get()).getFilesDir().getAbsolutePath()+"/Recordings";
            directory = new File(recordingsDirPath);
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    Log.e("ERROR","couldn't create directory " + recordingsDirPath);
                }
            }
            scanNewRecordings();

        }
    }


    void scanNewRecordings(){
        recordingArrayList.clear();
        File dir = new File(recordingsDirPath);
        recordingArray = dir.listFiles(getFileFilter());
        if (recordingArray == null) recordingArray = new File[0];
        for (File file : recordingArray) {
            recordingArrayList.add(
                    new Recording(
                            file.getName()
                            , 0L
                            , Uri.fromFile(file)
                            , Formatter.formatShortFileSize((Context)activityContext.get(), file.length())
                    )
            );
        }

    }


    public File[] getRecordingArray() {
        scanNewRecordings();
        return recordingArray;
    }

    ArrayList<Recording> getRecordingArrayList() {
        scanNewRecordings();
        return recordingArrayList;
    }

    void setRecordingArrayList(ArrayList<Recording> updated){
        recordingArrayList = updated;
    }

    private FileFilter getFileFilter(){
        return new FileFilter() {
            @Override
            public boolean accept(File file) {
                return ( file.getAbsolutePath().matches(".*\\.m4a") );
            }
        };
    }


    String getRecordingsDirPath(){
        return recordingsDirPath;
    }


    FileFilter getRecordingFileFilter(){
        return new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (file.getAbsolutePath().matches(".*\\.m4a"));
            }
        };
    }

    String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));

            return hexString.toString();
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    void fetchCloud(){
        Activity activity = (Activity) activityReference.get();
        new VolleyController(activity);
        final String check_upload_url = Website.getUrl() + "php/fetch_uploads.php";
        StringRequest fetchCloudRecordings = new StringRequest(Request.Method.POST, check_upload_url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        JSONArray obj = null;
                        try {
                            obj = new JSONArray(response);
                            for (int i=0;i<obj.length();i++){
                                cloudRecordings.add(new CloudRecording(obj.getJSONObject(i).getString("title"),obj.getJSONObject(i).getString("filepath")));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        for (int j=0;j<recordingArrayList.size();j++){
                            for (int i=0;i<cloudRecordings.size();i++){
                                if (recordingArrayList.get(j).getTitle().equals(cloudRecordings.get(i).getTitle())){
                                    cloudRecordings.remove(i);
//                                    break;
                                }
                            }
                        }
                        for (int i=0;i<cloudRecordings.size();i++){
                            recordingArrayList.add(new Recording(cloudRecordings.get(i).getTitle(),cloudRecordings.get(i).getFilepath()));
                        }
                        cloudRecordings.clear();
//                        active = false;

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                active = false;
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username",User.getUserName());
                params.put("password", User.getUserPass());
                return params;
            }
        };
//        active = true;
        VolleyController.getInstance().addToRequestQueue(fetchCloudRecordings, "FETCHUPLOADS");

    }

}
