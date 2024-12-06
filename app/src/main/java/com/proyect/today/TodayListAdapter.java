package com.proyect.today;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyect.R;
import com.proyect.event.Event;
import com.proyect.event.EventViewerActivity;
import com.proyect.friend.FriendsAdapter;
import com.proyect.user.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class TodayListAdapter extends ArrayAdapter<Event>
{

    /**
     * Declaramos las variables de clase necesarias
     * */

    ArrayList<Event> events;
    private Context context;
    private String[] monthsArray;
    private String formattedDate;

    DatabaseReference reference;

    /**
     * Constructor con argumentos
     * */
    public TodayListAdapter(Context context, int resource, ArrayList<Event> events)
    {
        super(context, resource, events);
        this.context = context;
        this.events = events;
        this.monthsArray = context.getResources().getStringArray(R.array.material_calendar_months_array);
    }

    /**
     * Método para cargar el layout de cada elemento de la lista y darle funcionalidad
     * */

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        //Hacemos que se coja como holder, el que hemos personalizado
        TodayListViewHolder holder;
        Event event = getItem(position);
        if(convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_today_list_fragment, parent, false);
            holder = new TodayListViewHolder(convertView);
            convertView.setTag(holder);
        }
        else
        {
            holder = (TodayListViewHolder) convertView.getTag();
        }

        //Se asignan los valores a los elementos en pantalla
        holder.tvEventName.setText(event.getName());
        holder.tvEventPlace.setText(event.getPlace());
        holder.tvEventHour.setText(event.getHour());

        if (!event.getImage().isEmpty())
        {
            Glide.with(holder.itemView.getContext())
                    .load(event.getImage())
                    .fitCenter()
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.ic_event_list).into(holder.ivEventImage);
        }

        //Cogemos la refencia a la base de datos y declaramos el intent
        //al que se va cuando se clica en el evento
        reference = FirebaseDatabase.getInstance().getReference();

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

            //Pasamos los extras necesarios al intent
            intent.putExtra("event_id", event.getId());
            intent.putExtra("event_name", event.getName());
            intent.putExtra("event_data", formattedDate);
            intent.putExtra("event_date", event.getDate());
            intent.putExtra("event_place", event.getPlace());
            intent.putExtra("event_hour", event.getHour());

            if(!event.getImage().isEmpty())
            {
                intent.putExtra("event_image", event.getImage());
            }
        }

        //Si no se puede parsear se recoge una excepción
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                context.startActivity(intent);
            }
        });

        //Miramos cuales son los usuarios registrados y cogemos sus referencias y el número total
        Map<String, String> registeredUsers = event.getRegisteredUsers();

        holder.tvEventUsers.setText(""+registeredUsers.size());

        ArrayList <User> registeredUsersList = new ArrayList<User>();

        //Asignamos un adaptador para mostrar los amigos
        FriendsAdapter friendsAdapter = new FriendsAdapter(registeredUsersList,
                FirebaseAuth.getInstance().getUid(), false);

        holder.rvRegisteredUsers.setAdapter(friendsAdapter);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        //Dentro del bucle miramos si el usuario no es null y lo añadimos a la lista de usuarios
        //registrados dentro del evento
        for(String userId : registeredUsers.keySet())
        {
            reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    User user = snapshot.getValue(User.class);

                    if(user != null)
                    {
                        registeredUsersList.add(user);
                        friendsAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    Log.e("ERROR", "No se han podido cargar los participantes");
                }
            });

        }


        return convertView;
    }



}
