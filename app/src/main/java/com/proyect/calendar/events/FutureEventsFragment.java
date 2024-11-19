package com.proyect.calendar.events;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyect.R;
import com.proyect.calendar.CalendarFragmentAdapter;
import com.proyect.event.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PastEventsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FutureEventsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView myRecyclerView;

    private CalendarFragmentAdapter calendarFragmentAdapter;

    private DatabaseReference databaseReference;

    private ArrayList<Event> pastEvents;
    private ArrayList<Event> currentEvents;
    private ArrayList<Event> nextEvents;
    private ArrayList<Event> periodicEvents;

    public FutureEventsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PastEventsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PastEventsFragment newInstance(String param1, String param2) {
        PastEventsFragment fragment = new PastEventsFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_past_event, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rvEventLog);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        databaseReference = FirebaseDatabase.getInstance().getReference();

        nextEvents = new ArrayList<Event>();
        pastEvents = new ArrayList<Event>();
        currentEvents = new ArrayList<Event>();
        periodicEvents = new ArrayList<Event>();

        loadUserEvents();

        calendarFragmentAdapter = new CalendarFragmentAdapter(view.getContext(), nextEvents);

        recyclerView.setAdapter(calendarFragmentAdapter);



        // Inflate the layout for this fragment
        return view;
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
                        pastEvents.clear();
                        currentEvents.clear();
                        nextEvents.clear();
                        periodicEvents.clear();

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

                                    //Si es anterior, se carga en eventos pasados
                                    if (calendar.before(today)) {
                                        pastEvents.add(event);
                                    }

                                    if (today.equals(calendar)) {
                                        currentEvents.add(event);
                                    }
                                }

                                //Si no se consigue parsear bien la fecha se recoge una excepción
                                catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }

                        //Le decimos al adaptador que la lista ha cambiado sus datos
                        calendarFragmentAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), R.string.loadeventserror,
                                Toast.LENGTH_SHORT).show();
                    }
                });


    }
}