package com.kokkinosk.mynotesvoicetext;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.Calendar;

public class RecordActivity extends AppCompatActivity {
    private static String fileName = null;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    public TextView timerTextView;
    private long startHTime = 0L;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long duration = 0L;
    private String fullFileName;
    enum Status {
        RECORDING,RESET,PAUSE
    }
    String directoryPath ;
    final RecordingUIManager recUIMan = new RecordingUIManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //------------ INITIAL -----------
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //--------- PERMISSIONS ----------
        PermissionsUtils.checkAndRequestPermissions(this);


        //--------- VARIABLES ------------
        ActionBar actionbar = getSupportActionBar();
        directoryPath = getFilesDir().getAbsolutePath();
        Window window = this.getWindow();
        FloatingActionButton fab_rec = findViewById(R.id.fab_rec);
        FloatingActionButton fab_rec_stop = findViewById(R.id.fab_stop_rec);


        //--------- CUSTOMIZE LOOK ---------
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorDarkRed));
        timerTextView = findViewById(R.id.timer);
        if (actionbar != null) {
            actionbar.setTitle("New Recording");
            actionbar.setBackgroundDrawable(new ColorDrawable(Color.RED));
        }

        //---------- F.A.B. SETUP -----------

        ///------/* STOP RECORDING BUTTON */---------
        fab_rec_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // stopRecording();
                //toggleRecordIcon();
                recUIMan.stopRecording();
                toggleRecordIcon(Status.RESET);
                view.animate()
                        .rotationBy(180)
                        .translationX(-view.getWidth() * 0.9f)
                        .alpha(0f);
                view.setVisibility(View.INVISIBLE);
                ((FloatingActionButton) findViewById(R.id.fab_rec)).setImageDrawable(ContextCompat.getDrawable(findViewById(R.id.fab_rec).getContext(), R.drawable.baseline_mic_white_48dp));
                findViewById(R.id.fab_rec).setTag("RESET");

            }
        });

        ///------/* START/PAUSE RECORDING BUTTON */---------

        fab_rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recUIMan.mainAction(view);

            }
        });
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.record_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.history) {
            findViewById(R.id.history).animate().rotationBy(-360).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    startActivity(new Intent(getApplicationContext(),RecordHistoryActivity.class).putExtra("DIRPATH",directoryPath));
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void showRenameDialog() {
        final EditText newFilename = new EditText(this);
        newFilename.setText(fileName);

         new AlertDialog.Builder(this)
                .setTitle("Save recording as")
                .setCancelable(false)
                .setView(newFilename)

                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        File from = new File(fullFileName);

                        fileName = newFilename.getText().toString();
                        fullFileName = directoryPath + "/"+ fileName;
                        File to = new File(fullFileName);
                        if(from.exists())
                            from.renameTo(to);
                        Toast.makeText(getApplicationContext(),"Recording saved to '"+ fullFileName+"'",Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean delete = new File(fullFileName).getAbsoluteFile().delete();
                        if (!delete){
                            Toast.makeText(getApplicationContext(),"Could not delete the file.",Toast.LENGTH_LONG).show();
                        }
                        else Toast.makeText(getApplicationContext(),"Your recording was deleted",Toast.LENGTH_LONG).show();
                    }
                }).show();
    }





    private class RecordingUIManager{

        private boolean isRecording = false;
        private boolean isPaused = false;


        boolean isRecording(){
            return isRecording;
        }

        boolean isPaused(){
            return isPaused;
        }


        void mainAction(View v){
            String tag = (String) v.getTag();

            /*
            *
            *   RESET -> RECORD -> PAUSE -> RECORD
            *
            *
            */
            if (tag.equals("RESET")){
                startRecording();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (findViewById(R.id.fab_stop_rec).getVisibility() == View.INVISIBLE) {
                        findViewById(R.id.fab_stop_rec).setVisibility(View.VISIBLE);
                        findViewById(R.id.fab_stop_rec).animate()
                                .rotationBy(180)
                                .translationX(v.getWidth() * 0.9f)
                                .alpha(1.0f);


                    }
                }
                v.setTag("RECORD");
            }
            else if (tag.equals("PAUSE")){
                resumeRecording();
                v.setTag("RECORD");
            }
            else if (tag.equals("RECORD")){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    pauseRecording();
                    v.setTag( "PAUSE");
                }
                // STOP RECORDING ON ANDROID 6.0 INSTEAD OF PAUSE BECAUSE OF NO NATIVE SUPPORT
                else {
                    stopRecording();
                    v.setTag("RESET");
                }
            }
        }

        boolean startRecording() {
            fileName = Calendar.getInstance().getTime() +".m4a";
            fullFileName = directoryPath+ "/"+fileName;

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setOutputFile(fullFileName);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

            try {
                recorder.prepare();
                recorder.start();
                isRecording = true;
                startHTime = SystemClock.elapsedRealtime();
                customHandler.postDelayed(updateTimerThread, 0);
                toggleRecordIcon(Status.RECORDING);
                return true;
            } catch (Exception e) {
                Log.e("RECORD", "prepare() failed");
                e.printStackTrace();
                return false;
            }


        }

        @SuppressLint("SetTextI18n")
        void stopRecording() {

            recorder.stop();
            recorder.release();
            customHandler.removeCallbacks(updateTimerThread);
            recorder = null;
            isRecording = false;
            isPaused = false;
            ((TextView) findViewById(R.id.timer)).setText("00:00");
            toggleRecordIcon(Status.RESET);
            showRenameDialog();
        }

        void resumeRecording() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                recorder.resume();
                startHTime  = SystemClock.elapsedRealtime() - duration;
                isPaused = false;
                toggleRecordIcon(Status.RECORDING);
            }
            else {
                Toast.makeText(getApplicationContext(),"Pause/Resume function does not work in this android version",Toast.LENGTH_SHORT).show();
            }
        }

        void pauseRecording() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                recorder.pause();
                duration = timeInMilliseconds;
                isPaused = true;
                toggleRecordIcon(Status.PAUSE);

            }
            else {
                Toast.makeText(getApplicationContext(),"Pause/Resume function does not work in this android version",Toast.LENGTH_SHORT).show();
            }

        }






    }


    void toggleRecordIcon(Status status  ){
        switch (status) {
            case RESET:
                ((FloatingActionButton)findViewById(R.id.fab_rec)).setImageDrawable(ContextCompat.getDrawable(findViewById(R.id.fab_rec).getContext(), R.drawable.baseline_mic_white_48dp));
                break;
            case PAUSE:
                ((FloatingActionButton) findViewById(R.id.fab_rec)).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_play));
                break;
            case RECORDING:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    ((FloatingActionButton) findViewById(R.id.fab_rec)).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_pause));
                else
                    ((FloatingActionButton) findViewById(R.id.fab_rec)).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_stop_rec));
                break;
        }
    }



    private Runnable updateTimerThread = new Runnable() {


        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        public void run() {

            timeInMilliseconds = SystemClock.elapsedRealtime() - startHTime;
            int secs = (int) (timeInMilliseconds / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            if (timerTextView != null && !recUIMan.isPaused)
                timerTextView.setText("" + String.format("%02d", mins) + ":" + String.format("%02d", secs));
            customHandler.postDelayed(this,500);
        }
    };


}

