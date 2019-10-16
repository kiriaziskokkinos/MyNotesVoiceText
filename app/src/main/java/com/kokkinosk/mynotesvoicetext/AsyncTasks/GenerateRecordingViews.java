package com.kokkinosk.mynotesvoicetext.AsyncTasks;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.kokkinosk.mynotesvoicetext.CloudRecording;
import com.kokkinosk.mynotesvoicetext.R;
import com.kokkinosk.mynotesvoicetext.Recording;
import com.kokkinosk.mynotesvoicetext.RecordingManager;
import com.kokkinosk.mynotesvoicetext.User;
import com.kokkinosk.mynotesvoicetext.VolleyController;
import com.kokkinosk.mynotesvoicetext.Website;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.kokkinosk.mynotesvoicetext.RecordingManager.cloudRecordings;
import static com.kokkinosk.mynotesvoicetext.RecordingManager.recordingArrayList;

public class GenerateRecordingViews extends AsyncTask<Void, View, String> {

    private WeakReference<Activity> activityReference;
    private MediaMetadataRetriever mmr = new MediaMetadataRetriever();
    private RecordingManager recman;
    final Object lock = new Object();
    // only retain a weak reference to the activity
    public GenerateRecordingViews(Activity context) {
        activityReference = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        activityReference.get().findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        ((ProgressBar) activityReference.get().findViewById(R.id.progressBar)).setIndeterminate(true);
        recman = new RecordingManager(activityReference.get());

    }

    @Override
    protected synchronized String doInBackground(Void... params) {


        if (User.getLoginStatus()){
            new VolleyController((Activity)activityReference.get());
            final String check_upload_url = Website.getUrl() + "php/fetch_uploads.php";

            Map<String, String> params2 = new HashMap<>();
            params2.put("username",User.getUserName());
            params2.put("password", User.getUserPass());

            StringRequest request = new StringRequest(Request.Method.POST, check_upload_url, new Response.Listener<String>() {
                @Override
                public synchronized void onResponse(String response) {
                    Log.d("RESPONSE",response);
                    try {
                        JSONArray obj = new JSONArray(response);
                        Log.d("RESPONSE",response);
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


                    synchronized (lock){
                        lock.notify();
                    }


                }
            }, null);

            request.setParams(params2);


            VolleyController.getInstance().getRequestQueue().add(request);





            synchronized (lock){
                try {
                    lock.wait(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

            LayoutInflater inflater = activityReference.get().getLayoutInflater();
            for (int i = 0; i < recordingArrayList.size(); i++) {
                recordingArrayList.get(i).setMyView(inflater.inflate(R.layout.recording_item, (LinearLayout) activityReference.get().findViewById(R.id.linlay), false));
            }
            return "task finished";


    }

    @Override
    protected void onProgressUpdate(View... progress) {

    }

    @Override
    protected void onPostExecute(String result) {
        View currentView;
        TextView rec_name;
        TextView rec_dur;
        TextView rec_size;
        int millSecond;
        int secs;
        int mins;
        final Activity activity = activityReference.get();
        if (activity == null || activity.isFinishing()) return;

        int i;
        for (i = 0; i < recordingArrayList.size(); i++) {
            final Recording rec = recordingArrayList.get(i);
            currentView = rec.getMyView();
            currentView.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorWhite));
            rec_name = currentView.findViewById(R.id.noteTitle);
            rec_dur = currentView.findViewById(R.id.recording_duration);
            rec_size = currentView.findViewById(R.id.recording_size);
            rec_name.setText(rec.getTitle());
            if (rec.isOnCloud()){
                AppCompatImageView iv = rec.getMyView().findViewById(R.id.upload_button);
                iv.setImageResource(R.drawable.baseline_cloud_download_24);
                rec_dur.setText(" ");
                rec_size.setText(" ");

            }
            else {
                mmr.setDataSource(activity.getApplicationContext(), rec.getUri());
                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                millSecond = Integer.parseInt(durationStr);
                secs = millSecond / 1000;
                mins = secs / 60;
                secs = secs % 60;
                rec_dur.setText("" + String.format("%02d", mins) + ":" + String.format("%02d", secs));
                rec_size.setText(rec.getFileSize());
                rec.getMyView().findViewById(R.id.cardview).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri uri = FileProvider.getUriForFile(view.getContext(), view.getContext().getApplicationContext().getPackageName() + ".provider", new File(rec.getUri().getPath()));
                        intent.setDataAndType(uri, activity.getContentResolver().getType(uri));
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        activity.startActivity(intent);
                    }
                });
            }




            rec.getMyView().findViewById(R.id.cardview).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {

                    return false;
                }
            });

