package com.proyect.event;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.proyect.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Clase EventListAdapter que extiende la clase adaptador de recyclerview
 * para poder mostrar los eventos en una lista, con un viewholder personalizado
 */

public class EventListAdapter extends RecyclerView.Adapter<EventListViewHolder> {
    /**
     * Creamos como variable de clase un arraylist para contener los eventos
     */
    ArrayList<Event> events;
    private Context context;
    private String[] monthsArray;
    private String formattedDate;

    /**
     * Constructor con argumentos
     */

    public EventListAdapter(Context context, ArrayList<Event> events)
    {
        this.events = events;
        this.context = context;
        this.monthsArray = context.getResources().getStringArray(R.array.material_calendar_months_array);
    }

    /**
     * Sobreescritura del método para poder inflar cada uno de los items con su layout personalizado
     */

    @NonNull
    @Override
    public EventListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_list_fragment, parent, false);

        return new EventListViewHolder(view);
    }

    /**
     * Sobreescritura del método para poder dar funcionalidad a los elementos
     * en pantalla de cada elemento
     */

    @Override
    public void onBindViewHolder(@NonNull EventListViewHolder holder, int position)
    {
        //Cogemos el evento correspondiente a la posición
        Event event = events.get(position);

        //Setteamos el texto texto a cada textview
        holder.tvEventName.setText(event.getName());
        holder.tvEventDate.setText(event.getDate());
        holder.tvEventPlace.setText(event.getPlace());

        //Si tiene una imagen asignada el evento se carga en el imageview
        //En caso contrario se carga un placeholder
        if (event.getImage()!= null && !event.getImage().isEmpty())
        {
            Glide.with(holder.itemView.getContext())
                    .load(event.getImage())
                    .fitCenter()
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.ic_event_list).into(holder.ivEventImage);
        }

        //(Yosef) Aquí hay que pasar event_data. Como has reutilizado este que ya estaba creado por mi
        //pero no has cambiado los datos que pasa, no se visualiza el texto que cambia de idioma
        //lo implemento para que la vista en eventviewer sea la misma que si se pincha desde next events

        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Event event = events.get(holder.getAbsoluteAdapterPosition());

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
                }

                catch (ParseException e)
                {
                    throw new RuntimeException(e);
                }

                Intent intent = new Intent(view.getContext(), EventViewerActivity.class);
                intent.putExtra("event_name", event.getName());
                intent.putExtra("event_id", event.getId());
                intent.putExtra("event_place", event.getPlace());
                intent.putExtra("event_date", event.getDate());
                intent.putExtra("event_hour", event.getHour());
                intent.putExtra("event_image", event.getImage());
                intent.putExtra("event_data", formattedDate);

                view.getContext().startActivity(intent);
            }
        });
    }

    /**
     * Método sobreescrito que devuelve la cantidad de eventos contenidos en el arraylist
     */

    @Override
    public int getItemCount()
    {
        return events.size();
    }


}
