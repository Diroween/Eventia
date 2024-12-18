package com.proyect.friend;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyect.R;
import com.proyect.user.User;
import com.proyect.user.UserSettings;

import java.util.ArrayList;

/**
 * Actividad que sirve para aceptar y rechazar peticiones de amistad
 * */

public class FriendRequestActivity extends AppCompatActivity implements FriendRequestAdapter.OnFriendRequestActionListener
{
    /**
     * Creamos las variables de clase necesarias
     * //
     * Un imageview para la imagen del usuario
     * un textview para mostrar el nombre del usuario
     * //
     * El reciclerview donde se cargarán las peticiones
     * El adaptador del recyclerview
     * El arraylist que le pasamos al adaptador
     * El databasereference para poder grabar y eliminar los datos en la bdd
     * */

    ImageView ivUserImage;
    TextView tvDisplayname;
    TextView tvPendingRequests;

    RecyclerView rvFriends;
    FriendRequestAdapter friendRequestAdapter;
    ArrayList<FriendRequest> friendRequests;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Clases necesarias para el funcionamiento correcto de la activity
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_friend_request);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Se fuerza a la aplicación a mostrarse en vertical
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Creamos una toolbar para el main de la aplicación
        Toolbar toolbar = (Toolbar)findViewById(R.id.tb_friend_req);

        //Le indicamos a la activity que use la toolbar que hemos creado
        setSupportActionBar(toolbar);

        //Asignamos textview al elemento gráfico
        tvDisplayname = findViewById(R.id.tv_displayname);
        tvPendingRequests = findViewById(R.id.tv_no_pending_requests);


        //le decimos que su texto debe ser el nombre de usuario de la app
        tvDisplayname.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

        //Asignamos el contenedor de imagen para el usuario
        ivUserImage = findViewById(R.id.iv_user_image);

        //Cogemos la imagen del usuario, en caso de que la tenga
        Uri imageUri = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();

        //Si la tiene
        if(imageUri != null)
        {
            //Cargamos la imagen
            Glide.with(this).load(imageUri.toString())
                    .placeholder(R.drawable.baseline_tag_faces_128)
                    .transform(new CircleCrop())
                    .into(ivUserImage);
        }

        //Si no la tiene
        else
        {
            //Cargamos directamente el placeholder
            Glide.with(this).load(R.drawable.baseline_tag_faces_128)
                    .into(ivUserImage);
        }

        //Asignamos un escuchador para el click
        //El escuchador nos abre el portal de configuración del usuario
        ivUserImage.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, UserSettings.class);
            startActivity(intent);
        });

        //Le asignamos la misma función al textview para que pulse donde pulse
        //el usuario le abra los settings
        tvDisplayname.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, UserSettings.class);
            startActivity(intent);
        });

        //inicializamos el recyclerview y le asignamos un manejador
        rvFriends = findViewById(R.id.rv_friends);
        rvFriends.setLayoutManager(new LinearLayoutManager(this));

        //Inicializamos el arraylist de solicitudes
        friendRequests = new ArrayList<FriendRequest>();

        //Inicializamos el adaptador con el arraylist y el escuchador de eventos
        friendRequestAdapter = new FriendRequestAdapter(friendRequests, this);

        //setteamos el adaptador al recyclerview
        rvFriends.setAdapter(friendRequestAdapter);

        //Inicializamos el databasereference como una referencia de la instancia de una base de datos
        databaseReference = FirebaseDatabase.getInstance().getReference();

        //Le decimos a la referencia que mire en lista de peticiones del usuario
        //y que le ponga un escuchador para los cambios en la base
        //así se podrá cargar la lista de solicitudes
        databaseReference.child("users").child(FirebaseAuth.getInstance().getUid())
                .child("friend_requests")
                .addValueEventListener(new ValueEventListener()
                {
                    /**
                     * Método para gestionar los cambios en la base de datos
                     * @param snapshot datos recogidos de una localización específica de la bdd
                     * */

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        //vaciamos el arraylist
                        friendRequests.clear();

                        //coge todas las apariciones que encuentra en los datos
                        //decimos que guarda cada aparición como una petición
                        //añadimos cada solicitud al arraylist
                        for (DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            FriendRequest request = dataSnapshot.getValue(FriendRequest.class);
                            friendRequests.add(request);
                        }

                        //si no hay solicitudes mostramos el texto de advertencia
                        if (friendRequests.isEmpty()) {
                            tvPendingRequests.setVisibility(View.VISIBLE);
                        } else {
                            tvPendingRequests.setVisibility(View.GONE);
                        }

                        //Le notificamos al adaptador que los datos han cambiado
                        friendRequestAdapter.notifyDataSetChanged();
                    }

                    /**
                     * Método para notificar errores en la base de datos
                     * */

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        //Un mensaje de error
                        /*Toast.makeText(getApplicationContext(), R.string.nodataload,
                                Toast.LENGTH_SHORT).show();*/
                    }
                });
    }

    /**
     * Sobreescritura del método del escuchador para aceptar las peticiones al pulsar el botón
     * */

    @Override
    public void onAccept(String userId)
    {
        //Cuando se pulsa se ejecuta el método acceptFriendRequest pasando como argumentos:
        //el id del usuario actual
        //El id del usuario al que se le acepta la petición
        acceptFriendRequest(FirebaseAuth.getInstance().getUid(), userId);
    }

    @Override
    public void onReject(String userId)
    {
        //Cuando se pulsa se ejecuta el método rejectFriendRequest pasando como argumentos:
        //el id del usuario actual
        //El id del usuario al que se le rechaza la petición
        rejectFriendRequest(userId);
    }

    /**
     * Método para aceptar las peticiones de amistad
     *
     * @param currentUserId El usuario que tiene una petición de amistad
     * @param targetUserId El usuario que mandó la solicitud de amistad
     *
     * */

    private void acceptFriendRequest(String currentUserId, String targetUserId)
    {
        //cogemos la referencia del usuario que mandó la petición de amistad
        //dentro de las peticiones de amistad del usuario que la recibió
        DatabaseReference database = databaseReference.child("users")
                .child(FirebaseAuth.getInstance().getUid()).child("friend_requests")
                .child(targetUserId);

        //Le añadimos un escuchador para poder hacer cambios en la bdd
        database.addListenerForSingleValueEvent(new ValueEventListener()
        {
            /**
             * Método para gestionar los cambios en la base de datos
             * @param snapshot datos recogidos de una localización específica de la bdd
             * */

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                //Si los datos existen añadimos a los dos usuarios como amigos en la bdd
                //y borramos la petición de amistad de la bdd
                if (snapshot.exists())
                {
                    //cogemos el nombre de usuario del usuario con el id que mandó la petición
                    String name = snapshot.child("name").getValue(String.class);

                    //creamos un nuevo usuario con el id y el nombre
                    User friend = new User(targetUserId, name);

                    //Añadimos en la sección de amigos de los dos usuarios al otro usuario
                    //en este caso con el friend que hemos creado
                    databaseReference.child("users").child(currentUserId)
                            .child("friends").child(targetUserId)
                            .setValue(friend);

                    //En este caso creamos un nuevo usuario cogiendo los datos del usuario receptor
                    databaseReference.child("users").child(targetUserId)
                            .child("friends").child(currentUserId)
                            .setValue(new User(currentUserId,
                                    FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));

                    //eliminamos la petición de la lista de amistad
                    database.removeValue();

                    //Recoremos las peticiones
                    //si la petición coincide con la enviada por el usuario se elimina del arraylist
                    //notificamos que un elemento se eliminó y salimos del bucle
                    for (int i = 0; i < friendRequests.size(); i++)
                    {
                        if (friendRequests.get(i).getFrom().equals(targetUserId))
                        {
                            friendRequests.remove(i);
                            friendRequestAdapter.notifyItemRemoved(i);
                            break;
                        }
                    }

                    //mandamos un mensaje al usuario de que se aceptó la petición
                    Toast.makeText(getApplicationContext(), R.string.friendaccepted,
                            Toast.LENGTH_SHORT).show();
                }
            }

            /**
             * Método para notificar errores en la base de datos
             * */

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                //Un mensaje de error
                Toast.makeText(getApplicationContext(), R.string.accepterror,
                        Toast.LENGTH_SHORT).show();
            }

        });
    }

    /**
     * Método para rechazar las peticiones de amistad
     *
     * @param targetUserId El usuario que mandó la solicitud de amistad
     * */

    private void rejectFriendRequest(String targetUserId)
    {
        //cogemos la referencia del usuario que mandó la petición de amistad
        //dentro de las peticiones de amistad del usuario que la recibió
        DatabaseReference database = databaseReference.child("users")
                .child(FirebaseAuth.getInstance().getUid()).child("friend_requests")
                .child(targetUserId);

        //Le añadimos un escuchador para poder hacer cambios en la bdd
        database.addListenerForSingleValueEvent(new ValueEventListener()
        {
            /**
             * Método para gestionar los cambios en la base de datos
             * @param snapshot datos recogidos de una localización específica de la bdd
             * */

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                //Si los datos existen borramos la petición de amistad de la bdd
                if(snapshot.exists())
                {
                    //eliminamos la petición de la lista de amistad
                    database.removeValue();

                    //Recoremos las peticiones
                    //si la petición coincide con la enviada por el usuario se elimina del arraylist
                    //notificamos que un elemento se eliminó y salimos del bucle
                    for (int i = 0; i < friendRequests.size(); i++)
                    {
                        if (friendRequests.get(i).getFrom().equals(targetUserId))
                        {
                            friendRequests.remove(i);
                            friendRequestAdapter.notifyItemRemoved(i);
                            break;
                        }
                    }

                    //mandamos un mensaje de feedback para el usuario
                    Toast.makeText(getApplicationContext(), R.string.requestrejected,
                            Toast.LENGTH_SHORT).show();
                }
            }

            /**
             * Método para notificar errores en la base de datos
             * */

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                //un mensaje de error
                Toast.makeText(getApplicationContext(), R.string.rejecterror
                        , Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Realizamos un override de los métodos necesarios para crear el menú
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //le decimos al menú que la crearse y use el layout de la carpeta menu
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

}