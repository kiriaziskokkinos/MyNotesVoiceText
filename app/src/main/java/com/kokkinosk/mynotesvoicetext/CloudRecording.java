package com.kokkinosk.mynotesvoicetext;

public class CloudRecording{
    String title;
    String filepath;
    CloudRecording() {}

    public CloudRecording(String title, String filepath){
        this.title = title;
        this.filepath = filepath;
    }

    public String getTitle() {
        return title;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
                        