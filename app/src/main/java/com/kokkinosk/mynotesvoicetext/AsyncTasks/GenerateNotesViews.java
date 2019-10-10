package com.kokkinosk.mynotesvoicetext.AsyncTasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.request.StringRequest;
import com.kokkinosk.mynotesvoicetext.Note;
import com.kokkinosk.mynotesvoicetext.NoteManager;
import com.kokkinosk.mynotesvoicetext.R;
import com.kokkinosk.mynotesvoicetext.Recording;
import com.kokkinosk.mynotesvoicetext.User;
import com.kokkinosk.mynotesvoicetext.VolleyController;
import com.kokkinosk.mynotesvoicetext.Website;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GenerateNotesViews extends AsyncTask<Void, View, String> {
    ArrayList<Note> notesArrayList = new ArrayList<>();
    NoteManager nman;
    private WeakReference<Activity> activityReference;

    public GenerateNotesViews(Activity context) {
        activityReference = new WeakReference<>(context);
        nman = new NoteManager(activityReference.get());
    }

    @Override
    protected String doInBackground(Void... voids) {

        nman.check();
        LayoutInflater inflater = activityReference.get().getLayoutInflater();
        for (int i = 0; i < NoteManager.notes.size(); i++) {
            NoteManager.notes.get(i).setMyView(inflater.inflate(R.layout.note_item, (LinearLayout) activityReference.get().findViewById(R.id.linlay), false));
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Note currentNote;
        for (int i = 0; i < NoteManager.notes.size(); i++) {
            currentNote = NoteManager.notes.get(i);
            ((TextView) currentNote.getMyView().findViewById(R.id.noteTitle)).setText(currentNote.getTitle());
            ((TextView) currentNote.getMyView().findViewById(R.id.noteBody)).setText(currentNote.getBody());
            ((LinearLayout) activityReference.get().findViewById(R.id.linlay)).addView(currentNote.getMyView());
            final int finalI = i;
            final Note finalCurrentNote = currentNote;
//            generateUploadButton(activityReference.get(),currentNote);

            if (!User.getLoginStatus()) currentNote.getMyView().findViewById(R.id.upload_button).setVisibility(View.GONE);
            currentNote.getMyView().findViewById(R.id.delete_img).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((LinearLayout) activityReference.get().findViewById(R.id.linlay)).removeView(finalCurrentNote.getMyView());
                    NoteManager.notes.remove(finalI);
                    NoteManager.updateNotes();
                }
            });
        }
    }
//    void generateUploadButton(final Activity activity, final Note note ) {
//        new VolleyController(activity);
//        if (!User.getLoginStatus()) {
//            note.getMyView().findViewById(R.id.upload_button).setVisibility(View.GONE);
//        } else {
//            note.getMyView().findViewById(R.id.upload_button).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(final View view) {
//                    if (User.getLoginStatus()) {
//
//                        final String upload_url = Website.getUrl() + "php/upload.php";
//
////                        activityReference.get().findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
//
//                        SimpleMultiPartRequest uploadRequest = new SimpleMultiPartRequest(Request.Method.POST, upload_url, new Response.Listener<String>() {
//                            @Override
//                            public void onResponse(String response) {
////                                Toast.makeText(activityReference.get(), response, Toast.LENGTH_LONG).show();
//                                if (response.equals("OK")) {
//                                    AppCompatImageView iv = rec.getMyView().findViewById(R.id.upload_button);
//                                    iv.setImageResource(R.drawable.baseline_cloud_done_24);
//                                    iv.invalidate();
//                                    rec.getMyView().invalidate();
//                                    rec.getMyView().findViewById(R.id.upload_button).invalidate();
//                                    rec.getMyView().findViewById(R.id.upload_button).setOnClickListener(new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View view) {
//
//                                        }
//                                    });
//
//                                }
//                            }
//                        }, new Response.ErrorListener() {
//                            @Override
//                            public void onErrorResponse(VolleyError error) {
////                                Toast.makeText(activityReference.get(), "", Toast.LENGTH_LONG).show();
//                            }
//                        });
//                        uploadRequest.addFile(
//                                "uploadedNote"
//                                , new File(note.getUri().getPath()).getAbsolutePath()
//                        );
//                        uploadRequest.addStringParam("username", User.getUserName());
//                        uploadRequest.addStringParam("password", User.getUserPass());
//                        uploadRequest.setRetryPolicy(new DefaultRetryPolicy(30000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//
//
//
//                        VolleyController.getInstance().addToRequestQueue(uploadRequest, "UPLOAD");
//
//                    }
//                }
//
//            });
//
//            final String check_upload_url = Website.getUrl() + "php/check_upload.php";
//            StringRequest checkUploadRequest = new StringRequest(Request.Method.POST, check_upload_url,
//                    new Response.Listener<String>() {
//                        @Override
//                        public void onResponse(String response) {
//                            if (response.equals("OK")){
//                                AppCompatImageView iv = rec.getMyView().findViewById(R.id.upload_button);
//                                iv.setImageResource(R.drawable.baseline_cloud_done_24);
//                                rec.getMyView().findViewById(R.id.upload_button).setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//
//                                    }
//                                });
//
//                            }
//                            else {
//                                AppCompatImageView iv = rec.getMyView().findViewById(R.id.upload_button);
//                                iv.setImageResource(R.drawable.baseline_cloud_upload_24);
//                            }
//
//                        }
//                    }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//
//                    error.printStackTrace();
//                }
//            }) {
//                @Override
//                protected Map<String, String> getParams() {
//                    Map<String, String> params = new HashMap<String, String>();
//                    params.put("username",User.getUserName());
//                    params.put("password", User.getUserPass());
//                    params.put("filename",rec.getTitle());
//                    return params;
//                }
//            };
//
//            VolleyController.getInstance().addToRequestQueue(checkUploadRequest, "CHECKUPLOAD");
//        }
//    }
}
