package com.kokkinosk.mynotesvoicetext.AsyncTasks;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.kokkinosk.mynotesvoicetext.CloudRecording;
import com.kokkinosk.mynotesvoicetext.Note;
import com.kokkinosk.mynotesvoicetext.NoteManager;
import com.kokkinosk.mynotesvoicetext.R;
import com.kokkinosk.mynotesvoicetext.Recording;
import com.kokkinosk.mynotesvoicetext.User;
import com.kokkinosk.mynotesvoicetext.VolleyController;
import com.kokkinosk.mynotesvoicetext.Website;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.kokkinosk.mynotesvoicetext.NoteManager.notes;
import static com.kokkinosk.mynotesvoicetext.NoteManager.updateNotes;
import static com.kokkinosk.mynotesvoicetext.RecordingManager.recordingArrayList;


public class GenerateNotesViews extends AsyncTask<Void, View, String> {
    ArrayList<Note> notesArrayList = new ArrayList<>();
    NoteManager nman;
    private WeakReference<Activity> activityReference;
    ArrayList<Note> cloudNotes = new ArrayList<>();
    final Object lock = new Object();
    public GenerateNotesViews(Activity context) {
        activityReference = new WeakReference<>(context);
        nman = new NoteManager(activityReference.get());
    }

    @Override
    protected String doInBackground(Void... voids) {

        nman.check();
        LayoutInflater inflater = activityReference.get().getLayoutInflater();
        if (User.isLoggedIn()){
            new VolleyController((Activity)activityReference.get());
            final String check_upload_url = Website.getUrl() + "php/fetch_notes.php";

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
                            Note n = new Note(obj.getJSONObject(i).getString("title"),obj.getJSONObject(i).getString("body"),null);
                            n.setOnCloud(true);
                            cloudNotes.add(n);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    for (int j=0;j<notes.size();j++){
                        for (int i=0;i<cloudNotes.size();i++){
                            if (notes.get(j).getTitle().equals(cloudNotes.get(i).getTitle()) && notes.get(j).getBody().equals(cloudNotes.get(i).getBody())) {
                                cloudNotes.remove(i);
//                                    break;
                            }
                        }
                    }
                    notes.addAll(cloudNotes);
                    cloudNotes.clear();
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
        for (int i = 0; i < notes.size(); i++) {
            notes.get(i).setMyView(inflater.inflate(R.layout.note_item, (LinearLayout) activityReference.get().findViewById(R.id.linlay), false));
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Note currentNote;
        for (int i = 0; i < notes.size(); i++) {
            currentNote = notes.get(i);
            ((TextView) currentNote.getMyView().findViewById(R.id.noteTitle)).setText(currentNote.getTitle());
            ((TextView) currentNote.getMyView().findViewById(R.id.noteBody)).setText(currentNote.getBody());
            ((LinearLayout) activityReference.get().findViewById(R.id.linlay)).addView(currentNote.getMyView());
            generateDeleteButton(activityReference.get(),currentNote);
            generateUploadButton(activityReference.get(),currentNote);

        }


    }


    void generateDeleteButton(final Activity activity, final Note note){
        if (!User.isLoggedIn()){
            note.getMyView().findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notes.remove(note);
                    NoteManager.updateNotes();
                    ((LinearLayout) activity.findViewById(R.id.linlay)).removeView(note.getMyView());
                }
            });
        }
        else {
            if(!note.isOnCloud()){
                note.getMyView().findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        notes.remove(note);
                        NoteManager.updateNotes();
                        ((LinearLayout) activity.findViewById(R.id.linlay)).removeView(note.getMyView());
                    }
                });
            }
            else{
                note.getMyView().findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final String url = Website.getUrl() + "php/delete_note.php";
                        StringRequest sr = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.d("NOTESCLOUD",response);
                                        if (response.equals("OK")){
                                            notes.remove(note);
                                            NoteManager.updateNotes();
                                            ((LinearLayout) activity.findViewById(R.id.linlay)).removeView(note.getMyView());
                                            Toast.makeText(activity.getApplicationContext(), "Your cloud note was deleted.", Toast.LENGTH_LONG).show();
                                        }
                                        else {
                                            Toast.makeText(activity.getApplicationContext(), "There was an error while trying to delete your note." , Toast.LENGTH_LONG).show();
                                        }

                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(activityReference.get().getApplicationContext(),"Couldn't delete cloud note.",Toast.LENGTH_LONG).show();
                                error.printStackTrace();
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("username",User.getUserName());
                                params.put("password", User.getUserPass());
                                params.put("title",note.getTitle());
                                params.put("body",note.getBody());
                                return params;
                            }
                        };

                        sr.setRetryPolicy(new DefaultRetryPolicy(
                                5000,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                        Volley.newRequestQueue(activityReference.get()).add(sr);
                    }
                });
            }

        }
    }
    void generateUploadButton(final Activity activity, final Note note ) {
        if (!User.isLoggedIn()){
            note.getMyView().findViewById(R.id.upload_button).setVisibility(View.GONE);
        }
        else {
            if (note.isOnCloud()) {
                AppCompatImageView iv = note.getMyView().findViewById(R.id.upload_button);
                iv.setImageResource(R.drawable.baseline_cloud_done_24);

                note.getMyView().findViewById(R.id.upload_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });


            }
            else {
                AppCompatImageView iv = note.getMyView().findViewById(R.id.upload_button);
                iv.setImageResource(R.drawable.baseline_cloud_upload_24);

                note.getMyView().findViewById(R.id.upload_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String url = Website.getUrl() + "php/upload_note.php";
                        StringRequest sr = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.d("NOTESCLOUD_UPLOAD",response);
                                        if (response.equals("OK")){
                                            AppCompatImageView iv = note.getMyView().findViewById(R.id.upload_button);
                                            iv.setImageResource(R.drawable.baseline_cloud_done_24);
                                            note.setOnCloud(true);
                                            updateNotes();
                                        }
                                        else {
                                            Toast.makeText(activity.getApplicationContext(), "There was an error while trying to upload your note." , Toast.LENGTH_LONG).show();
                                        }

                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(activityReference.get().getApplicationContext(),"Couldn't upload note.",Toast.LENGTH_LONG).show();
                                error.printStackTrace();
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("username",User.getUserName());
                                params.put("password", User.getUserPass());
                                params.put("title",note.getTitle());
                                params.put("body",note.getBody());
                                return params;
                            }
                        };

                        sr.setRetryPolicy(new DefaultRetryPolicy(
                                5000,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                        Volley.newRequestQueue(activityReference.get()).add(sr);
                    }
                });
            }
        }
    }

}
