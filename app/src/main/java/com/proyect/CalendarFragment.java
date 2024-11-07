package com.proyect;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.applandeo.materialcalendarview.CalendarDay;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener;


import java.util.ArrayList;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CalendarView calendarView;


    public CalendarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CalendarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalendarFragment newInstance(String param1, String param2) {
        CalendarFragment fragment = new CalendarFragment();
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

    /**
    * *-Yosef-* Comento esta parte, aunque quedaría hacer la lista inferior
     * donde aparecerían los próximos eventos
    * */

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        //Inicializamos el calendario
        calendarView = (CalendarView) view.findViewById(R.id.cv_calendar);

        //Inicializamos el arraylist de calendardays con el que se pondrán los iconos
        //*-Yosef-* más adelante se tienen que coger los días, cargarlos como calendar days
        //y esos calendar days ponerlos en el array para que muestre iconitos los días de evento
        ArrayList<CalendarDay> events = new ArrayList<>();

        //Hacemos una instancia de la clase Calendar de Java
        Calendar calendar = Calendar.getInstance();

        //Añadimos un día de prueba para que salga un iconito
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        //hacemos un calendar day con el día que hemos creado
        CalendarDay calendarDay = new CalendarDay(calendar);

        //le ponemos un icono
        calendarDay.setImageResource(R.drawable.ic_event_list);

        //lo añadimos al arraylist
        events.add(calendarDay);

        //le pasamos a la vista el array de días de evento
        calendarView.setCalendarDays(events);

        //Le setteamos un manejador de eventos para cuando pulsamos en alguna fecha
        calendarView.setOnCalendarDayClickListener(new OnCalendarDayClickListener()
        {
            @Override
            public void onClick(@NonNull CalendarDay calendarDay)
            {
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }


}