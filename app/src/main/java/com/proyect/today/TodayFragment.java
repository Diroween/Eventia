package com.proyect.today;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyect.R;
import com.proyect.event.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TodayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TodayFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    //Creamos un ListView donde irán las tareas del día, así como el arraylist
    //donde irán todas las tareas listadas
    ListView lvToday;
    TodayListAdapter adapter;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TextView tvDateToday;
    private SwipeRefreshLayout srlToday;

    private ArrayList<Event> todayEvents;

    private DatabaseReference databaseReference;

    public TodayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TodayFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TodayFragment newInstance(String param1, String param2) {
        TodayFragment fragment = new TodayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        databaseReference = FirebaseDatabase.getInstance().getReference();

        todayEvents = new ArrayList<Event>();

        //Inicializamos textviews para poner un saludo y otro para mostrar el día de hoy
        tvDateToday = view.findViewById(R.id.tv_date_today);

        //Inicializo el listview
        lvToday = (ListView) view.findViewById(R.id.lv_today);

        //Indicamos el swipe refresh sea la decalrada en el layout
        srlToday = view.findViewById(R.id.srl_today);

        //Le damos formato a la fecha que aparecerá
        SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMMM");

        //Cogemos la fecha de hoy
        Date today = new Date();

        //Mostramos el día de hoy en el textview formateado
        String strToday = getString(R.string.greetings_today) + " " + format.format(today);
        tvDateToday.setText(strToday);

        //Creamos un adaptador de String pasándole el contexto, un layout y el array today
        adapter = new TodayListAdapter(getContext(), R.layout.item_today_list_fragment, todayEvents);


        //Setteamos el listview con nuestro adaptador
        lvToday.setAdapter(adapter);


        loadUserEvents();

        //Ahora le setteamos un escuchador de eventos cuando se refresca la lista
        srlToday.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                //Le indicamos al adaptardor que ha habido cambios
                //y le forzamosa actualizarse
                adapter.notifyDataSetChanged();

                //Hacemos que la flecha de refrescar desaparezca
                srlToday.setRefreshing(false);
            }

        });


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_today, container, false);
    }


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
                        todayEvents.clear();

                        //Dentro del bucle:
                        //Creamos un evento para cada coincidencia de la base de datos
                        //Si el usuario está registrado en ese evento
                        //se pasa a tratar los datos del evento
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Event event = dataSnapshot.getValue(Event.class);

                            if (event != null && dataSnapshot.child("registeredUsers")
                                    .hasChild(user.getUid())) {
                                try {
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

                                    //Hacemos una nueva referencia que representa el día actual
                                    Calendar today = Calendar.getInstance();

                                    //Tomamos la fecha de hoy y la del evento y generamos un objeto LocalDate para comparar ambas
                                    LocalDate ldt = today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                                    ldt = LocalDate.of(ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth());

                                    LocalDate ldt2 = eventDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                                    ldt2 = LocalDate.of(ldt2.getYear(), ldt2.getMonthValue(), ldt2.getDayOfMonth());

                                    //Si coinciden, añadimos el evento al array
                                    if (ldt.equals(ldt2)) {
                                        todayEvents.add(event);
                                    }


                                }

                                //Si no se consigue parsear bien la fecha se recoge una excepción
                                catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }

                        //Ordenamos los eventos por fecha y hora
                        //para ello ordenamos el arraylist

                        todayEvents.sort(new Comparator<Event>() {
                            @Override
                            public int compare(Event e1, Event e2) {
                                //Damos un formato simple a la fecha y la hora y los comparamos
                                try {
                                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");

                                    SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

                                    Date date1 = sdfDate.parse(e1.getDate());

                                    Date date2 = sdfDate.parse(e2.getDate());

                                    //Si la fecha es la misma pasamos a ordenar por hora
                                    if (date1.equals(date2)) {
                                        //Cogemos las horas y se comparan
                                        Date time1 = sdfTime.parse(e1.getHour());
                                        Date time2 = sdfTime.parse(e2.getHour());

                                        return time1.compareTo(time2);
                                    }

                                    //Si las fechas no son iguales se ordena directamente por fecha
                                    else {
                                        return date1.compareTo(date2);
                                    }
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });

                        //Le decimos al adaptador que la lista ha cambiado sus datos
                        adapter.notifyDataSetChanged();


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), R.string.loadeventserror,
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }

}