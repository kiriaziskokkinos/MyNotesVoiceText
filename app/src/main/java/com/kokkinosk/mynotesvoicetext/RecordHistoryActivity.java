package com.kokkinosk.mynotesvoicetext;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class RecordHistoryActivity extends AppCompatActivity {
    private Uri uri = null;
    private MediaMetadataRetriever mmr = new MediaMetadataRetriever();
    private String dirpath;
    private ArrayList<Recording> recordingArrayList = new ArrayList<>();
    File[] recordingArray;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_history);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Window window = this.getWindow();
        LinearLayout linlay = findViewById(R.id.linlay);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorDarkRed));
        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle("Recording History");
        actionbar.setBackgroundDrawable(new ColorDrawable(Color.RED));
        Intent intent = getIntent();
        dirpath = intent.getStringExtra("DIRPATH");
        if (dirpath == null){
            String extStore = System.getenv("EXTERNAL_STORAGE");
            File f_exts = new File(extStore);
            dirpath = f_exts.getAbsolutePath().concat("/"+getApplicationContext().getApplicationInfo().loadLabel(getApplicationContext().getPackageManager())+"/"+"Recordings");
        }

        File dir = new File(dirpath);
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return ( file.getAbsolutePath().matches(".*\\.m4a")  || file.getAbsolutePath().matches(".*\\.3gpp"));
            }
        };
        recordingArray = dir.listFiles(filter);

        if (recordingArray != null) {
            for (File file : recordingArray) {
                recordingArrayList.add(
                        new Recording(
                                file.getName()
                                , 0L
                                , Uri.fromFile(file)
                                , Formatter.formatShortFileSize(getApplicationContext(), file.length())
                        )
                );
            }
            new generateViews(this).execute();

        }
    }
    private static class generateViews extends AsyncTask<Void, View, String> {

        private WeakReference<RecordHistoryActivity> activityReference;

        // only retain a weak reference to the activity
        generateViews(RecordHistoryActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute(){
            ((ProgressBar)activityReference.get().findViewById(R.id.progressBar)).setIndeterminate(true);
        }
        @Override
        protected String doInBackground(Void... params) {

            // do some long running task...
            LayoutInflater inflater = activityReference.get().getLayoutInflater();
            for (int i = 0; i < activityReference.get().recordingArrayList.size(); i++) {
                activityReference.get().recordingArrayList.get(i).setMyView(inflater.inflate(R.layout.recording_item, (LinearLayout) activityReference.get().findViewById(R.id.linlay), false));
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
            final RecordHistoryActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            for (final Recording rec : activityReference.get().recordingArrayList) {
                currentView = rec.getMyView();
                currentView.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.colorWhite));
                rec_name = currentView.findViewById(R.id.noteTitle);
                rec_dur = currentView.findViewById(R.id.recording_duration);
                rec_size = currentView.findViewById(R.id.recording_size);
                rec_name.setText(rec.getTitle());
                activity.mmr.setDataSource(activity.getApplicationContext(), rec.getUri());
                String durationStr = activity.mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                millSecond = Integer.parseInt(durationStr);
                secs = (int) (millSecond / 1000);
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
                                        // boolean delete = recordingArray[finalI].getAbsoluteFile().delete();
                                        boolean delete = new File(rec.getUri().getPath()).delete();
                                        String deleteResult;
                                        if (!delete) {
                                            deleteResult = "Could not delete the file.";
                                        } else {
                                            deleteResult = "Your recording was deleted";
                                            ((LinearLayout) activity.findViewById(R.id.linlay)).removeView(rec.getMyView());
                                            activityReference.get().recordingArrayList.remove(rec);
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
                ((LinearLayout)activity.findViewById(R.id.linlay)).addView(rec.getMyView());
            }
            ((ProgressBar)activityReference.get().findViewById(R.id.progressBar)).setIndeterminate(false);
            ((ProgressBar)activityReference.get().findViewById(R.id.progressBar)).setVisibility(View.GONE);
        }
    }
}
