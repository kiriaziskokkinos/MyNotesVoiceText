package com.kokkinosk.mynotesvoicetext.AsyncTasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kokkinosk.mynotesvoicetext.Note;
import com.kokkinosk.mynotesvoicetext.NoteManager;
import com.kokkinosk.mynotesvoicetext.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class GenerateNotesViews extends AsyncTask<Void, View, String> {
    ArrayList<Note> notesArrayList = new ArrayList<>();
    NoteManager nman;
    private WeakReference<Activity> activityReference;

    public GenerateNotesViews(Activity context) {
        activityReference = new WeakReference<>(context);
        nman = new NoteManager(activityReference.get());
    }

    @Override
    protected String doInBackground(Void... voids) {

        nman.check();
        LayoutInflater inflater = activityReference.get().getLayoutInflater();
        for (int i = 0; i < NoteManager.notes.size(); i++) {
            NoteManager.notes.get(i).setMyView(inflater.inflate(R.layout.note_item, (LinearLayout) activityReference.get().findViewById(R.id.linlay), false));
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Note currentNote;
        for (int i = 0; i < NoteManager.notes.size(); i++) {
            currentNote = NoteManager.notes.get(i);
            ((TextView) currentNote.getMyView().findViewById(R.id.noteTitle)).setText(currentNote.getTitle());
            ((TextView) currentNote.getMyView().findViewById(R.id.noteBody)).setText(currentNote.getBody());
            ((LinearLayout) activityReference.get().findViewById(R.id.linlay)).addView(currentNote.getMyView());
            final int finalI = i;
            final Note finalCurrentNote = currentNote;
            currentNote.getMyView().findViewById(R.id.delete_img).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((LinearLayout) activityReference.get().findViewById(R.id.linlay)).removeView(finalCurrentNote.getMyView());
                    NoteManager.notes.remove(finalI);
                    NoteManager.updateNotes();
                }
            });
        }
    }
}
