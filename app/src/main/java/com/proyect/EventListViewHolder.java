package com.proyect;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Clase EventListViewHolder que extiende el ViewHolder de Recyclerview para poder
 * asignar cada elemento visual con su variable y poder asignarles su funcionalidad
 * */

public class EventListViewHolder extends RecyclerView.ViewHolder
{

    /**
     * Creamos las variables de clase,
     * tantas como elementos tengamos que dar funcionalidad
     * */

    TextView tvEventName;
    TextView tvEventDate;
    TextView tvEventPlace;
    ImageView ivEventImage;

    /**
     * Método sobreescrito que llama a la superclase
     * y se asignan las variables a los elementos en pantalla
     * */

    public EventListViewHolder(@NonNull View itemView)
    {
        super(itemView);

        tvEventDate = itemView.findViewById(R.id.tv_event_date_title);
        tvEventName = itemView.findViewById(R.id.tv_event_name);
        tvEventPlace = itemView.findViewById(R.id.tv_event_place);
        ivEventImage = itemView.findViewById(R.id.iv_event_image);
    }
}
