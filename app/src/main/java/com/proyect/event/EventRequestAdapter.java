package com.proyect.event;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyect.R;

import java.util.ArrayList;

/**
 * Adaptador personalizado para poder cargar los elementos en la lista
 * */

public class EventRequestAdapter extends RecyclerView.Adapter<EventRequestViewHolder>
{
    /**
     * Variables de clase:
     * Un arraylista para contener las peticiones
     * Un escuchador personalizado
     * */

    private ArrayList<EventRequest> eventRequests;
    private OnRequestActionListener listener;

    /**
     * Constructor con argumentos
     * */

    public EventRequestAdapter(ArrayList<EventRequest> eventRequests
            , OnRequestActionListener listener)
    {
        this.eventRequests = eventRequests;
        this.listener = listener;
    }

    /**
     * Método para cargar el layout de cada item
     * */

    @NonNull
    @Override
    public EventRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_request, parent, false);

        return new EventRequestViewHolder(view);
    }

    /**
     * Método para poder dar funcionalidad y cargar los datos de cada elemento de la lsita
     * */

    @Override
    public void onBindViewHolder(@NonNull EventRequestViewHolder holder, int position)
    {
        //Guardamos el evento
        EventRequest eventRequest = eventRequests.get(position);

        //Cogemos una referencia a la base de datos
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        //Recogemos el nombre de cada evento con el id de cada invitación
        databaseReference.child("events").child(eventRequest.getEventId())
                .addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        //Asignamos el nombre al textview correspondiente
                        String eventName = snapshot.child("name").getValue(String.class);

                        holder.tvEventName.setText(eventName);
                    }

                    /**
                     * Si no se ha podido cargar cargamos el nombre como loading
                     * */
                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        Log.e("INFO", "No se ha podido cargar");
                    }
                });

        //Asinamos el escuchador a los botones
        holder.btnAcccept.setOnClickListener(l -> listener.onAccept(eventRequest.getEventId()));
        holder.btnReject.setOnClickListener(l -> listener.onReject(eventRequest.getEventId()));
    }

    /**
     * Método que devuelve la cantidad de eventos cargados
     * */

    @Override
    public int getItemCount()
    {
        return eventRequests.size();
    }

    /**
     * Interfaz personalziada para dar funcionalidad los botones
     * */

    public interface OnRequestActionListener
    {
        void onAccept(String eventId);
        void onReject(String eventId);
    }
}
