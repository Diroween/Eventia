package com.proyect.calendar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarDay;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyect.event.Event;
import com.proyect.event.EventCreationActivity;
import com.proyect.event.EventLogActivity;
import com.proyect.event.EventRequestsActivity;
import com.proyect.R;
import com.proyect.notification.ReminderWorker;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


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

    FloatingActionButton fbEventLog;
    private String mParam1;
    private String mParam2;
    private DatabaseReference databaseReference;
    private ArrayList<CalendarDay> calendarDays;
    private CalendarFragmentAdapter calendarAdapter;
    private CalendarFragmentAdapter calendarAdapterPast;
    private RecyclerView rvCalendar;
    private RecyclerView rvCalendar2;
    private ArrayList<Event> nextEvents;
    private ArrayList<Event> pastEvents;

    private TabLayout tabLayout;
    private ViewPager viewPager;


    /**
     * Constructor vacío necesario para poder crear el fragment
     */

    public CalendarFragment() {

    }

    /**
     * Método para crear nuevas instancias de CalendarFragment
     */

    public static CalendarFragment newInstance(String param1, String param2) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Método para lanzar el trabajo en segundo plano
     */

    public static void createWorkRequest(String workName, String titulo, String message, long timeDelayInSeconds) {
        OneTimeWorkRequest myWorkRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                .setInitialDelay(timeDelayInSeconds, TimeUnit.SECONDS)
                .setInputData(new Data.Builder().putString("title", titulo).putString("message", message).build())
                .build();

        // Es posible pasar una List<OneTimeWorkRequest> directamente con el segundo método de .enqueueUniqueWork();
        WorkManager.getInstance(context).enqueueUniqueWork(
                workName,
                ExistingWorkPolicy.KEEP,
                myWorkRequest
        );
    }

    /**
     * Método para poder crear el fragment
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
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

        //Inicializamos el calendario personalizado
        calendarView = (CalendarView) view.findViewById(R.id.cv_calendar);

        //Inicializamos la referencia a la bdd
        databaseReference = FirebaseDatabase.getInstance().getReference();

        //inicializamos el array de próximos eventos
        nextEvents = new ArrayList<Event>();

        pastEvents = new ArrayList<Event>();

        //inicializamos el adaptador con un contexto y el array de próximos eventos
        calendarAdapter = new CalendarFragmentAdapter(view.getContext(), nextEvents);

        calendarAdapterPast = new CalendarFragmentAdapter(view.getContext(), pastEvents);

        //Inicializamos el arraylist de calendardays con el que se pondrán los iconos
        calendarDays = new ArrayList<>();

        fbEventRequests = view.findViewById(R.id.fb_requests);

        fbEventLog = view.findViewById(R.id.fb_event_log);

        fbEventLog.setOnClickListener(l -> {
            Intent intentLog = new Intent(view.getContext(), EventLogActivity.class);

            startActivity(intentLog);
        });

        fbEventRequests.setOnClickListener(l ->
        {
            Intent intentRequests = new Intent(view.getContext(), EventRequestsActivity.class);

            startActivity(intentRequests);
        });

        //Llamamos al método que cargará todos los eventos del usuario en los arraylists
        //tanto para el calendario como para la lista
        loadUserEvents();

        //Le setteamos un manejador de eventos para cuando pulsamos en alguna fecha
        calendarView.setOnCalendarDayClickListener(new OnCalendarDayClickListener() {
            @Override
            public void onClick(@NonNull CalendarDay calendarDay) {
                //instancia de Calendar de Java basada en el día que se pulsa
                Calendar calendar = calendarDay.getCalendar();

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
        });

        return view;
    }

    /**
     * Método para actualizar la lista de eventos cuando uno se elimina
     */

    @Override
    public void onResume() {
        super.onResume();
        loadUserEvents();
    }

    /**
     * Método para poder cargar los eventos en el calendario y en la lista
     */

    public void loadUserEvents() {
        //Recogemos los datos del usuario de Firebase que ha iniciado sesión
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //A la referencia de la base de datos le indicamos que vaya al contenedor eventos
        //y le ponemos un escuchador para que encuentre coincidencias
        databaseReference.child("events").orderByChild("date")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //Vaciamos la arraylist por si hubiera alguno todavía
                        nextEvents.clear();
                        pastEvents.clear();

                        //Dentro del bucle:
                        //Creamos un evento para cada coincidencia de la base de datos
                        //Si el usuario está registrado en ese evento
                        //se pasa a tratar los datos del evento
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Event event = dataSnapshot.getValue(Event.class);

                            if (event != null && dataSnapshot.child("registeredUsers")
                                    .hasChild(user.getUid())) {
                                try {
                                    //Se recoge la fecha del evento
                                    Date eventDate = new SimpleDateFormat("yyyy-MM-dd")
                                            .parse(event.getDate());

                                    //Se coge una instancia de Calendar de Java
                                    Calendar calendar = Calendar.getInstance();

                                    //Le decimos que se settee en el día del evento
                                    calendar.setTime(eventDate);

                                    //hacemos una nueva referencia que representa el día actual
                                    Calendar today = Calendar.getInstance();

                                    //Si el día del evento es posterior al día de hoy
                                    //Se carga en futuros eventos
                                    if (calendar.after(today)) {
                                        nextEvents.add(event);
                                    }

                                    if (calendar.before(today)) {
                                        pastEvents.add(event);
                                    }

                                    //Se hace un nuevo CalendarDay personalizado
                                    //del día del evento
                                    CalendarDay calendarDay = new CalendarDay(calendar);

                                    //Le ponemos una etiquetita para representar que ese día hay un evento

                                    //calendarDay.setImageResource(R.drawable.ic_event_list);
                                    //calendarDay.setBackgroundResource(R.drawable.baseline_delete_outline_24);
                                    calendarDay.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_exit_bckgrnd));
                                    calendarDay.setLabelColor(R.color.orange);

                                    //Añadimos el día al calendario
                                    //Esto lo hace con todos los días, representando así todos los
                                    //días pasados donde también tuvo evento el usuario
                                    calendarDays.add(calendarDay);

                                    // Dividimos el año, mes y día
                                    String[] fechas = event.getDate().split("-");

                                    // Dividimos horas y minutos
                                    String[] horas = event.getHour().split(":");

                                    LocalDate fechaEvento = LocalDate.of(Integer.parseInt(fechas[0]), Integer.parseInt(fechas[1]), Integer.parseInt(fechas[2]));
                                    LocalTime horaEvento = LocalTime.of(Integer.parseInt(horas[0]), Integer.parseInt(horas[1]));

                                    LocalDateTime fechaHoraActual = LocalDateTime.now();
                                    LocalDateTime fechaHoraEvento = LocalDateTime.of(fechaEvento, horaEvento);

                                    ZoneOffset offset = ZonedDateTime.now().getOffset();

                                    long delaySegundos = fechaHoraEvento.toEpochSecond(offset) - fechaHoraActual.toEpochSecond(offset);

                                    if (delaySegundos > 0) {
                                        createWorkRequest(event.getId(),
                                                "Tu evento \"" + event.getName() + "\" tendrá lugar el " + event.getDate() + " a las " + event.getHour(),
                                                "Ubicación: " + event.getPlace(),
                                                delaySegundos);


                                        //Toast.makeText(getContext(), ""+ delaySegundos, Toast.LENGTH_LONG).show();

                                        if (delaySegundos > 3600) {
                                            //delaySegundos = delaySegundos - 3600;
                                            createWorkRequest(event.getId() + "_3600",
                                                    "Tu evento \"" + event.getName() + "\" tendrá lugar dentro de una hora!",
                                                    "Ubicación: " + event.getPlace(),
                                                    delaySegundos - 3600);

                                            Log.d("TIME_RECORDER", String.valueOf(delaySegundos));

                                            if (delaySegundos > 86400) {
                                                //delaySegundos = delaySegundos - 86400;
                                                createWorkRequest(event.getId() + "_86400",
                                                        "Tu evento \"" + event.getName() + "\" tendrá lugar dentro de 24h!",
                                                        "Ubicación: " + event.getPlace(),
                                                        delaySegundos - 86400);

                                                Log.d("TIME_RECORDER", String.valueOf(delaySegundos));

                                                if (delaySegundos > 604800) {
                                                    //delaySegundos = delaySegundos - 604800;
                                                    createWorkRequest(event.getId() + "_604800",
                                                            "Tu evento \"" + event.getName() + "\" tendrá lugar dentro de 7 días!",
                                                            "Ubicación: " + event.getPlace(),
                                                            delaySegundos - 604800);

                                                    Log.d("TIME_RECORDER", String.valueOf(delaySegundos));

                                                }
                                            }
                                        }
                                    }
                                }

                                //Si no se consigue parsear bien la fecha se recoge una excepción
                                catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }

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


}