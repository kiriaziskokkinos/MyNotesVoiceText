package com.kokkinosk.mynotesvoicetext;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
class VisualizerView extends View {
    private static final int MAX_AMPLITUDE = 32767;

    private float[] amplitudes;
    private float[] vectors;
    private int insertIdx = 0;
    private Paint pointPaint;
    private Paint linePaint;
    private int width;
    private int height;

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        linePaint = new Paint();
        linePaint.setColor(Color.RED);
        linePaint.setStrokeWidth(5);
        pointPaint = new Paint();
        pointPaint.setColor(Color.BLUE);
        pointPaint.setStrokeWidth(5);
    }

    @Override
    protected void onSizeChanged(int width, int h, int oldw, int oldh) {
        this.width = width;
        height = h;
        amplitudes = new float[this.width * 2]; // xy for each point across the width
        vectors = new float[this.width * 4]; // xxyy for each line across the width
    }

    /**
     * modifies draw arrays. cycles back to zero when amplitude samples reach max screen size
     */
    public void addAmplitude(int amplitude) {
        invalidate();
        float scaledHeight = ((float) amplitude / MAX_AMPLITUDE) * (height - 1);
        int ampIdx = insertIdx * 2;
        amplitudes[ampIdx++] = insertIdx;   // x
        amplitudes[ampIdx] = scaledHeight;  // y
        int vectorIdx = insertIdx * 4;
        vectors[vectorIdx++] = insertIdx;   // x0
        vectors[vectorIdx++] = 0;           // y0
        vectors[vectorIdx++] = insertIdx;   // x1
        vectors[vectorIdx] = scaledHeight;  // y1


        // insert index must be shorter than screen width
        insertIdx = (insertIdx +10 ) >= width ? 0 : insertIdx +10;
    }

    public void clearVisualizer(){
        invalidate();
        insertIdx =0;
        amplitudes = new float[this.width * 2]; // xy for each point across the width
        vectors = new float[this.width * 4]; // xxyy for each line across the width

    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawLines(vectors, linePaint);
        canvas.drawPoints(amplitudes, pointPaint);
    }
}


public class RecordActivity extends AppCompatActivity {
    private static String fileName = null;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    private VisualizerView visualizerView;
    public TextView timerTextView;
    private long startHTime = 0L;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long duration = 0L;
    private String fullFileName;
    private Handler handler = new Handler();
    int maxAmplitude;
    final Runnable updater = new Runnable() {
        public void run() {
            handler.postDelayed(this, 10);
            if (recUIMan.isPaused) return;
             if (recorder != null) maxAmplitude = recorder.getMaxAmplitude();
             else maxAmplitude =0;
            if (maxAmplitude != 0) {
                visualizerView.addAmplitude(maxAmplitude);
//                visualizerView.addAmplitude(32767);

            }
        }
    };

    String directoryPath ;
    final RecordingUIManager recUIMan = new RecordingUIManager();



    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        handler.removeCallbacks(updater);
        handler = new Handler();
        handler.post(updater);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //------------ INITIAL -----------
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        visualizerView = findViewById(R.id.visualizerView);
        visualizerView.setScaleY(-1);
//        getLayoutInflater().inflate((VisualizerView) findViewById(R.id.visualizer),R.id.visualizer,null);

        //--------- PERMISSIONS ----------
        PermissionsUtils.checkAndRequestPermissions(this);


        //--------- VARIABLES ------------
        ActionBar actionbar = getSupportActionBar();
        if (User.isLoggedIn()) directoryPath = getFilesDir().getAbsolutePath()+"/Recordings/"+md5(User.getUserName());
        else directoryPath = getFilesDir().getAbsolutePath()+"/Recordings";
        Window window = this.getWindow();
        FloatingActionButton fab_rec_primary = findViewById(R.id.fab_rec_primary);
        FloatingActionButton fab_rec_secondary = findViewById(R.id.fab_rec_secondary);


        //--------- CUSTOMIZE LOOK ---------
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorDarkRed));
        timerTextView = findViewById(R.id.timer);
        if (actionbar != null) {
            actionbar.setTitle("New Recording");
            actionbar.setBackgroundDrawable(new ColorDrawable(Color.RED));
        }

        //---------- F.A.B. SETUP -----------

        ///------/* START/STOP/PAUSE RECORDING BUTTON */---------
        final Activity activity = this;

        fab_rec_primary.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!PermissionsUtils.hasPermissions(activity.getApplicationContext(), Manifest.permission.RECORD_AUDIO)){
                    PermissionsUtils.requestRecordAudioPermission(activity);
                }
                else recUIMan.mainAction(view);
            }
        });


        ///------/* STOP RECORDING BUTTON */---------
        fab_rec_secondary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recUIMan.stopRecording();
