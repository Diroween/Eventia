package com.proyect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Clase EventListAdapter que extiende la clase adaptador de recyclerview
 * para poder mostrar los eventos en una lista, con un viewholder personalizado
 * */

public class EventListAdapter extends RecyclerView.Adapter<EventListViewHolder>
{
    /**
     * Creamos como variable de clase un arraylist para contener los eventos
     * */
    ArrayList<Event> events;

    /**
     * Constructor con argumentos
     * */

    public EventListAdapter (ArrayList<Event> events)
    {
        this.events = events;
    }

    /**
     * Sobreescritura del método para poder inflar cada uno de los items con su layout personalizado
     * */

    @NonNull
    @Override
    public EventListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_item,
                parent, false);

        return new EventListViewHolder(view);
    }

    /**
     * Sobreescritura del método para poder dar funcionalidad a los elementos
     * en pantalla de cada elemento
     * */

    @Override
    public void onBindViewHolder(@NonNull EventListViewHolder holder, int position)
    {
        //Cogemos el evento correspondiente a la posición
        Event event = events.get(position);

        //Setteamos el texto texto a cada textview
        holder.tvEventName.setText(event.getName());
        holder.tvEventDate.setText(event.getDate());
        holder.tvEventPlace.setText(event.getPlace());

        //Si tiene una imagen asignada el evento se carga en el imageview
        //En caso contrario se carga un placeholder
        if(!event.getImage().isEmpty())
        {
            Glide.with(holder.itemView.getContext())
                    .load(event.getImage())
                    .placeholder(R.drawable.ic_event_list)
                    .into(holder.ivEventImage);
        }
    }

    /**
     * Método sobreescrito que devuelve la cantidad de eventos contenidos en el arraylist
     * */

    @Override
    public int getItemCount()
    {
        return events.size();
    }
}
