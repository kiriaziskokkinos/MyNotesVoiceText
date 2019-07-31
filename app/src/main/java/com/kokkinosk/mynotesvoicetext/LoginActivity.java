package com.kokkinosk.mynotesvoicetext;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        findViewById(R.id.login_offline_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(),LandingActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.login_online_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> execute = new AsyncTask<Void, Void, Void>() {
                    private WeakReference<Activity> activityReference;


                    final Activity activity = activityReference.get();

                    @Override
                    protected Void doInBackground(Void... voids) {
                        return null;
                    }


                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                    }
                }.execute();


                Intent intent = new Intent(view.getContext(), LandingActivity.class);
                startActivity(intent);
            }
        });

    }
}
