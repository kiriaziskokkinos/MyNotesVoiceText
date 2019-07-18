package com.kokkinosk.mynotesvoicetext;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

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
        LinearLayout linlay = findViewById(R.id.linlay);
        //linlay.removeAllViews();
        //new GenerateRecordingViews(this).execute();


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


    @Override
    protected void onResume() {
        super.onResume();
        new GenerateRecordingViews(this).execute();
    }
}