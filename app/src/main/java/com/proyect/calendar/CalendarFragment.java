package com.proyect.calendar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarDay;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyect.R;
import com.proyect.event.Event;
import com.proyect.event.EventCreationActivity;
import com.proyect.event.EventDateComparator;
import com.proyect.event.EventOnCurrentDayActivity;
import com.proyect.event.EventRequest;
import com.proyect.event.EventRequestsActivity;
import com.proyect.notification.NotificationHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * Clase para poder visualizar un calendario, realizar nuevos eventos
 * y ver cuando se tendrán los próximos eventos
 */
public class CalendarFragment extends Fragment {

    /**
     * Variables necesarias para crear el fragment
     */

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static Context context;
    /**
     * Creamos tantas variables de clase como elementos vayamos a tratar
     * Un calendarview personalizado
     * Una referencia a la base de datos
     * Una arraylist de días de calendario personalizables
     * Un adaptador para el recyclerview
     * una lista para los siguientes eventos
     * Un arraylist para contener los futuros eventos
     */

    public CalendarView calendarView;
    FloatingActionButton fbEventRequests;
    TextView tvEventRequests;
    private DatabaseReference databaseReference;
    private FirebaseUser user;
    private ArrayList<CalendarDay> calendarDays;
    private CalendarFragmentAdapter calendarAdapter;
    private RecyclerView rvCalendar;
    private ArrayList<Event> nextEvents;
    private ArrayList<Event> pastEvents;
    private ArrayList<String> allEvents;


    /**
     * Constructor vacío necesario para poder crear el fragment
     */

    public CalendarFragment() {

    }

    /**
     * Método para crear nuevas instancias de CalendarFragment
     */

