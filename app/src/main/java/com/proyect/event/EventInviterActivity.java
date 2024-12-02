package com.proyect.event;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyect.R;
import com.proyect.friend.FriendInviterAdapter;
import com.proyect.user.User;

import java.util.ArrayList;

/**
 * Clase para poder invitar amigos a los eventos creados
 * */

public class EventInviterActivity extends AppCompatActivity
{
    /**
     * Creamos tantas variables de clase como necesitamos:
     * Un adaptador para la lista de amigos
     * Una lista recyclerview
     * Un arraylist de usuarios
     * Una referencia a la base de datos
     * El usuario que se selecciona de la lista
     * El botón para invitar a los amigos
     * */

    FriendInviterAdapter friendsAdapter;
    RecyclerView rvFriends;
    ArrayList<User> friends;
    DatabaseReference databaseReference;
    User selectedFriend;
    Button btnInvite;

    String eventId;
    String eventName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Métodos necesarios para mostrar correctamente los elementos por pantalla
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_inviter);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Recogemos el nombre del evento y su id
        Intent intent = getIntent();

        eventId = intent.getStringExtra("event_id");
        eventName = intent.getStringExtra("event_name");

        //Inicializamos los elementos en pantalla y el resto de variables
        rvFriends = findViewById(R.id.rv_friends);
        btnInvite = findViewById(R.id.btn_invite_friend);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        friends = new ArrayList<User>();

        rvFriends.setLayoutManager(new LinearLayoutManager(this));

        //Indicamos el usuario seleccionado en el escuchador
        friendsAdapter = new FriendInviterAdapter(friends, friend->
        {
            selectedFriend = friend;
        });

        //Asignamos el adaptardor a la lista
        rvFriends.setAdapter(friendsAdapter);

        //Damos funcionalidad al botón de invitar
        btnInvite.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                inviteFriend();
            }
        });

        loadFriends();
    }

    /**
     * Método para cargar los usuarios en la lista
     * */

    private void loadFriends()
    {
        //Cogemos la referencia de los amigos del usuario activo
        databaseReference.child("users").child(FirebaseAuth.getInstance().getUid())
                .child("friends").addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        //Vaciamos el arraylist
                        friends.clear();

                        //En el bucle:
                        //Cogemos cada amigo
                        //si existe lo buscamos en la lista de usuario y cargamos sus datos
                        //se notifica que los datos del adaptador han cambiado
                        //Si no se pueden cargar se guarda un mensaje en log
                        for (DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            //Cogemos cada amigos
                            String friendId = dataSnapshot.getKey();

                            databaseReference.child("users").child(friendId)
                                    .addListenerForSingleValueEvent(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot)
                                        {
                                            User friend = snapshot.getValue(User.class);

                                            if (friend != null && friends.add(friend))
                                            {
                                                friendsAdapter.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error)
                                        {
                                            Log.e("INFO"
                                                    , "No se han podido cargar los datos");
                                        }
                                    });
                        }
                    }

                    /**
                     * Si no se pueden cargar se guarda un mensaje en log
                     * */

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        Log.e("INFO"
                                , "No se han podido cargar los datos");
                    }
                });
    }

    /**
     * Método para poder cargar a los amigos en la lista
     * */

    private void inviteFriend()
    {
        //Cogemos la referencia de donde está
        //el usuario en la base de datos y sus peticiones de evento
        //y donde el evento y sus usuarios registrados
        DatabaseReference userEventRequestsRef = databaseReference.child("users")
                .child(selectedFriend.getId()).child("eventsRequests");

        DatabaseReference eventRegisteredUsersRef = databaseReference
                .child("events").child(eventId).child("registeredUsers");

        //Primero, verificamos si el usuario ya ha sido invitado al evento
        userEventRequestsRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                //Si ya ha sido invitado se muestra un mensaje
                if (snapshot.hasChild(eventId))
                {
                    Toast.makeText(EventInviterActivity.this
                            , R.string.alreadyinvited
                            , Toast.LENGTH_SHORT).show();
                }

                //sino verificamos si el usuario ya está registrado en el evento
                else
                {
                    eventRegisteredUsersRef.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot)
                        {
                            //Si está registrado se muestra un mensaje
                            if (snapshot.hasChild(selectedFriend.getId()))
                            {
                                Toast.makeText(EventInviterActivity.this
                                        , R.string.alreadyregistered
                                        , Toast.LENGTH_SHORT).show();
                            }

                            //Si no añadimos una petición del evento
                            else
                            {
                                userEventRequestsRef.child(eventId)
                                        .setValue("pending").addOnCompleteListener(task ->
                                        {
                                    if (task.isSuccessful())
                                    {
                                        //Si se completa el proceso se manda un mensaje
                                        Toast.makeText(EventInviterActivity.this
                                                , getResources()
                                                        .getText(R.string.eventrequestsent) + " "
                                                        + eventName, Toast.LENGTH_SHORT)
                                                .show();
                                    }

                                    //sino se registra un error
                                    else
                                    {
                                        Log.e("INFO"
                                                , "No se puedo enviar la petición");
                                    }
                                });
                            }
                        }

                        /**
                         * Si no se puede registrar el evento guardamos el error
                         * */
                        @Override
                        public void onCancelled(@NonNull DatabaseError error)
                        {
                            Log.e("INFO"
                                    , "Error al cargar el evento");
                        }
                    });
                }
            }

            /**
             * Si no se puede registrar el evento guardamos el error
             * */
            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Log.e("INFO"
                        , "Error al cargar el evento");
            }
        });
    }
}