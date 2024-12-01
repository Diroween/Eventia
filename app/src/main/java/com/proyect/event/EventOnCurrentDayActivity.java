package com.proyect.event;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyect.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class EventOnCurrentDayActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton floatingActionButton;
    ArrayList<Event> events;
    EventListAdapter listAdapter;

    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_current_day);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Tomamos el intent de CalendarFragment
        String date = getIntent().getStringExtra("date");

        recyclerView = findViewById(R.id.rvEventsOnCurrentDay);
        floatingActionButton = findViewById(R.id.fbCreateEvent);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        events = new ArrayList<>();

        listAdapter = new EventListAdapter(EventOnCurrentDayActivity.this, events);

        recyclerView.setAdapter(listAdapter);

        floatingActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(getApplicationContext(), EventCreationActivity.class);
                intent.putExtra("date", date);
                startActivity(intent);
            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference().child("events");

        String currentUserId = FirebaseAuth.getInstance().getUid();

        //Le asignamos un escuchador a la referencia de datos
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            /**
             * Método sobreescrito para la lógica de funcionamiento
             * */

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                //vaciamos el array de eventos
                events.clear();

                //En el bucle:
                //recogemos cada evento
                //le damos un formato a las fechas que coincida
                // con el que se le pasa de calendar fragment
                //si la fecha pasada es igual a al evento encontrado se añade al array list
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Event event = dataSnapshot.getValue(Event.class);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                    Date eventDate;

                    String formattedDate;

                    try
                    {
                        eventDate  = sdf.parse(event.getDate());

                        formattedDate = sdf.format(eventDate);
                    }
                    catch (ParseException e)
                    {
                        throw new RuntimeException(e);
                    }


                    if (event != null && dataSnapshot.child("registeredUsers")
                            .hasChild(currentUserId))
                    {
                        if (date.equals(formattedDate))
                        {
                            events.add(event);
                        }
                    }
                }

                //Le indicamos al adaptador que los datos han cambiado
                listAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("INFO", "No se han podido cargar los eventos");
            }
        });

    }

}
