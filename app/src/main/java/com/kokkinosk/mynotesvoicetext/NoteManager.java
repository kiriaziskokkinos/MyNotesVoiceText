package com.kokkinosk.mynotesvoicetext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.JsonWriter;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

class  NoteManager {

    private List<Note> notes;
    File file;
    Gson gson = new Gson();

     NoteManager(Context applicationContext){
        if (applicationContext == null) {
            Log.e("NoteManager","got NULL context in constructor");
        }
        notes = new ArrayList<>();
        String directoryName = applicationContext.getFilesDir().getPath()+"/"+"Notes";
        File directory = new File(directoryName);
        if (! directory.exists()) directory.mkdir();
        String fileName = "notes";
        file = new File(directoryName+"/"+fileName);
        //file.delete();
         //Toast.makeText(applicationContext,directoryName+"/"+fileName,Toast.LENGTH_LONG).show();
        notes = initializeNotes(applicationContext);
        if (notes == null) {
           // Toast.makeText(applicationContext,"An exception has occured.",Toast.LENGTH_LONG).show();
        }
        else Log.d("NoteManager","Initialized and found " + notes.size() + " notes stored");
    }

    boolean check(){
         return (notes == null);
    }

    void addNote(String title, String body){
        notes.add(new Note(title,body));
        writeChanges();
    }

    ArrayList<Note> initializeNotes(Context context){
        try {
            JsonReader reader = null;
            file.createNewFile();
            reader = new JsonReader(new FileReader(file));
            ArrayList<Note> tmp =  gson.fromJson(reader, List.class);
            if (tmp == null) return new ArrayList<>();
            Toast.makeText(context,tmp.size()+" notes.",Toast.LENGTH_LONG).show();
            return tmp;
        } catch (FileNotFoundException e) {
            //Toast.makeText(context,"FileNotFoundException.",Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            //Toast.makeText(context,"IOException.",Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return null;
        }

     }



    void writeChanges(){
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(gson.toJson(notes,List.class));
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
