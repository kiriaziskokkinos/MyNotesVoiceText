package com.kokkinosk.mynotesvoicetext.AsyncTasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;

import java.lang.ref.WeakReference;

public class DoLogin extends AsyncTask<Void, View, String> {
    private WeakReference<Activity> activityReference;

    // only retain a weak reference to the activity
    public DoLogin(Activity context) {
        activityReference = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... voids) {
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
}
