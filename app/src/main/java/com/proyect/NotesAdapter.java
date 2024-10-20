package com.proyect;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

/**
 * Adaptador personalizado para poder hacer el RecyclerView
 * */

public class NotesAdapter extends RecyclerView.Adapter<NotesViewHolder>
{
    /**
     * Variables de clase, el contexto y un arraylist de notas
     * */

    Context context;
    ArrayList<Note> arrayNotes;

    /**
     * Constructor con argumentos
     * */

    public NotesAdapter(Context context, ArrayList<Note> arrayNotes)
    {
        this.context = context;
        this.arrayNotes = arrayNotes;
    }

    /**
     * Sobreescritura del método onCreateViewHolder
     *
     * @return NotesViewHolder una instancia de NotesViewHolder
     * */

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        //se indica que el holder tiene que inflarse con el layout de item
        return new NotesViewHolder(LayoutInflater.from(context).inflate(R.layout.item_note_view,
                parent, false));
    }

    /**
     * Método en el que se indica la lógica del adaptador y sus items
     * */

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position)
    {
        //Cogemos la nota correspondiente a la posición del arraylist
        Note note = arrayNotes.get(position);

        //indicamos que cada textview tenga su texto correspondiente
        holder.tvNoteName.setText(note.getNoteName());
        holder.tvNoteBody.setText(note.getNoteBody());

        //Le ponemos al holder, el cual es cada uno de los items de la lista,
        //que debe hacer al ser pulsados
        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            /**
             * Sobreescritura del método onClick
             * */
            @Override
            public void onClick(View view)
            {
                //Cogemos la instancia de FirebaseAuthentication que estamos usando
                //y que hemos inicializado en la LoginActivity
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                //Cogemos el usuario actual con el que se ha iniciado sesión
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                //Creamos un intent para que se abra la notes activity
                //esto nos permitirá abrir las notas y modificarlas
                Intent intent = new Intent(view.getContext(), NotesActivity.class);

                //Pasamos como extras el nombre para el contenedor de notas
                //el nombre de la nota y su cuerpo

                intent.putExtra("note_store", firebaseUser.getEmail());
                intent.putExtra("note_name", note.getNoteName());
                intent.putExtra("note_body", note.getNoteBody());

                //iniciamos al actividad
                context.startActivity(intent);
            }
        });
    }

    /**
     * Método contador de items de la lista
     * */
    @Override
    public int getItemCount()
    {
        return arrayNotes.size();
    }
}
