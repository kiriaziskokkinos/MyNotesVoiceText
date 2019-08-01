package com.kokkinosk.mynotesvoicetext;

import android.view.View;

import androidx.annotation.Nullable;

public class Note {
    private String Title;
    private String Body;
    private transient View myView;
    Note() {}


    Note(String title, String body, @Nullable View view) {
        Title = title;
        Body = body;
        myView = view;
    }

    public View getMyView() {
        return myView;
    }

    public void setMyView(View myView) {
        this.myView = myView;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getBody() {
        return Body;
    }

    public void setBody(String body) {
        Body = body;
    }
}
