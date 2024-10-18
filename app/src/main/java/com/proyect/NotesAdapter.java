package com.proyect;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotesAdapter extends RecyclerView.Adapter<NotesViewHolder>
{

    Context context;
    ArrayList<Note> arrayNotes;

    public NotesAdapter(Context context, ArrayList<Note> arrayNotes)
    {
        this.context = context;
        this.arrayNotes = arrayNotes;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new NotesViewHolder(LayoutInflater.from(context).inflate(R.layout.item_note_view,
                parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position)
    {
        Note note = arrayNotes.get(position);

        holder.tvNoteName.setText(note.getNoteName());
        holder.tvNoteBody.setText(note.getNoteBody());

        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(view.getContext(), NotesActivity.class);

                intent.putExtra("note_name", note.getNoteName());
                intent.putExtra("note_body", note.getNoteBody());

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return arrayNotes.size();
    }
}
