package com.kokkinosk.mynotesvoicetext;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class NoteActivity extends AppCompatActivity {
    NoteManager nman;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ActionBar actionbar = getSupportActionBar();
        Window window = this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorDarkYellow));
        nman = new NoteManager(this.getApplicationContext());
        if (nman.check()) finish();
        if (actionbar != null) {
            actionbar.setTitle("New Note");
            actionbar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this,R.color.colorYellow)));
        }
            FloatingActionButton fab = findViewById(R.id.fab_done);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    nman.addNote(((EditText)findViewById(R.id.noteTitle)).getText().toString(),((EditText)findViewById(R.id.noteBody)).getText().toString());
                    finish();
                }
            });


    }
}