    public static CalendarFragment newInstance() {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Método para poder crear el fragment
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    /**
     * Método para dar funcionalidad a los elementos que hay en pantalla
     * y poder cargar el layout del fragment
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //hacemos una vista que es la que cargará nuestro layout
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        tvEventRequests = view.findViewById(R.id.tv_event_req);

        //Inicializamos el calendario personalizado
        calendarView = (CalendarView) view.findViewById(R.id.cv_calendar);

        //Inicializamos la referencia a la bdd
        databaseReference = FirebaseDatabase.getInstance().getReference();

        //inicializamos el array de próximos eventos
        nextEvents = new ArrayList<Event>();

        pastEvents = new ArrayList<Event>();

        allEvents = new ArrayList<String>();

        //inicializamos el adaptador con un contexto y el array de próximos eventos
        calendarAdapter = new CalendarFragmentAdapter(view.getContext(), nextEvents);

        rvCalendar = view.findViewById(R.id.rv_calendar);

        //Le ponemos un gestor de layouts a la lista
        rvCalendar.setLayoutManager(new LinearLayoutManager(getContext()));

        rvCalendar.setAdapter(calendarAdapter);

        //Inicializamos el arraylist de calendardays con el que se pondrán los iconos
        calendarDays = new ArrayList<>();

        fbEventRequests = view.findViewById(R.id.fb_requests);

        fbEventRequests.setOnClickListener(l ->
        {
            Intent intentRequests = new Intent(view.getContext(), EventRequestsActivity.class);

            startActivity(intentRequests);
        });

        // Cargamos los eventos del usuario
        loadUserEvents();
        checkPendingEventRequests();


        calendarView.setOnCalendarDayClickListener(new OnCalendarDayClickListener() {
            @Override
            public void onClick(@NonNull CalendarDay calendarDay) {

                Calendar calendar = calendarDay.getCalendar();

                LocalDate fechaSelec = calendarDay.getCalendar().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                if (allEvents.contains(fechaSelec.toString())) {
                    Intent intent = new Intent(calendarView.getContext(), EventOnCurrentDayActivity.class);
                    intent.putExtra("date", fechaSelec.toString());
                    startActivity(intent);
                } else {
                    //Cogemos los datos de ese día
                    String year = String.valueOf(calendar.get(Calendar.YEAR));
                    String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
                    String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

                    //los juntamos en un string formateado
                    String date = year + "-" + month + "-" + day;

                    //Creamos un intent para pasar a la actividad de crear eventos
                    Intent intent = new Intent(calendarView.getContext(), EventCreationActivity.class);

                    //Pasamos el día seleccionado a la siguiente activity
                    intent.putExtra("date", date);

                    //iniciamos el intent
                    startActivity(intent);
                }
            }
        });

        return view;
    }

    /**
     * Método para actualizar la lista de eventos cuando uno se crea, elimina o modifica
     */

    @Override
    public void onResume() {
        super.onResume();
        loadUserEvents();
        checkPendingEventRequests();
    }

    /**
     * Método para poder cargar los eventos en el calendario y en la lista
     */

    public void loadUserEvents() {
        //Recogemos los datos del usuario de Firebase que ha iniciado sesión
        user = FirebaseAuth.getInstance().getCurrentUser();

        //A la referencia de la base de datos le indicamos que vaya al contenedor eventos
        //y le ponemos un escuchador para que encuentre coincidencias
        databaseReference.child("events").orderByChild("date")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //Vaciamos la arraylist por si hubiera alguno todavía
                        nextEvents.clear();
                        pastEvents.clear();
                        allEvents.clear();
                        calendarDays.clear();

                        //Creamos un formato para las fechas que se van a guardar
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                        //Dentro del bucle:
                        //Creamos un evento para cada coincidencia de la base de datos
                        //Si el usuario está registrado en ese evento
                        //se pasa a tratar los datos del evento
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Event event = dataSnapshot.getValue(Event.class);

                            if (event != null && dataSnapshot.child("registeredUsers")
                                    .hasChild(user.getUid())) {
                                try {
                                    //Formateamos las fechas guardar
                                    Date date = sdf.parse(event.getDate());
                                    String formattedDate = sdf.format(date);

                                    //Se añaden las fechas de los eventos al ArrayList para dar
                                    //funcionalidad mas adelante al listener del calendario
                                    //y manejar los eventos
                                    allEvents.add(formattedDate);

                                    //Establecemos un formato fecha/hora
                                    SimpleDateFormat simpleDateFormat =
                                            new SimpleDateFormat("yyyy-MM-dd HH:mm");

                                    //Se recoge la fecha del evento
                                    Date eventDate = simpleDateFormat.parse(event.getDate()
                                            + " " + event.getHour());

                                    //Se coge una instancia de Calendar de Java
                                    Calendar calendar = Calendar.getInstance();

                                    //Le decimos que se settee en el día del evento
                                    calendar.setTime(eventDate);

                                    //hacemos una nueva referencia que representa el día actual
                                    Calendar today = Calendar.getInstance();

                                    //Se hace un nuevo CalendarDay personalizado
                                    //del día del evento
                                    CalendarDay calendarDay = new CalendarDay(calendar);

                                    //Si el día del evento es posterior al día de hoy
                                    //Se carga en futuros eventos

                                    if (calendar.after(today)) {
                                        nextEvents.add(event);
                                        calendarDay.setBackgroundDrawable(ResourcesCompat
                                                .getDrawable(getResources(),
                                                        R.drawable.calendar_day_current,
                                                        null));
                                        //calendarDay.setLabelColor(R.color.black);
                                    } else if (calendar.before(today)) {
                                        pastEvents.add(event);
                                        calendarDay.setBackgroundDrawable(ResourcesCompat
                                                .getDrawable(getResources(),
                                                        R.drawable.calendar_day_past_event,
                                                        null));
                                        //calendarDay.setLabelColor(R.color.black);
                                    } else {
                                        nextEvents.add(event);
                                        calendarDay.setBackgroundDrawable(ResourcesCompat
                                                .getDrawable(getResources(),
                                                        R.drawable.calendar_day_current, null));
                                    }

                                    //Añadimos el día al calendario
                                    //Esto lo hace con todos los días, representando así todos los
                                    //días pasados donde también tuvo evento el usuario
                                    calendarDays.add(calendarDay);

                                    //Programamos las notificaciones del evento empleando el calculo de getSecondsUntilEvent()
                                    NotificationHelper.enqueueNotifications(getContext(), event, NotificationHelper.getSecondsUntilEvent(event));
                                }

                                //Si no se consigue parsear bien la fecha se recoge una excepción
                                catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }

                        //Ordenamos los eventos por fecha y hora
                        //para ello ordenamos el arraylist

                        nextEvents.sort(new EventDateComparator());

                        //añadimos al calendario todos los dias personalizados
                        calendarView.setCalendarDays(calendarDays);

                        //Le decimos al adaptador que la lista ha cambiado sus datos
                        calendarAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), R.string.loadeventserror,
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void checkPendingEventRequests()
    {
        //Cogemos la referencia a las invitaciones de eventos que tiene el usuario
        databaseReference.child("users").child(user.getUid())
                .child("eventsRequests").addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if (snapshot.getChildrenCount() > 0) {
                            tvEventRequests.setVisibility(View.VISIBLE);
                            tvEventRequests.setText(String.valueOf(snapshot.getChildrenCount()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        Log.e("INFO", "Fallo al cargar los eventos");
                    }
                });
    }


}