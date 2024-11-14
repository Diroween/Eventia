package com.proyect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Actividad para poder ver los eventos
 * */

public class EventViewerActivity extends AppCompatActivity
{
    /**
     * Variables de clase, tantas como elementos tengamos que tratar:
     * Una lista de las personas registradas en la app
     * Un arraylist de usuarios registrados
     * Un adaptador personalizado
     * Una referencia a la base de datos
     * TextViews e ImageView para mostrar los datos de los eventos
     * Un botón flotante para poder añadir a tus amigos al evento
     * */

    RecyclerView rv_users;
    ArrayList<User> registeredUsers;
    FriendsAdapter adapter;
    DatabaseReference reference;

    TextView tvEventName;
    TextView tvEventData;
    ImageView ivEventImage;

    FloatingActionButton fbAddFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Métodos necesarios para poder mostrar elementos en pantalla correctamente
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_viewer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Inicializamos las variables
        tvEventName = findViewById(R.id.tv_event_name);
        tvEventData = findViewById(R.id.tv_event_data);
        ivEventImage = findViewById(R.id.iv_event_image);

        fbAddFriends = findViewById(R.id.fb_addFriends);

        //Recogemos los datos del evento que pasamos desde el adaptador de calendarFragment
        Intent intent = getIntent();

        String eventId = intent.getStringExtra("event_id");
        String eventName = intent.getStringExtra("event_name");
        String eventData = intent.getStringExtra("event_data");
        String eventImage = intent.getStringExtra("event_image");

        //Asignamos lso datos recogidos
        tvEventName.setText(eventName);
        tvEventData.setText(eventData);

        Glide.with(this)
                .load(eventImage)
                .placeholder(R.drawable.ic_event_list)
                .transform(new CircleCrop())
                .into(ivEventImage);

        rv_users = findViewById(R.id.rv_registered_users);

        rv_users.setLayoutManager(new LinearLayoutManager(this));

        registeredUsers = new ArrayList<User>();

        adapter = new FriendsAdapter(registeredUsers);

        rv_users.setAdapter(adapter);

        //Cogemos una referncia a base de datos
        reference = FirebaseDatabase.getInstance().getReference();

        //Damos función al botón de añdir amigos al evento
        fbAddFriends.setOnClickListener(l ->
        {
            Intent intentInvite = new Intent(this, EventInviterActivity.class);

            intentInvite.putExtra("event_id", eventId);
            intentInvite.putExtra("event_name", eventName);

            startActivity(intentInvite);
        });

        //Cargamos los usuarios registrado en el evento
        loadRegisteredUsers(eventId);
    }

    /**
     * Método para poder cargar los usuarios registrado en la lista
     * */

    private void loadRegisteredUsers(String eventId)
    {
        //Cogemos una referencia a la base de datos
        reference.child("events").child(eventId).child("registeredUsers")
                .addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        //Vaciamos el arraylist
                        registeredUsers.clear();

                        //En el bucle:
                        //Cogemos cada usuario registrado
                        //Lo buscamos en la base de datos de usuarios
                        //Si existe se añade el usario
                        //y se notifican cambios de datos para el adaptador
                        for(DataSnapshot dataSnapshot: snapshot.getChildren())
                        {
                            String userId = dataSnapshot.getKey();

                            reference.child("users").child(userId)
                                    .addListenerForSingleValueEvent(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot)
                                        {
                                            User user = snapshot.getValue(User.class);

                                            if(user != null)
                                            {
                                                registeredUsers.add(user);
                                                adapter.notifyDataSetChanged();
                                            }
                                        }

                                        /**
                                         * Si no se puede se loggea el mensaje
                                         * */

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error)
                                        {
                                            Log.println(Log.INFO, "Info", error.getMessage());
                                        }
                                    });
                        }
                    }

                    /**
                     * Si no se puede se loggea el mensaje
                     * */

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        Log.println(Log.INFO, "Info", error.getMessage());
                    }
                });
    }
}