            generateDeleteButton(activityReference.get(),rec);
            generateUploadButton(activityReference.get(),rec);


            ((LinearLayout) activity.findViewById(R.id.linlay)).addView(rec.getMyView(), i);

        }



        ((LinearLayout) activity.findViewById(R.id.linlay)).getChildCount();
        while (((LinearLayout) activity.findViewById(R.id.linlay)).getChildCount() > i) {
            int count = ((LinearLayout) activity.findViewById(R.id.linlay)).getChildCount();

            ((LinearLayout) activity.findViewById(R.id.linlay)).removeViewAt(count - 1);
        }
        ((ProgressBar) activityReference.get().findViewById(R.id.progressBar)).setIndeterminate(false);
        activityReference.get().findViewById(R.id.progressBar).setVisibility(View.GONE);
    }




    void generateDeleteButton(final Activity activity, final Recording rec){
        rec.getMyView().findViewById(R.id.delete_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Confirm Delete")
                        .setCancelable(true)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {


                                if (User.getLoginStatus()){
                                    final String url = Website.getUrl() + "php/delete_upload.php";
                                    StringRequest sr = new StringRequest(Request.Method.POST, url,
                                            new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    if (response.equals("OK")){
                                                        boolean delete = new File(rec.getUri().getPath()).delete();
                                                        String deleteResult;
                                                        if (!delete) {
                                                            deleteResult = "Could not delete the file.";
                                                        } else {

                                                            deleteResult = "Your recording was deleted";
                                                            ((LinearLayout) activity.findViewById(R.id.linlay)).removeView(rec.getMyView());
                                                            recordingArrayList.remove(rec);
                                                        }
                                                        Toast.makeText(activity.getApplicationContext(), deleteResult, Toast.LENGTH_LONG).show();
                                                    }
                                                    else {
                                                        Toast.makeText(activity.getApplicationContext(), response , Toast.LENGTH_LONG).show();
                                                    }

                                                }
                                            }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {

                                            error.printStackTrace();
                                        }
                                    }) {
                                        @Override
                                        protected Map<String, String> getParams() {
                                            Map<String, String> params = new HashMap<String, String>();
                                            params.put("username",User.getUserName());
                                            params.put("password", User.getUserPass());
                                            params.put("filename",rec.getTitle());
                                            return params;
                                        }
                                    };

                                    Volley.newRequestQueue(activityReference.get()).add(sr);
                                }
                                else {
                                    boolean delete = new File(rec.getUri().getPath()).delete();
                                    String deleteResult;
                                    if (!delete) {
                                        deleteResult = "Could not delete the file.";
                                    } else {

                                        deleteResult = "Your recording was deleted";
                                        ((LinearLayout) activity.findViewById(R.id.linlay)).removeView(rec.getMyView());
                                        recordingArrayList.remove(rec);
                                    }
                                    Toast.makeText(activity.getApplicationContext(), deleteResult, Toast.LENGTH_LONG).show();
                                }





                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }).show();
            }
        });
    }
    void generateUploadButton(final Activity activity, final Recording rec ) {
        new VolleyController(activity);
        if (!User.getLoginStatus()) {
            rec.getMyView().findViewById(R.id.upload_button).setVisibility(View.GONE);
        } else {

            if (rec.isOnCloud()) {
//                AppCompatImageView iv = rec.getMyView().findViewById(R.id.upload_button);
//                iv.setImageResource(R.drawable.baseline_cloud_download_24);
//                rec.getMyView().findViewById(R.id.upload_button).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Log.d("DOWNLOAD", rec.getUri().toString());
//                        SimpleMultiPartRequest downloadRequest = new SimpleMultiPartRequest(Request.Method.GET, ( (String) Website.getUrl() + rec.getUri().toString()), new Response.Listener<byte[]>() {
//                            @Override
//                            public void onResponse(byte[] response) {
//
//                            }
//
//
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
////                                Toast.makeText(activityReference.get(), "", Toast.LENGTH_LONG).show();
//                    }
//                });
                rec.getMyView().findViewById(R.id.upload_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        class TestAsync extends AsyncTask<Void, Integer, String> {

                            protected void onPreExecute() {
                                super.onPreExecute();
                            }

                            protected String doInBackground(Void...arg0) {

                                try  (BufferedInputStream in = new BufferedInputStream(new URL(Website.getUrl()+"/"+rec.getUri().toString()).openStream())) {
                                    FileOutputStream fileOutputStream =  new FileOutputStream(recman.recordingsDirPath+"/"+rec.getTitle());
                                    byte[] dataBuffer = new byte[1024];
                                    int bytesRead;
                                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                                    }
                                } catch (IOException e) {
                                    // handle exception
                                }
                                return "You are at PostExecute";
                            }


                            protected void onPostExecute(String result) {
                                super.onPostExecute(result);
                                ((LinearLayout)activity.findViewById(R.id.linlay)).removeAllViews();
                                new GenerateRecordingViews(activity).execute();
                                new GenerateNotesViews(activity).execute();


                            }
                        }
                        new TestAsync().execute();

                    }
                });

            }
            else{
                rec.getMyView().findViewById(R.id.upload_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        if (User.getLoginStatus()) {

                            final String upload_url = Website.getUrl() + "php/upload.php";
                            SimpleMultiPartRequest uploadRequest = new SimpleMultiPartRequest(Request.Method.POST, upload_url, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    if (response.equals("OK")) {
                                        AppCompatImageView iv = rec.getMyView().findViewById(R.id.upload_button);
                                        iv.setImageResource(R.drawable.baseline_cloud_done_24);
                                        iv.invalidate();
                                        rec.getMyView().invalidate();
                                        rec.getMyView().findViewById(R.id.upload_button).invalidate();
                                        rec.getMyView().findViewById(R.id.upload_button).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                            }
                                        });

                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                }
                            });
                            uploadRequest.addFile(
                                    "uploadedFile"
                                    , new File(rec.getUri().getPath()).getAbsolutePath()
                            );
                            uploadRequest.addStringParam("username", User.getUserName());
                            uploadRequest.addStringParam("password", User.getUserPass());
                            uploadRequest.setRetryPolicy(new DefaultRetryPolicy(30000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));



                            VolleyController.getInstance().addToRequestQueue(uploadRequest, "UPLOAD");

                        }
                    }

                });

                final String check_upload_url = Website.getUrl() + "php/check_upload.php";
                StringRequest checkUploadRequest = new StringRequest(Request.Method.POST, check_upload_url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (response.equals("OK")){
                                    AppCompatImageView iv = rec.getMyView().findViewById(R.id.upload_button);
                                    iv.setImageResource(R.drawable.baseline_cloud_done_24);
                                    rec.getMyView().findViewById(R.id.upload_button).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                        }
                                    });

                                }
                                else {
                                    AppCompatImageView iv = rec.getMyView().findViewById(R.id.upload_button);
                                    iv.setImageResource(R.drawable.baseline_cloud_upload_24);
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        error.printStackTrace();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("username",User.getUserName());
                        params.put("password", User.getUserPass());
                        params.put("filename",rec.getTitle());
                        return params;
                    }
                };
                VolleyController.getInstance().addToRequestQueue(checkUploadRequest, "CHECKUPLOAD");
            }


        }

    }

}


