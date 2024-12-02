package com.proyect.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * Clase comparadora para ordenar los eventos por fecha ascendente
 * */
public class EventDateComparator implements Comparator<Event> {
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

            //Si las horas no son iguales se ordena directamente por fecha
            else {
                return date1.compareTo(date2);
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
