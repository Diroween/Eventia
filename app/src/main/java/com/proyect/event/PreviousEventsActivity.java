package com.proyect.event;

import android.os.Bundle;
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
import com.proyect.R;
import com.proyect.calendar.CalendarFragmentAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

public class PreviousEventsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    CalendarFragmentAdapter calendarFragmentAdapter;
    ArrayList<Event> pastEvents;

    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Métodos necesarios para que la cativity se muestre correctamente en pantalla

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_previous_events);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseReference = FirebaseDatabase.getInstance().getReference();

        loadUserEvents();

        recyclerView = findViewById(R.id.rvPastEvents);
        pastEvents = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        calendarFragmentAdapter = new CalendarFragmentAdapter(this, pastEvents);
        recyclerView.setAdapter(calendarFragmentAdapter);

    }

    public void loadUserEvents()
    {
        //Recogemos los datos del usuario de Firebase que ha iniciado sesión
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //A la referencia de la base de datos le indicamos que vaya al contenedor eventos
        //y le ponemos un escuchador para que encuentre coincidencias
        databaseReference.child("events").orderByChild("date")
                .addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        //Vaciamos la arraylist por si hubiera alguno todavía
                        pastEvents.clear();

                        //Dentro del bucle:
                        //Creamos un evento para cada coincidencia de la base de datos
                        //Si el usuario está registrado en ese evento
                        //se pasa a tratar los datos del evento
                        for (DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            Event event = dataSnapshot.getValue(Event.class);

                            if (event != null && dataSnapshot.child("registeredUsers")
                                    .hasChild(user.getUid()))
                            {
                                try
                                {
                                    //Establecemos un formato fecha/hora
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                                    //Se recoge la fecha del evento
                                    Date eventDate = simpleDateFormat.parse(event.getDate());

                                    //Se coge una instancia de Calendar de Java
                                    Calendar calendar = Calendar.getInstance();

                                    //Le decimos que se settee en el día del evento
                                    calendar.setTime(eventDate);

                                    //hacemos una nueva referencia que representa el día actual
                                    Calendar today = Calendar.getInstance();

                                    //Si el día del evento es posterior al día de hoy
                                    //Se carga en futuros eventos


                                    //Si es anterior, se carga en eventos pasados
                                    if (calendar.before(today))
                                    {
                                        pastEvents.add(event);
                                    }
                                }

                                //Si no se consigue parsear bien la fecha se recoge una excepción
                                catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }

                        pastEvents.sort(new Comparator<Event>()
                        {
                            @Override
                            public int compare(Event e1, Event e2)
                            {
                                //Damos un formato simple a la fecha y la hora y los comparamos
                                try
                                {
                                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");

                                    SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

                                    Date date1 = sdfDate.parse(e1.getDate());

                                    Date date2 = sdfDate.parse(e2.getDate());

                                    //Si la fecha es la misma pasamos a ordenar por hora
                                    if (date1.equals(date2))
                                    {
                                        //Cogemos las horas y se comparan
                                        Date time1 = sdfTime.parse(e1.getHour());
                                        Date time2 = sdfTime.parse(e2.getHour());

                                        return time1.compareTo(time2);
                                    }

                                    //Si las fechas no son iguales se ordena directamente por fecha
                                    else
                                    {
                                        return date1.compareTo(date2);
                                    }
                                }
                                catch (ParseException e)
                                {
                                    throw new RuntimeException(e);
                                }
                            }
                        });

                        //Le decimos al adaptador que la lista ha cambiado sus datos
                        calendarFragmentAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        Toast.makeText(getApplicationContext(), R.string.loadeventserror,
                                Toast.LENGTH_SHORT).show();
                    }
                });


    }
}
