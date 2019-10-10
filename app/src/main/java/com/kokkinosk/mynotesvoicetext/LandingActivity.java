package com.kokkinosk.mynotesvoicetext;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.kokkinosk.mynotesvoicetext.AsyncTasks.GenerateNotesViews;
import com.kokkinosk.mynotesvoicetext.AsyncTasks.GenerateRecordingViews;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;


public class LandingActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        PermissionsUtils.checkAndRequestPermissions(this);
        createSpeedDial();
        setColours();

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
        findViewById(R.id.scrollView).setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
//               Log.d("SCROLL","i = "+ i + " i1 = " + i1+ " i2 = " + i2+ " i3 = " + i3);
                if (i1>0){
                    speedDialView.hide();
                }
                else {
                    speedDialView.show();
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        new GenerateRecordingViews(this).execute();
        new GenerateNotesViews(this).execute();
    }



}



