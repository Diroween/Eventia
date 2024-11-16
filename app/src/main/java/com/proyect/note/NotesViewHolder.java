package com.proyect.note;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.proyect.R;

/**
 * Clase que extiende ViewHolder para poder crear una lista RcyclerView
 * */

public class NotesViewHolder extends RecyclerView.ViewHolder
{
    /**
     * Variavles de clase que son los textview de la vista
     * que mostrarán el nombre y el cuerpo de la nota
     * */

    TextView tvNoteName;
    TextView tvNoteBody;

    /**
     * Constructor con argumentos que asigna las vistas a los textviews
     * */

    public NotesViewHolder(@NonNull View itemView)
    {
        super(itemView);

        tvNoteName = itemView.findViewById(R.id.tv_note_name);
        tvNoteBody = itemView.findViewById(R.id.tv_note_date);

    }



}
