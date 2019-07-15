package com.kokkinosk.mynotesvoicetext;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.io.File;
import java.io.FileFilter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class LandingActivity extends AppCompatActivity {

    private ArrayList<Recording> recordingArrayList = new ArrayList<>();
    File[] recordingArray;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        PermissionsUtils.checkAndRequestPermissions(this);
        createSpeedDial();
        setColours();
        LinearLayout linlay = findViewById(R.id.linlay);
        new generateViews(this).execute();


    }
    private static class generateViews extends AsyncTask<Void, View, String> {

        private WeakReference<LandingActivity> activityReference;

        // only retain a weak reference to the activity
        generateViews(LandingActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute(){
            ((ProgressBar)activityReference.get().findViewById(R.id.progressBar)).setVisibility(View.VISIBLE);
        }
        @Override
        protected String doInBackground(Void... params) {
            LayoutInflater inflater = activityReference.get().getLayoutInflater();
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return ( file.getAbsolutePath().matches(".*\\.m4a")); // || file.getAbsolutePath().matches(".*\\.3gpp"));
                }
            };
            String extStore = System.getenv("EXTERNAL_STORAGE");
            File f_exts = new File(extStore);
            String dirpath = f_exts.getAbsolutePath().concat("/"+activityReference.get().getApplicationContext().getApplicationInfo().loadLabel(activityReference.get().getApplicationContext().getPackageManager())+"/"+"Recordings");
            File dir = new File(dirpath);
            activityReference.get().recordingArray = dir.listFiles(filter);

            if (activityReference.get().recordingArray != null) {
                for (File file : activityReference.get().recordingArray) {
                    activityReference.get().recordingArrayList.add(
                            new Recording(
                                    file.getName()
                                    , 0L
                                    , Uri.fromFile(file)
                                    , Formatter.formatShortFileSize(activityReference.get().getApplicationContext(), file.length())
                            )
                    );
                }
            }
            

            return null;
        }
        @Override
        protected void onProgressUpdate(View... progress) {

        }

        @Override
        protected void onPostExecute(String result) {
            ((ProgressBar)activityReference.get().findViewById(R.id.progressBar)).setVisibility(View.GONE);
        }
    }




















    void setColours(){
        Window window = this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorDarkOrange));
        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle("Notes & Voice Recordings");
        actionbar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this,R.color.colorOrange)));
    }






    void createSpeedDial(){
        final SpeedDialView speedDialView = findViewById(R.id.speedDial);
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_text, R.drawable.icon_text)
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorYellow, getTheme()))
                        .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, getTheme()))

                        .create()

        );
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_voice, R.drawable.icon_voice)
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorRed, getTheme()))
                        .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, getTheme()))
                        .create()

        );

        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem speedDialActionItem) {
                Intent action;
                if ( speedDialActionItem.getId() ==  R.id.fab_text ) {
                    action = new Intent(speedDialView.getContext(), NoteActivity.class);
                    startActivity(action);
                }
                else if (speedDialActionItem.getId() ==  R.id.fab_voice) {
                    action = new Intent(speedDialView.getContext(),RecordActivity.class);
                    startActivity(action);
                }
                return false;

            }
        });
    }
}