package com.kokkinosk.mynotesvoicetext;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class RecordHistoryActivity extends AppCompatActivity {

    //RecordingManager recman;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_history);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Window window = this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorDarkRed));
        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle("Recording History");
        actionbar.setBackgroundDrawable(new ColorDrawable(Color.RED));
        //recman = new RecordingManager(this);
        new GenerateRecordingViews(this).execute();
    }

}
