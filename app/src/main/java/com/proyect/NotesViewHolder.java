package com.proyect;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NotesViewHolder extends RecyclerView.ViewHolder
{
    TextView tvNoteName;
    TextView tvNoteBody;

    public NotesViewHolder(@NonNull View itemView)
    {
        super(itemView);

        tvNoteName = itemView.findViewById(R.id.tv_note_name);
        tvNoteBody = itemView.findViewById(R.id.tv_note_date);

    }



}
