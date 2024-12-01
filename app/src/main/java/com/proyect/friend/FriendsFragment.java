package com.proyect.friend;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyect.R;
import com.proyect.user.User;

import java.util.ArrayList;

/**
 * Fragment friends creado para contener el registro de amistades de la app
 */

public class FriendsFragment extends Fragment
{

    /**
     * Variables param creadas por el propio fragment para su funcionamiento correcto
     * */
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    /**
     * Variables de clase para dar funcionalidad a todos los elementos
     * Un Recyclerview para mostrar los amigos
     * Un adaptador personalizado para dar funciones y mostrar los elementos de la lista
     * Un Arraylist de usuarios que son los amigos que tiene un usuario en la app
     * Una referencia a la base de datos para poder recoger los datos y cargar en el Arraylist
     * */

    private RecyclerView rvFriends;
    private FriendsAdapter friendsAdapter;
    private ArrayList<User> friends;
    private DatabaseReference databaseReference;

    /**
     * Dos botones:
     * uno para abrir las peticiones de amistad
     * otro para abrir la forma de aceptarlos
     * */

    FloatingActionButton fbFriendRequests;
    FloatingActionButton fbAddFriend;

    /**
     * Constructor sin argumentos necesario para el funcionamiento del fragment
     * */

    public FriendsFragment()
    {
        //Constructor vacío necesario
    }

    /**
     * Constructor con argumentos para poder crear instancias del fragment
     */

    public static FriendsFragment newInstance(String param1, String param2)
    {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Método onCreate usado para crear el fragment
     * */

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /**
     * Método necesario para el correcto funcionamiento del fragment
     * En él describimos el comportamiento que deben seguir los elementos
     * */

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //Inicializamos el Recyclerview y le asignamos un manejador de layout
        rvFriends = view.findViewById(R.id.rv_friends);

        rvFriends.setLayoutManager(new LinearLayoutManager(view.getContext()));

        //inicializamos el arraylist y lo pasamos a la inicialización del adaptador
        friends = new ArrayList<User>();

        //setteamos el LongClick como false, ya que no queremos un menú contextual en este fragment
        friendsAdapter = new FriendsAdapter(friends, false);

        //asignamos el adaptador Recyclerview
        rvFriends.setAdapter(friendsAdapter);

        //Cogemos la referencia de nuestra base de datos
        databaseReference = FirebaseDatabase.getInstance().getReference();

        //Buscamos en la referencia los amigos que tiene el usuario que está actualmente con
        //la sesión iniciada y le pasamos un escuchador para poder recoger los datos
        databaseReference.child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("friends")
                .addValueEventListener(new ValueEventListener()
                {
                    /**
                     * Método para poder tratar los datos de la referencia
                     * */
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        //vaciamos el Arraylist
                        friends.clear();

                        for(DataSnapshot dataSnapshot: snapshot.getChildren())
                        {
                            //En el bucle:
                            //recorremos los datos cogiendo cada unos de los hijos
                            //del snapshot, los cuales serán cada uno de los amigos
                            //y los añadimos al Arraylist
                            String friendUid = dataSnapshot.getKey();

                            databaseReference.child("users").child(friendUid)
                                    .addListenerForSingleValueEvent(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot)
                                        {
                                            User friend = dataSnapshot.getValue(User.class);

                                            //Si tiene una foto el usuario la recoge para mostrarla
                                            if(friend != null && snapshot.hasChild("imageUrl"))
                                            {
                                                friend.setImageUrl(snapshot.child("imageUrl")
                                                        .getValue(String.class));
                                            }
                                            friends.add(friend);

                                            //notificamos al adaptador que los datos han cambiado
                                            friendsAdapter.notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error)
                                        {
                                            Log.e("INFO"
                                                    ,"No se han podido cargar los amigos");
                                        }
                                    });
                        }
                    }

                    /**
                     * Método para gestionar errores en la base de datos
                     * */

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                       Log.e("INFO", "No se han podido cargar los amigos");
                    }
                });

        //Asignamos el botón de las peticiones
        fbFriendRequests = view.findViewById(R.id.fb_requests);

        //Le setteamos un escuchador de click para que cargue la actividad
        //FriendRequest para aceptar o rechazar las solicitudes de amistad
        fbFriendRequests.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(view.getContext(), FriendRequestActivity.class);

                startActivity(i);
            }
        });

        //Asignamos el botón de las peticiones
        fbAddFriend = view.findViewById(R.id.fb_addFriends);

        //Le setteamos un escuchador de click para que cargue la actividad
        //FriendSearcherActivity para que pueda mandar peticiones de amistad
        fbAddFriend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(view.getContext(), FriendSearcherActivity.class);

                startActivity(i);
            }
        });

    }

    /**
     * Método necesario para poder inflar el fragment
     * */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }


}