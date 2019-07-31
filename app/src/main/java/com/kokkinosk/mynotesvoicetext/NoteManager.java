package com.kokkinosk.mynotesvoicetext;

import android.content.Context;
import android.util.Log;

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

public class NoteManager {

    public static ArrayList<Note> notes;
    private static File file;
    private static Gson gson = new Gson();

    public NoteManager(Context applicationContext) {
        if (applicationContext == null) {
            Log.e("NoteManager","got NULL context in constructor");
        }
        notes = new ArrayList<>();
        String directoryName = applicationContext.getFilesDir().getPath()+"/"+"Notes";
        File directory = new File(directoryName);
        if (! directory.exists()) directory.mkdir();
        String fileName = "notes";
        file = new File(directoryName+"/"+fileName);
        notes = initializeNotes(applicationContext);
         Log.d("NoteManager", "Initialized and found " + notes.size() + " notes stored");
    }

    public boolean check() {
         return (notes == null);
    }

    public static void updateNotes() {
        try {
            writeChanges();
            JsonReader reader = null;
            file.createNewFile();
            reader = new JsonReader(new FileReader(file));

            Type type = new TypeToken<ArrayList<Note>>() {
            }.getType();
            ArrayList<Note> tmp = gson.fromJson(reader, type);
            notes = tmp;

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private static void writeChanges() {
        try {
            Type type = new TypeToken<ArrayList<Note>>() {
            }.getType();
            FileWriter fw = new FileWriter(file);
            fw.write(gson.toJson(notes, type));
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void addNote(String title, String body){
        notes.add(new Note(title, body, null));
        writeChanges();
    }

    private ArrayList<Note> initializeNotes(Context context){
        try {
            JsonReader reader = null;
            file.createNewFile();
            reader = new JsonReader(new FileReader(file));

            Type type = new TypeToken<ArrayList<Note>>() {
            }.getType();
            ArrayList<Note> tmp = gson.fromJson(reader, type);
            if (tmp == null) return new ArrayList<>();
            return tmp;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

     }

    ArrayList<Note> getNotesList() {
        return notes;
    }

}
