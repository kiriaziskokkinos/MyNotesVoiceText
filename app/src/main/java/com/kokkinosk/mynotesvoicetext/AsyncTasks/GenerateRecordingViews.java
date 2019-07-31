package com.kokkinosk.mynotesvoicetext.AsyncTasks;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.kokkinosk.mynotesvoicetext.R;
import com.kokkinosk.mynotesvoicetext.Recording;
import com.kokkinosk.mynotesvoicetext.RecordingManager;

import java.io.File;
import java.lang.ref.WeakReference;

public class GenerateRecordingViews extends AsyncTask<Void, View, String> {

    private WeakReference<Activity> activityReference;
    private MediaMetadataRetriever mmr = new MediaMetadataRetriever();
    private RecordingManager recman;

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
    protected String doInBackground(Void... params) {

        // do some long running task...
        LayoutInflater inflater = activityReference.get().getLayoutInflater();
        for (int i = 0; i < RecordingManager.recordingArrayList.size(); i++) {
            RecordingManager.recordingArrayList.get(i).setMyView(inflater.inflate(R.layout.recording_item, (LinearLayout) activityReference.get().findViewById(R.id.linlay), false));
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
        for (i = 0; i < RecordingManager.recordingArrayList.size(); i++) {
            final Recording rec = RecordingManager.recordingArrayList.get(i);
            currentView = rec.getMyView();
            currentView.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorWhite));
            rec_name = currentView.findViewById(R.id.noteTitle);
            rec_dur = currentView.findViewById(R.id.recording_duration);
            rec_size = currentView.findViewById(R.id.recording_size);
            rec_name.setText(rec.getTitle());
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

            rec.getMyView().findViewById(R.id.cardview).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {

                    return false;
                }
            });

            rec.getMyView().findViewById(R.id.delete_img).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(view.getContext())
                            .setTitle("Confirm Delete")
                            .setCancelable(true)
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    boolean delete = new File(rec.getUri().getPath()).delete();
                                    String deleteResult;
                                    if (!delete) {
                                        deleteResult = "Could not delete the file.";
                                    } else {
                                        deleteResult = "Your recording was deleted";
                                        ((LinearLayout) activity.findViewById(R.id.linlay)).removeView(rec.getMyView());
                                        RecordingManager.recordingArrayList.remove(rec);
                                    }
                                    Toast.makeText(activity.getApplicationContext(), deleteResult, Toast.LENGTH_LONG).show();


                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            }).show();
                }
            });
            ((LinearLayout) activity.findViewById(R.id.linlay)).addView(rec.getMyView(), i);

        }
        //if (((LinearLayout) activity.findViewById(R.id.linlay)).getChildCount() > i)
        //  ((LinearLayout) activity.findViewById(R.id.linlay)).removeViews(i+1,((LinearLayout) activity.findViewById(R.id.linlay)).getChildCount());
        ((LinearLayout) activity.findViewById(R.id.linlay)).getChildCount();
        while (((LinearLayout) activity.findViewById(R.id.linlay)).getChildCount() > i) {
            int count = ((LinearLayout) activity.findViewById(R.id.linlay)).getChildCount();

            ((LinearLayout) activity.findViewById(R.id.linlay)).removeViewAt(count - 1);
        }
        ((ProgressBar) activityReference.get().findViewById(R.id.progressBar)).setIndeterminate(false);
        activityReference.get().findViewById(R.id.progressBar).setVisibility(View.GONE);
    }
}