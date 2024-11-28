package com.proyect.event;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.proyect.R;

import java.util.ArrayList;

/**
 * Clase EventListAdapter que extiende la clase adaptador de recyclerview
 * para poder mostrar los eventos en una lista, con un viewholder personalizado
 */

public class EventListAdapter extends RecyclerView.Adapter<EventListViewHolder> {
    /**
     * Creamos como variable de clase un arraylist para contener los eventos
     */
    ArrayList<Event> events;

    /**
     * Constructor con argumentos
     */

    public EventListAdapter(ArrayList<Event> events) {
        this.events = events;
    }

    /**
     * Sobreescritura del método para poder inflar cada uno de los items con su layout personalizado
     */

    @NonNull
    @Override
    public EventListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_list_fragment, parent, false);

        return new EventListViewHolder(view);
    }

    /**
     * Sobreescritura del método para poder dar funcionalidad a los elementos
     * en pantalla de cada elemento
     */

    @Override
    public void onBindViewHolder(@NonNull EventListViewHolder holder, int position) {
        //Cogemos el evento correspondiente a la posición
        Event event = events.get(position);

        //Setteamos el texto texto a cada textview
        holder.tvEventName.setText(event.getName());
        holder.tvEventDate.setText(event.getDate());
        holder.tvEventPlace.setText(event.getPlace());

        //Si tiene una imagen asignada el evento se carga en el imageview
        //En caso contrario se carga un placeholder
        if (event.getImage() != null) {
            Glide.with(holder.itemView.getContext()).load(event.getImage()).fitCenter().transform(new CircleCrop()).placeholder(R.drawable.ic_event_list).into(holder.ivEventImage);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Event event = events.get(holder.getAbsoluteAdapterPosition());

                Intent intent = new Intent(view.getContext(), EventViewerActivity.class);
                intent.putExtra("event_name", event.getName());
                intent.putExtra("event_id", event.getId());
                intent.putExtra("event_place", event.getPlace());
                intent.putExtra("event_date", event.getDate());
                intent.putExtra("event_hour", event.getHour());
                intent.putExtra("event_image", event.getImage());

                view.getContext().startActivity(intent);
            }
        });
    }

    /**
     * Método sobreescrito que devuelve la cantidad de eventos contenidos en el arraylist
     */

    @Override
    public int getItemCount() {
        return events.size();
    }


}
