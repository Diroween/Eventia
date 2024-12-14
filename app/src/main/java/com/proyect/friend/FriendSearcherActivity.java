package com.proyect.friend;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyect.R;
import com.proyect.user.User;
import com.proyect.user.UserSettings;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Actividad para buscar amigos y que salgan representado en un lista
 * Se pueden buscar amigos con un mismo nickname pero con distinto id
 */

public class FriendSearcherActivity extends AppCompatActivity {
    /**
     * Creamos todas las variables necesarias
     * //
     * Un imageview para la imagen del usuario
     * un textview para mostrar el nombre del usuario
     * //
     * el edittext para introducir el nombre, los botones buscar y enviar petición
     * el recyclerview, el adaptador, el arraylist, una referencia para la bdd y un usuario
     */

    ImageView ivUserImage;
    TextView tvDisplayname;

    private EditText etUsername;
    private Button btnSearch;
    private Button btnSendRequest;
    private RecyclerView rvUsers;
    private FriendSearcherAdapter searcherAdapter;
    private ArrayList<User> users;
    private DatabaseReference databaseReference;
    private User selectedUser;

    /**
     * Método necesario para el funcionamiento de la actividad
     */

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //métodos necesarios para crear los elementos y que se vean correctamente en pantalla
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_friend_searcher);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Se fuerza a la aplicación a mostrarse en vertical
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Creamos una toolbar para el main de la aplicación
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_friend_search);

        //Le indicamos a la activity que use la toolbar que hemos creado
        setSupportActionBar(toolbar);

        //Asignamos textview al elemento gráfico
        tvDisplayname = findViewById(R.id.tv_displayname);

        //le decimos que su texto debe ser el nombre de usuario de la app
        tvDisplayname.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

        //Asignamos el contenedor de imagen para el usuario
        ivUserImage = findViewById(R.id.iv_user_image);

        //Cogemos la imagen del usuario, en caso de que la tenga
        Uri imageUri = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();

        //Si la tiene
        if (imageUri != null)
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

        //Inicializamos las variables con los elementos del xml
        etUsername = findViewById(R.id.et_username);
        btnSearch = findViewById(R.id.btn_search);
        btnSendRequest = findViewById(R.id.btn_sendRequest);
        rvUsers = findViewById(R.id.rv_users);

        //le asignamos un manejador de layout
        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        //incializamos el arraylist
        users = new ArrayList<User>();

        //incializamos el adaptador y le ponemos el escuchador
        //le decimos que usuario seleccionado sea user
        //que al estar ya seleccionado un usuario aparezca el botón de amigo
        //nos sale un mensaje en el que nos indica cual es el usuario seleccionado
        searcherAdapter = new FriendSearcherAdapter(users, user ->
        {
            selectedUser = user;
            btnSendRequest.setVisibility(View.VISIBLE);
        });

        //setteamos el adaptador al recyclerview
        rvUsers.setAdapter(searcherAdapter);

        //cogemos la referencia de la base de datos
        databaseReference = FirebaseDatabase.getInstance().getReference();

        //le ponemos el escuchador de eventos al botón
        //y cuando se pulse se ejecutará el método seachUsers
        //pasando como argumento el username que hemos pasado
        // *-Yosef-* es parecido a un setOnClickListener, pero está vez lo he hecho en el adaptador
        // y lo sobreescribo aquí, no es más extraño que esto.
        btnSearch.setOnClickListener(v ->
        {
            if (!etUsername.getText().toString().isEmpty())
            {
                searchUsers(etUsername.getText().toString());
            } else {
                Toast.makeText(getApplicationContext(), R.string.usernonselected,
                        Toast.LENGTH_SHORT).show();
            }
        });

        //Ahora al botón de sendrequest le ponemos en su escuchador que si el usuario seleccionado
        //no es null, tiene que enviar la solicitud, sino manda un mensaje de feedback
        btnSendRequest.setOnClickListener(v ->
        {
            if (selectedUser != null)
            {
                sendFriendRequest(FirebaseAuth.getInstance().getUid(), selectedUser.getId());
            }
            else
            {
                Toast.makeText(getApplicationContext(), R.string.usernonselected,
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Método para buscar a un usuario
     *
     * @param username nombre de usuario del usuario que queremos encontrar
     */

    private void searchUsers(String username)
    {
        //cogemos la referencia de los usuarios ordenamos por nombre que sean igual al que
        // se ha puesto en el edittext y le ponemos un escuchador para hacer cambios
        databaseReference.child("users").orderByChild("name")
                .addListenerForSingleValueEvent(new ValueEventListener()
                {
                    /**
                     * Método para modificar o recoger datos en la bdd
                     * @param snapshot datos recogidos de la bdd
                     * */
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        //si los datos existen los incluimos para mostrarlos
                        if (snapshot.exists())
                        {
                            //vaciamos el arraylist
                            users.clear();
                          
                            if(username.length() >= 3)
                            {
                                //por cada hijo que haya encontrado le decimos que coja los datos
                                //de cada usuario
                                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                                {
                                    User user = dataSnapshot.getValue(User.class);

                                    //Normalizamos los nombres introducidos quitándoles
                                    //los acentos
                                    String introducedName = Normalizer.normalize(username,
                                            Normalizer.Form.NFD)
                                            .replaceAll("\\p{M}", "");

                                    String databaseName = Normalizer.normalize(user.getName(),
                                            Normalizer.Form.NFD)
                                            .replaceAll("\\p{M}", "");

                                    //En el regex le ponemos que el nombre por el que tiene
                                    //que buscar es el principio del nombre introducido
                                    String regex = "^" + introducedName + ".*";

                                    //le pasamos al patrón el regex con una bandera indicando
                                    //que no tenga en cuenta minusculas y mayúsculas
                                    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

                                    //Al matcher le pasamos el nombre nomalizado de la base de datos
                                    Matcher matcher = pattern.matcher(databaseName);

                                    //Si hay alguna coincidencia
                                    if (matcher.find())
                                    {
                                        //si el usuario no es null nos lo añade al arraylist
                                        //y si tiene una foto el usuario la recoge para mostrarla
                                        if (user != null && dataSnapshot.hasChild("imageUrl"))
                                        {
                                            user.setImageUrl(dataSnapshot
                                                    .child("imageUrl").getValue(String.class));

                                            users.add(user);
                                        }
                                        else if (user != null)
                                        {
                                            users.add(user);
                                        }

                                        //si no es así nos ponemos un log
                                        else
                                        {
                                            Log.e("Error",
                                                    "No se han encontrado usuarios");
                                        }

                                    }
                                }

                                //Si no se ha encontrado a nadie se lanza un Toast informativo
                                if(users.isEmpty())
                                {
                                    Toast.makeText(FriendSearcherActivity.this,
                                            R.string.usernotfound, Toast.LENGTH_SHORT).show();
                                }
                            }

                            //Si el nombre no es lo suficientemente largo como iniciar la búsqueda
                            //se indica con un Toast informativo
                            else
                            {
                                Toast.makeText(FriendSearcherActivity.this,
                                        R.string.usernameshort,
                                        Toast.LENGTH_SHORT).show();
                            }

                        }

                        //Si no existen resultados se muestra un toast
                        else
                        {
                            Toast.makeText(FriendSearcherActivity.this,
                                    R.string.usernotfound,
                                    Toast.LENGTH_SHORT).show();
                        }
                        searcherAdapter.notifyDataSetChanged();
                    }

                    /**
                     * Si ha habido algún error de base de datos lo muestramos en un toast
                     * */
                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        Log.e("Error", "No se han podido buscar usuarios");
                    }
                });
    }

    /**
     * Método para enviar una solicitud de amistad
     *
     * @param currentUserId el usuario que está actualmente conectado
     * @param targetUserId  el usuario que va a enviar la petición de amistad
     */
    private void sendFriendRequest(String currentUserId, String targetUserId)
    {
        //Si la persona a la que se va a enviar una solicitud no es el propio usuario
        if (!currentUserId.equals(targetUserId))
        {
            //cogemos la referencia de la base de datos para ver si ya son amigos
            DatabaseReference database = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(currentUserId).child("friends")
                    .child(targetUserId);

            //le ponemos un escuchador de eventos para poder recoger datos y hacer cambios en la bdd
            database.addListenerForSingleValueEvent(new ValueEventListener()
            {
                /**
                 * Método para modificar o recoger datos en la bdd
                 * @param snapshot datos recogidos de la bdd
                 * */
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    //si los datos ya existen significa que ya sois amigos
                    //se le muestra un mensaje al usuario
                    if (snapshot.exists())
                    {
                        Toast.makeText(getApplicationContext(), R.string.alreadyfriends,
                                Toast.LENGTH_SHORT).show();
                    }

                    //Si no se recoge una nueva referencia de las peticiones de amistad
                    else
                    {
                        DatabaseReference dataref = FirebaseDatabase.getInstance().getReference()
                                .child("users").child(targetUserId)
                                .child("friend_requests").child(currentUserId);
                        //le ponemos un escuchador de eventos para poder recoger datos
                        //y hacer cambios en la bdd
                        dataref.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            /**
                             * Método para modificar o recoger datos en la bdd
                             * @param snapshot datos recogidos de la bdd
                             * */

                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {
                                //Si los datos existen, significa que ya se
                                //ha mandado una petición de amistad
                                if (snapshot.exists())
                                {
                                    //Cogemos la petición
                                    FriendRequest existingRequest =
                                            snapshot.getValue(FriendRequest.class);

                                    //Si el estado de la petición es pending y no es null
                                    //mandamos un mensaje diciendo que ya se
                                    //ha enviado una solicitud
                                    if (existingRequest != null && existingRequest.getStatus()
                                            .equals("pending"))
                                    {
                                        Toast.makeText(getApplicationContext(),
                                                R.string.alreadysent,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    //sino creamos una nueva petición de amistad
                                    else
                                    {
                                        FriendRequest request = new FriendRequest(currentUserId,
                                                FirebaseAuth.getInstance().getCurrentUser()
                                                        .getDisplayName(), "pending");

                                        //modificamos la base de datos con el nuevo valor
                                        dataref.setValue(request).addOnCompleteListener(
                                                new OnCompleteListener<Void>()
                                                {
                                                    /**
                                                     * Método para comprobar que se haya llevado a cabo
                                                     * las acciones de intorducir la petición
                                                     * */
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        //si la tarea se completa satisfactoriamente
                                                        //mandamos un mensaje de feedback indicandolo
                                                        if (task.isSuccessful())
                                                        {
                                                            Toast.makeText(getApplicationContext(),
                                                                    R.string.requestsent,
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                        //si no se manda un mensaje diciendo que ha
                                                        //habido un error
                                                        else
                                                        {
                                                            Toast.makeText(getApplicationContext(),
                                                                    R.string.requestsenderror,
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                }

                                //si no existe la petición, se manda la solicitud
                                else
                                {
                                    //creamos la petición los datos del usuario
                                    FriendRequest request = new FriendRequest(currentUserId,
                                            FirebaseAuth.getInstance().getCurrentUser()
                                                    .getDisplayName(), "pending");

                                    //se introducen los datos de la friend request en la bdd
                                    dataref.setValue(request).addOnCompleteListener(
                                            new OnCompleteListener<Void>()
                                            {
                                                /**
                                                 * Método para comprobar que se haya llevado a cabo
                                                 * las acciones de introducir la petición
                                                 * */

                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    //si la tarea se completa satisfactoriamente
                                                    //mandamos un mensaje de feedback indicandolo
                                                    if (task.isSuccessful())
                                                    {
                                                        Toast.makeText(getApplicationContext(),
                                                                R.string.requestsent,
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                    //si no se manda un mensaje diciendo que ha
                                                    //habido un error
                                                    else
                                                    {
                                                        Toast.makeText(getApplicationContext(),
                                                                R.string.requestsenderror,
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            }

                            /**
                             * Método para gestionar fallo con la base de datos
                             * */
                            @Override
                            public void onCancelled(@NonNull DatabaseError error)
                            {
                                //se envía un toast indicando el fallo de checkear
                                Toast.makeText(getApplicationContext(),
                                        R.string.requestcheckerror,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                /**
                 * Método para gestionar fallo con la base de datos
                 * */
                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    //se envia un toast diciendo que no se ha podido comprobar la amistad
                    Toast.makeText(getApplicationContext(),
                            R.string.alreadycheckerror,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        //Si no se manda un mensaje diciendo que no puedes ser tu propio amigo
        else
        {
            Toast.makeText(getApplicationContext(), R.string.ownfrienderror,
                    Toast.LENGTH_SHORT).show();
        }

    }
}