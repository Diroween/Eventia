package com.proyect.today;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.proyect.R;
import com.proyect.event.Event;
import com.proyect.event.EventViewerActivity;
import com.proyect.user.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class TodayListAdapter extends ArrayAdapter<Event> {

    ArrayList<Event> events;
    private Context context;
    private String[] monthsArray;
    private String formattedDate;

    DatabaseReference reference;


    public TodayListAdapter(Context context, int resource, ArrayList<Event> events) {
        super(context, resource, events);
        this.context = context;
        this.events = events;
        this.monthsArray = context.getResources().getStringArray(R.array.material_calendar_months_array);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TodayListViewHolder holder;
        Event event = getItem(position);

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_today_list_fragment, parent, false);
            holder = new TodayListViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (TodayListViewHolder) convertView.getTag();
        }

        holder.tvEventName.setText(event.getName());
        holder.tvEventPlace.setText(event.getPlace());
        holder.tvEventHour.setText(event.getHour());

        if (event.getImage() != null)
        {
            Glide.with(holder.itemView.getContext())
                    .load(event.getImage())
                    .fitCenter()
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.ic_event_list).into(holder.ivEventImage);
        }

        reference = FirebaseDatabase.getInstance().getReference();

        Map<String, String> registeredUsers = event.getRegisteredUsers();

        holder.tvEventUsers.setText(""+registeredUsers.size());



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

            String format2 = String.format("%02d %s %d"
                    ,calendar.get(Calendar.DAY_OF_MONTH)
                    ,monthsArray[calendar.get(Calendar.MONTH)]
                    ,calendar.get(Calendar.YEAR));



            intent.putExtra("event_id", event.getId());
            intent.putExtra("event_name", event.getName());
            intent.putExtra("event_data", formattedDate);
            intent.putExtra("event_date", event.getDate());
            intent.putExtra("event_place", event.getPlace());
            intent.putExtra("event_hour", event.getHour());

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



        intent.putExtra("event_id", event.getId());
        intent.putExtra("event_name", event.getName());
        intent.putExtra("event_date", event.getDate());
        intent.putExtra("event_place", event.getPlace());
        intent.putExtra("event_hour", event.getHour());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(intent);
            }
        });

        return convertView;
    }

    private ArrayList<User> getRegisteredUsers() {
        return null;
    }



}
