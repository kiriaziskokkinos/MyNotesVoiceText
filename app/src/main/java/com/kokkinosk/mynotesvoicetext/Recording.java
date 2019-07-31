package com.kokkinosk.mynotesvoicetext;

import android.net.Uri;
import android.util.Log;
import android.view.View;

public class Recording {
    private String Title;
    private long Duration;
    private Uri filePath;
    private String fileSize;
    private View myView;

    Recording(String title, long duration, Uri uri, String size, View view){
        Title = title;
        Duration = duration;
        filePath = uri;
        fileSize = size;
        myView = view;
        Log.d("RECORD","Created new Record: " + Title + " with path " + filePath.getPath() );
    }


    Recording(String title, long duration, Uri uri, String size){
        Title = title;
        Duration = duration;
        filePath = uri;
        fileSize = size;
        myView = null;
        Log.d("RECORD","Created new Record: " + Title + " with path " + filePath.getPath() );
    }

    public String getTitle() {
        return Title;
    }

    void setTitle(String title){ Title = title;}

    public Uri getUri() {
        return filePath;
    }

    void setUri(Uri uri){ filePath = uri; }

    public View getMyView() {
        return myView;
    }

    public void setMyView(View myView) {
        this.myView = myView;
    }

    long getDuration() { return Duration; }

    void setDuration(long duration) { Duration = duration;  }

    public String getFileSize() {
        return fileSize;
    }

    void setFileSize(String fileSize) { this.fileSize = fileSize; }
}
