package com.kokkinosk.mynotesvoicetext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.transition.Visibility;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Objects;

import static java.security.AccessController.getContext;

public class RecordActivity extends AppCompatActivity {
    boolean recording = false;
    boolean pause = false;
    private static String fileName = null;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    public TextView timerTextView;
    private long startHTime = 0L;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long duration = 0L;
    private String fullFileName;

    String directoryName;

    void toggleRecordIcon(){
       if (!recording || !pause){
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ((FloatingActionButton) findViewById(R.id.fab_rec)).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_pause));
            else ((FloatingActionButton) findViewById(R.id.fab_rec)).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_stop_rec));
       }
       else
           ((FloatingActionButton) findViewById(R.id.fab_rec)).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_play));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        PermissionsUtils.checkAndRequestPermissions(this);
        ActionBar actionbar = getSupportActionBar();

        Window window = this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorDarkRed));

        if (actionbar != null) {
            actionbar.setTitle("New Recording");
            actionbar.setBackgroundDrawable(new ColorDrawable(Color.RED));
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        String extStore = System.getenv("EXTERNAL_STORAGE");
        File f_exts = new File(extStore);
        directoryName = f_exts.getAbsolutePath().concat("/"+getApplicationContext().getApplicationInfo().loadLabel(getApplicationContext().getPackageManager())+"/"+"Recordings");
        File directory = new File(directoryName);
        if (! directory.exists()) directory.mkdir();
        fileName = Calendar.getInstance().getTime() +".m4a";
        fullFileName = directoryName+"/"+fileName;
        FloatingActionButton fab_rec = findViewById(R.id.fab_rec);
        FloatingActionButton fab_rec_stop = findViewById(R.id.fab_stop_rec);
        timerTextView = findViewById(R.id.timer);



        fab_rec_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecording();
                toggleRecordIcon();
                view.animate()
                        .rotationBy(180)
                        .translationX(-view.getWidth()*0.9f)
                        .alpha(0f);
                view.setVisibility(View.INVISIBLE);
                ((FloatingActionButton)findViewById(R.id.fab_rec)).setImageDrawable(ContextCompat.getDrawable(findViewById(R.id.fab_rec).getContext(), R.drawable.baseline_mic_white_48dp));
                showRenameDialog();

            }
        });

        fab_rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean retval = false;
                if(!recording) {
                    recording = true;
                    retval = startRecording();
                    if (retval){
                        toggleRecordIcon();
                        pause = false;
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"Something went wrong.",Toast.LENGTH_LONG).show();
                    }
                }
                else {

                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                        stopRecording();
                        ((FloatingActionButton)findViewById(R.id.fab_rec)).setImageDrawable(ContextCompat.getDrawable(findViewById(R.id.fab_rec).getContext(), R.drawable.baseline_mic_white_48dp));
                        showRenameDialog();
                    }

                    else if (!pause) {
                        pauseRecording();
                    }
                    else {
                        resumeRecording();
                    }
                }
                    
                if (retval && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
                    if (findViewById(R.id.fab_stop_rec).getVisibility() == View.INVISIBLE) {
                        findViewById(R.id.fab_stop_rec).setVisibility(View.VISIBLE);
                        findViewById(R.id.fab_stop_rec).animate()
                                .rotationBy(180)
                                .translationX(view.getWidth() * 0.9f)
                                .alpha(1.0f);

                    }
                }
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.history) {
//            Toast.makeText(RecordActivity.this, "Action clicked", Toast.LENGTH_LONG).show();
            findViewById(R.id.history).animate().rotationBy(-360).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    startActivity(new Intent(getApplicationContext(),RecordHistoryActivity.class).putExtra("DIRPATH",directoryName));
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
                        fullFileName = directoryName + "/"+ fileName;
                        File to = new File(fullFileName);
                        if(from.exists())
                            from.renameTo(to);
                        Toast.makeText(getApplicationContext(),"Recording saved to '"+ fullFileName+"'",Toast.LENGTH_LONG).show();
                    }
                })
//                .setNegativeButton("Don't rename", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        Toast.makeText(getApplicationContext(),"Recording saved to '"+ fullFileName+"'",Toast.LENGTH_LONG).show();
//                    }
//                })
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

    private void stopRecording() {

        recorder.stop();
        recorder.release();
        customHandler.removeCallbacks(updateTimerThread);
        recorder = null;
        recording = false;
        pause = false;
        ((TextView) findViewById(R.id.timer)).setText("00:00");
    }

    private void resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder.resume();
            startHTime  = SystemClock.elapsedRealtime() - duration;
            pause = false;
            toggleRecordIcon();
        }
        else {
            Toast.makeText(getApplicationContext(),"Pause/Resume function does not work in this android version",Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder.pause();
            duration = timeInMilliseconds;
            pause = true;
            toggleRecordIcon();


        }
        else {
            Toast.makeText(getApplicationContext(),"Pause/Resume function does not work in this android version",Toast.LENGTH_SHORT).show();
        }
        
    }

    private boolean startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        fileName = Calendar.getInstance().getTime() +".m4a";
        fullFileName = directoryName+"/"+fileName;
        recorder.setOutputFile(fullFileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        try {
            recorder.prepare();
            recorder.start();
            recording = true;
            startHTime = SystemClock.elapsedRealtime();
            customHandler.postDelayed(updateTimerThread, 0);
            return true;
        } catch (Exception e) {
            Log.e("RECORD", "prepare() failed");
            e.printStackTrace();
            return false;
        }


    }
    private Runnable updateTimerThread = new Runnable() {


        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        public void run() {

            timeInMilliseconds = SystemClock.elapsedRealtime() - startHTime;
            int secs = (int) (timeInMilliseconds / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            if (timerTextView != null && !pause)
                timerTextView.setText("" + String.format("%02d", mins) + ":" + String.format("%02d", secs));
            customHandler.postDelayed(this,500);
        }


    };


}

