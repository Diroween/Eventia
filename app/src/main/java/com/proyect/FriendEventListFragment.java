package com.proyect;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Fragment FriendEventListFragment para mostrar los eventos
 * en los que un usuario coincide con us amigo
 */
public class FriendEventListFragment extends Fragment
{
    /**
     * Variables creadas por la clase fragment para su correcto funcionamiento
     * */

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    /**
     * Creamos las variables de clase:
     * //
     * Un recyclerview para poder mostrar los eventos
     * Un adaptador para pasarle al recyclerview
     * Un arraylist de eventos
     * Una referencia a la base de datos
     * */

    RecyclerView rvEvents;
    EventListAdapter listAdapter;
    ArrayList<Event> events;
    DatabaseReference databaseReference;

    /**
     * Constructor vacío necesario para el funcionamiento del fragment
     * */

    public FriendEventListFragment()
    {
        //Constructor vacío necesario
    }

    /**
     * Método para poder crear instancias del fragmento
     * */

    public static FriendEventListFragment newInstance(String param1, String param2)
    {
        FriendEventListFragment fragment = new FriendEventListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Método onCreate necesario
     * */

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /**
     * Método para realizar la lógica del fragment cuando se crea la vista
     * */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        //Creamos una vista que infle el layout
        View view = inflater.inflate(R.layout.fragment_friend_event_list, container, false);

        //Asignamos el recyclerview y le setteamos su layout manager
        rvEvents = view.findViewById(R.id.rv_event_list);
        rvEvents.setLayoutManager(new LinearLayoutManager(view.getContext()));

        //Inicializamos el arraylist
        events = new ArrayList<Event>();

        //Pasamos el arraylist como argumento para inicializar el listadapter
        listAdapter = new EventListAdapter(events);

        //Setteamos el adapter a la lista
        rvEvents.setAdapter(listAdapter);

        //Le decimos que guarde como referencia de la base de datos el hijo events
        //*-Yosef-* Esto lo hago directamente ya así para no estar escribiendo
        //un montonazo de lineas como en otras clases, que igual queda menos claro
        //pero no tiene sentido splittear en tantos paso siempre los métodos porque
        //se convierte es un horror ver tantas líneas
        databaseReference = FirebaseDatabase.getInstance().getReference().child("events");

        //Creamos dos strings en los que guardaremos el ID del usuario iniciado
        //y el id del amigo que se ha decidido consultar de la lista
        String currentUserId = FirebaseAuth.getInstance().getUid();
        String targetUserId = getActivity().getIntent().getStringExtra("friendId");

        //Le asignamos un escuchador a la referencia de datos
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            /**
             * Método sobreescrito para la lógica de funcionamiento
             * */

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                //Vaciamos el arraylist
                events.clear();

                //En el bucle:
                //cogemos cada evento encontrado en el contenedor
                //events que cogimos en la referencia
                //si existe algún evento y ese evento tiene como usuario registrado
                //al usuario y a su amigo lo guarda en el arraylist
                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Event event = dataSnapshot.getValue(Event.class);

                    if(event != null &&
                            dataSnapshot.child("registeredUsers").hasChild(currentUserId)
                            && dataSnapshot.child("registeredUsers").hasChild(targetUserId))
                    {
                        events.add(event);
                    }
                }

                //Le indicamos al adaptador que los datos han cambiado
                listAdapter.notifyDataSetChanged();
            }

            /**
             * Si no se han podido cargar los eventos se manda un toast informativo
             * */

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Log.e("INFO", "No se han podido cargar los eventos");
            }
        });

        //retornamos la vista para que se cargue el fragment
        return view;
    }
}