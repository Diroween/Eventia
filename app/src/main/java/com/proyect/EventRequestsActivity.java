package com.proyect;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Actividad para poder aceptar las peticiones de eventos que tiene un usuario
 * */

public class EventRequestsActivity extends AppCompatActivity
{
    /**
     * Creamos tantas variables de clase como vayamos a tratar:
     * Una lista para cargar las peticiones
     * Un adaptador personalizado para lista
     * Un arraylist de las peticiones de evento
     * Una referencia a la base de datos
     * El usuario que está con la sesión iniciada
     * */

    RecyclerView rvEventRequests;
    EventRequestAdapter eventRequestAdapter;
    ArrayList<EventRequest> eventRequests;
    DatabaseReference databaseReference;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_requests);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Inicializamos las variables y elementos en pantalla
        rvEventRequests = findViewById(R.id.rv_events);

        rvEventRequests.setLayoutManager(new LinearLayoutManager(this));

        eventRequests = new ArrayList<EventRequest>();

        //Asignamos los metodos para aceptar y rechazar peticiones en el escuchador
        eventRequestAdapter = new EventRequestAdapter(eventRequests,
                new EventRequestAdapter.OnRequestActionListener()
                {
                    @Override
                    public void onAccept(String eventId)
                    {
                        acceptEventRequest(eventId);
                    }

                    @Override
                    public void onReject(String eventId)
                    {
                        declineEventRequest(eventId);
                    }
        });

        rvEventRequests.setAdapter(eventRequestAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //Cargamos los eventos en la lista
        loadEventRequests();
    }

    /**
     * Método para poder cargar las invitaciones a los eventos en la lista
     * */

    private void loadEventRequests()
    {
        //Cogemos la referencia a las invitaciones de eventos que tiene el usuario
        databaseReference.child("users").child(currentUser.getUid())
                .child("eventsRequests")
                .addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        //borramos los datos del arraylist
                        eventRequests.clear();

                        //En el bucle cogemos cada petición y la cargamos en el arraylist
                        for(DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            String eventId = dataSnapshot.getKey();
                            String status = dataSnapshot.getValue(String.class);

                            eventRequests.add(new EventRequest(eventId, status));
                        }

                        //Notificamos que los datos del adaptador han cambiado
                        eventRequestAdapter.notifyDataSetChanged();
                    }

                    /**
                     * Si no se han podido cargar se registra en el log un error
                     * */

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        Log.e("INFO", "Fallo al cargar los eventos");
                    }
                });
    }

    /**
     * Método para poder aceptar las peticiones de usuario
     * @param eventId el id del evento
     * */

    private void acceptEventRequest(String eventId)
    {
        //Cogemos la referencia al usuario y a los usuarios registrados en el evento
        DatabaseReference userRef = databaseReference.child("users")
                .child(currentUser.getUid());

        DatabaseReference eventRef = databaseReference.child("events").child(eventId)
                .child("registeredUsers");

        //Grabamos al usuario en los usuarios registrados de la app
        eventRef.child(currentUser.getUid()).setValue(true).addOnCompleteListener(task ->
                {
                    //Si se ha podido completar correctamente
                    if (task.isSuccessful())
                    {
                        //Se elimina la invitación del usuario al evento
                        //y se vuelven a cargar los eventos
                        userRef.child("eventsRequests").child(eventId).removeValue()
                                .addOnCompleteListener(t ->
                                {
                                    if(t.isSuccessful())
                                    {
                                        Toast.makeText(EventRequestsActivity.this,
                                                R.string.eventsignup, Toast.LENGTH_SHORT)
                                                .show();

                                        loadEventRequests();
                                    }
                                    else
                                    {
                                        Log.e("INFO", "Fallo al eliminar el evento");
                                    }
                                });
                    }

                    //Si no se ha podido guardar se registra en el log
                    else
                    {
                        Log.e("INFO", "Fallo al registrar el usuario en el evento");
                    }
                });

    }

    /**
     * Método para rechazar las invitaciones a eventos
     * */
    private void declineEventRequest(String eventId)
    {
        //cogemos la referencia al evento en las invitaciones a eventos del usuario
        DatabaseReference userRef = databaseReference.child("users")
            .child(currentUser.getUid()); userRef.child("eventsRequests")
            .child(eventId).removeValue()
            .addOnCompleteListener(task ->
            {
                //Si se ha podido borrar se indica con un toast y se actualiza la lista
                if (task.isSuccessful())
                { Toast.makeText(EventRequestsActivity.this
                        , R.string.eventdeclined, Toast.LENGTH_SHORT).show();

                    loadEventRequests();
                }

                //Si no se registra un log
                else
                {
                    Log.e("INFO", "No se han podido cargar rechazar");
                }
            });
    }
}