//                toggleRecordIcon(Status.RESET);
                view.animate()
                        .rotationBy(180)
                        .translationX(-view.getWidth() * 0.9f)
                        .alpha(0f);
                view.setVisibility(View.INVISIBLE);
                ((FloatingActionButton) findViewById(R.id.fab_rec_primary)).setImageDrawable(ContextCompat.getDrawable(findViewById(R.id.fab_rec_primary).getContext(), R.drawable.baseline_mic_white_48dp));
            }
        });


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (recUIMan.isRecording){
            recUIMan.stopRecordingNoPrompt();
            boolean delete = new File(fullFileName).getAbsoluteFile().delete();
        }


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
                    if (recUIMan.isRecording){
                        recUIMan.stopRecordingNoPrompt();
                        boolean delete = new File(fullFileName).getAbsoluteFile().delete();
//                        toggleRecordIcon(Status.RESET);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) findViewById(R.id.fab_rec_secondary).animate()
                                .rotationBy(180)
                                .translationX(-findViewById(R.id.fab_rec_secondary).getWidth() * 0.9f)
                                .alpha(0f);
                        findViewById(R.id.fab_rec_secondary).setVisibility(View.INVISIBLE);
                        ((FloatingActionButton) findViewById(R.id.fab_rec_primary)).setImageDrawable(ContextCompat.getDrawable(findViewById(R.id.fab_rec_primary).getContext(), R.drawable.baseline_mic_white_48dp));
                        findViewById(R.id.fab_rec_primary).setTag("RESET");

                    }
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

        void mainAction(View v){

            if (isRecording) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (isPaused) {
                        isPaused = false;
                        ((FloatingActionButton) findViewById(R.id.fab_rec_primary)).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_pause));
                        resumeRecording();
                    } else {
                        isPaused = true;
                        ((FloatingActionButton) findViewById(R.id.fab_rec_primary)).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_play));
                        pauseRecording();
                    }
                } else {
                    isRecording = false;
                    isPaused = false;
                    ((FloatingActionButton)findViewById(R.id.fab_rec_primary)).setImageDrawable(ContextCompat.getDrawable(findViewById(R.id.fab_rec_primary).getContext(), R.drawable.baseline_mic_white_48dp));
                    stopRecording();
                }
            }
            else {
                // IF NOT RECORDING
                isRecording = true;
                startRecording();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // TOGGLE  FAB_PRIMARY
                    ((FloatingActionButton) findViewById(R.id.fab_rec_primary)).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_pause));
                    // ANIMATE FAB_SECONDARY
                    findViewById(R.id.fab_rec_secondary).setVisibility(View.VISIBLE);
                    findViewById(R.id.fab_rec_secondary).animate()
                                .rotationBy(180)
                                .translationX(v.getWidth() * 0.9f)
                                .alpha(1.0f);
                }
                else {
                    // TOGGLE  FAB_PRIMARY
                    ((FloatingActionButton) findViewById(R.id.fab_rec_primary)).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_stop_rec));
                    // FAB_SECONDARY SHOULD BE INVISIBLE)
                }
            }
        }

        void startRecording() {

            fileName = Calendar.getInstance().getTime() +".m4a";
            fullFileName = directoryPath+ "/"+fileName;
            visualizerView.clearVisualizer();
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
//                toggleRecordIcon(Status.RECORDING);
            } catch (Exception e) {
                Log.e("RECORD", "prepare() failed");
                e.printStackTrace();
            }


        }

        void stopRecordingNoPrompt() {

            recorder.stop();
            recorder.release();
            visualizerView.clearVisualizer();
            customHandler.removeCallbacks(updateTimerThread);
            recorder = null;
            isRecording = false;
            isPaused = false;
            ((TextView) findViewById(R.id.timer)).setText("00:00");
//            if (!msg.equals("DISCARD")) showRenameDialog();
        }
        void stopRecording() {

            recorder.stop();
            recorder.release();
            visualizerView.clearVisualizer();
            customHandler.removeCallbacks(updateTimerThread);
            recorder = null;
            isRecording = false;
            isPaused = false;
            ((TextView) findViewById(R.id.timer)).setText("00:00");
//            toggleRecordIcon(Status.RESET);
            showRenameDialog();
        }

        void resumeRecording() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                recorder.resume();
                startHTime  = SystemClock.elapsedRealtime() - duration;
                isPaused = false;
//                toggleRecordIcon(Status.RECORDING);
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
//                toggleRecordIcon(Status.PAUSE);

            }
            else {
                Toast.makeText(getApplicationContext(),"Pause/Resume function does not work in this android version",Toast.LENGTH_SHORT).show();
            }

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
            if (recorder != null){
                int var = recorder.getMaxAmplitude();
//                ((TextView)findViewById(R.id.test)).setText(String.valueOf(var));
            }

        }
    };
    String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();
            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) hexString.append(Integer.toHexString(0xFF & b));
            return hexString.toString();
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }

    }


}

