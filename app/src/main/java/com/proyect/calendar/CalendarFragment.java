package com.proyect.calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

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
import com.proyect.event.EventOnCurrentDayActivity;
import com.proyect.event.EventRequestsActivity;
import com.proyect.notification.ReminderWorker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
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
    private String mParam1;
    private String mParam2;
    private DatabaseReference databaseReference;
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

    public static CalendarFragment newInstance(String param1, String param2)
    {
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

        //empleamos ExistingWorkPolicy.REPLACE para sobreescribir trabajos previos en caso de haberse editado la hora del evento
        WorkManager.getInstance(context).enqueueUniqueWork(
                workName,
                ExistingWorkPolicy.REPLACE,
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


        loadUserEvents();


        calendarView.setOnCalendarDayClickListener(new OnCalendarDayClickListener()
        {
            @Override
            public void onClick(@NonNull CalendarDay calendarDay)
            {

                Calendar calendar = calendarDay.getCalendar();

                LocalDate fechaSelec = calendarDay.getCalendar().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                if (allEvents.contains(fechaSelec.toString()))
                {
                    Intent intent = new Intent(calendarView.getContext(), EventOnCurrentDayActivity.class);
                    intent.putExtra("date", fechaSelec.toString());
                    startActivity(intent);
                }
                else
                {
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
    public void onResume()
    {
        super.onResume();

        loadUserEvents();
    }

    /**
     * Método para poder cargar los eventos en el calendario y en la lista
     */

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
                        for (DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            Event event = dataSnapshot.getValue(Event.class);

                            if (event != null && dataSnapshot.child("registeredUsers")
                                    .hasChild(user.getUid()))
                            {
                                try
                                {
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

                                    if (calendar.after(today))
                                    {
                                        nextEvents.add(event);
                                        calendarDay.setBackgroundDrawable(ResourcesCompat
                                                .getDrawable(getResources(),
                                                        R.drawable.calendar_day_current,
                                                        null));
                                        //calendarDay.setLabelColor(R.color.black);
                                    }

                                    else if (calendar.before(today))
                                    {
                                        pastEvents.add(event);
                                        calendarDay.setBackgroundDrawable(ResourcesCompat
                                                .getDrawable(getResources(),
                                                        R.drawable.calendar_day_past_event,
                                                        null));
                                        //calendarDay.setLabelColor(R.color.black);
                                    }
                                    else
                                    {
                                        nextEvents.add(event);
                                        calendarDay.setBackgroundDrawable(ResourcesCompat
                                                .getDrawable(getResources(),
                                                        R.drawable.calendar_day_current, null));
                                    }

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

                                    programarNotificacion(event, delaySegundos);
                                }

                                //Si no se consigue parsear bien la fecha se recoge una excepción
                                catch (ParseException e)
                                {
                                    throw new RuntimeException(e);
                                }
                            }
                        }

                        //Ordenamos los eventos por fecha y hora
                        //para ello ordenamos el arraylist

                        nextEvents.sort(new Comparator<Event>()
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

    @SuppressLint("StringFormatInvalid")
    public void programarNotificacion(Event event, long delay) {

        String mensaje = getResources().getString(R.string.eventoNotificacionMensaje, event.getPlace());

        if (delay > 0) {
            //notificamos al momento
            createWorkRequest(event.getId(),
                    getResources().getString(R.string.eventoNotificacionTituloNow, event.getName(), event.getHour()),
                    mensaje,
                    delay);

            if (delay > 3600) {
                //notificamos una hora antes
                createWorkRequest(event.getId() + "_3600",
                        getResources().getString(R.string.eventoNotificacionTitulo1h, event.getName()),
                        mensaje,
                        delay - 3600);

                if (delay > 86400) {
                    //notificamos un día antes
                    createWorkRequest(event.getId() + "_86400",
                            getResources().getString(R.string.eventoNotificacionTitulo24h, event.getName()),
                            mensaje,
                            delay - 86400);

                    if (delay > 604800) {
                        //notificamos una semana antes
                        createWorkRequest(event.getId() + "_604800",
                                getResources().getString(R.string.eventoNotificacionTitulo7d, event.getName()),
                                mensaje,
                                delay - 604800);
                    }
                }
            }
        }
    }


}