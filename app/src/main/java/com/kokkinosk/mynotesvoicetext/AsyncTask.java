package com.kokkinosk.mynotesvoicetext;

import android.os.AsyncTask;

class BackgroundTask extends AsyncTask<Void, Integer, String> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
    @Override
    protected String doInBackground(Void...arg0) {

        return "You are at PostExecute";
    }
@Override
    protected void onProgressUpdate(Integer...a) {
        super.onProgressUpdate(a);

    }
@Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }
}
