package com.proyect;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Adaptador personalizado de Recyclerview para poder
 * mostrar futuros eventos en CalendarFragment
 * */

public class CalendarFragmentAdapter extends RecyclerView.Adapter<CalendarFragmentViewHolder>
{
    /**
     * Creamos tantos eventos de clase como vamos a necesitar
     * */

    private ArrayList <Event> eventList;
    private Context context;
    private String[] monthsArray;
    private String formattedDate;

    /**
     * Constructor con argumentos
     * @param context el contexto de la vista del fragment
     * @param eventList el arraylist de eventos que se pasan como argumento
     * */

    public CalendarFragmentAdapter(Context context, ArrayList<Event> eventList)
    {
        this.context = context; this.eventList = eventList;
        this.monthsArray = context.getResources().getStringArray(R.array.material_calendar_months_array);
    }

    /**
     * Método para inflar cada elemento
     * */

    @NonNull
    @Override
    public CalendarFragmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.item_calendar_fragment, parent, false);
        return new CalendarFragmentViewHolder(view);
    }

    /**
     * Método para dar funcionalidad a cada elemento
     * */

    @Override
    public void onBindViewHolder(@NonNull CalendarFragmentViewHolder holder, int position)
    {
        //Cogemos el evento perteneciente a la posición del arraylist
        Event event = eventList.get(position);

        //Ponemos el nombre del evento
        holder.tvEventName.setText(event.getName());

        Intent intent = new Intent(context, EventViewerActivity.class);

        try
        {
            //Creamos un formato para transformar la fecha guardad en String en fecha
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");

            //Creamos una fecha parseando el string guardado del evento
            Date date = inputDateFormat.parse(event.getDate());

            //Hacemos una instancia de Calendar
            Calendar calendar = Calendar.getInstance();

            //Le decimos que se settee como el día del evento
            calendar.setTime(date);

            //Formateamos la fecha dependiendo de si está el dispositivo en ingles o en español
            //más parecido a los carteles de eventos
            if(Locale.getDefault().getLanguage().equals(new Locale("es").getLanguage()))
            {
                formattedDate = String.format("%02d de %s de %d a las %s en %s"
                        ,calendar.get(Calendar.DAY_OF_MONTH)
                        ,monthsArray[calendar.get(Calendar.MONTH)]
                        ,calendar.get(Calendar.YEAR)
                        ,event.getHour()
                        ,event.getPlace());
            }

            else
            {
                formattedDate = String.format("%02d %s %d at %s in %s"
                        ,calendar.get(Calendar.DAY_OF_MONTH)
                        ,monthsArray[calendar.get(Calendar.MONTH)]
                        ,calendar.get(Calendar.YEAR)
                        ,event.getHour()
                        ,event.getPlace());
            }
            //le ponemos como texto al evento el string que hemos formateado
            holder.tvEventData.setText(formattedDate);

            intent.putExtra("event_id", event.getId());
            intent.putExtra("event_name", event.getName());
            intent.putExtra("event_data", formattedDate);

            if(event.getImage() != null)
            {
                intent.putExtra("event_image", event.getImage());
            }
        }

        //Si no se puede parsear se recoge una excepción
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }

        //Si el evento tiene una imagen se guarda
        //Sino se muestra un placeholder o se carga la que tenemos puesta por defecto
        //que es la misma que el placeholder
        if(event.getImage() != null)
        {
            Glide.with(context)
                    .load(event.getImage())
                    .placeholder(R.drawable.ic_event_list)
                    .into(holder.ivEventImage);
        }

        holder.itemView.setOnClickListener(l ->
        {
            context.startActivity(intent);
        });
    }

    /**
     * Método que devuelve el tamaño del array
     * */

    @Override
    public int getItemCount()
    {
        return eventList.size();
    }
}
