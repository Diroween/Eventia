package com.proyect.event;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyect.R;
import com.proyect.friend.FriendsAdapter;
import com.proyect.notification.NotificationHelper;
import com.proyect.user.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Actividad para poder ver los eventos
 */

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
     * Las variables String que representan los datos de un evento
     * El mapa con los roles de cada usuario
     */

    RecyclerView rv_users;
    ArrayList<User> registeredUsers;
    FriendsAdapter adapter;
    DatabaseReference reference;

    TextView tvEventName;
    TextView tvEventData;
    ImageView ivEventImage;

    FloatingActionButton fbAddFriends;
    FloatingActionButton fbEditEvent;
    FloatingActionButton fbDeleteEvent;

    String eventId;
    String eventName;
    String eventDate;
    String eventImage;
    String eventPlace;
    String eventHour;
    String eventData;

    HashMap<String, String> roles;

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
        fbEditEvent = findViewById(R.id.fb_edit_event);
        fbDeleteEvent = findViewById(R.id.fb_delete_event);

        roles = new HashMap<String, String>();

        //Cogemos una referncia a base de datos
        reference = FirebaseDatabase.getInstance().getReference();

        //Recogemos los datos del evento que pasamos desde el adaptador de calendarFragment
        Intent intent = getIntent();

        eventId = intent.getStringExtra("event_id");

        loadData();
        loadRoles();

        rv_users = findViewById(R.id.rv_registered_users);

        rv_users.setLayoutManager(new LinearLayoutManager(this));

        registeredUsers = new ArrayList<User>();

        String userId = FirebaseAuth.getInstance().getUid();

        //Al adaptador le pasamos true para que se despliegue el menu contextual
        adapter = new FriendsAdapter(registeredUsers, userId, this::showContextMenu,
                roles, true);

        rv_users.setAdapter(adapter);

        checkAdmin(eventId);

        //Damos función al botón de añdir amigos al evento
        fbAddFriends.setOnClickListener(l ->
        {
            Intent intentInvite = new Intent(this, EventInviterActivity.class);

            intentInvite.putExtra("event_id", eventId);
            intentInvite.putExtra("event_name", eventName);

            startActivity(intentInvite);
        });

        //damos función al botón de editar un evento
        fbEditEvent.setOnClickListener(l ->
        {
            Intent intentEditEvent = new Intent(this, EventEditorActivity.class);

            intentEditEvent.putExtra("event_id", eventId);
            intentEditEvent.putExtra("event_name", eventName);
            intentEditEvent.putExtra("event_place", eventPlace);
            intentEditEvent.putExtra("event_date", eventDate);
            intentEditEvent.putExtra("event_hour", eventHour);
            intentEditEvent.putExtra("event_image", eventImage);

            startActivity(intentEditEvent);
        });

        //Eliminamos al usuario del evento
        //Si es el último registrado se elimina el evento
        fbDeleteEvent.setOnClickListener(l ->
        {
            removeUserFromEvent(eventId);
        });

        //Cargamos los usuarios registrado en el evento
        loadRegisteredUsers(eventId);
    }

    /**
     * Método onResume de la actividad para actualizar los datos mostrados
     * (NO METER NADA MÁS AQUÍ, la aplicación carga muy rápido y a firebase no le da tiempo cargar
     * los datos del evento)
     * */

    @Override
    protected void onResume()
    {
        super.onResume();
        loadData();
    }

    /**
     * Método para cargar los datos del evento seleccionado en pantalla
     * */

    private void loadData()
    {
        //Cogemos la referencia en la base de datos del evento
        //cargamos todos los datos que necesitamos y los asignamos a los elementos en pantalla
        reference.child("events").child(eventId).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                //recogemos el evento de la bdd
                Event event = snapshot.getValue(Event.class);

                //Si el evento existe
                if(event != null)
                {
                    //Cargamos todas la variables
                    eventName = event.getName();
                    eventDate = event.getDate();
                    eventPlace = event.getPlace();
                    eventHour = event.getHour();
                    eventImage = event.getImage();
                    eventData = formatDate(event);

                    //Asignamos los datos recogidos a los elementos en pantalla
                    tvEventName.setText(eventName);
                    tvEventData.setText(eventData);

                    Glide.with(getApplicationContext())
                            .load(eventImage)
                            .placeholder(R.drawable.ic_event_list)
                            .transform(new CircleCrop())
                            .into(ivEventImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Log.e("Error", "No se han podido cargar datos");
            }
        });
    }

    /**
     * Método para cargar el mapa de roles de los usuarios registrados en el evento
     * */

    private void loadRoles()
    {
        //vaciamos el mapa de roles
        roles.clear();

        //cogemos al referencia a la bdd y buscamos usuarios y roles en el evento
        reference.child("events").child(eventId).addListenerForSingleValueEvent(
                new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                Event event = snapshot.getValue(Event.class);

                if(event != null)
                {
                    //Guardamos los roles de los usuarios
                    roles.putAll(event.getRegisteredUsers());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Log.e("Error", "No se han podido cargar roles");
            }
        });


    }

    /**
     * Método para poder cargar los usuarios registrado en la lista
     */

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
                        //Si existe se añade el usuario
                        //y se notifican cambios de datos para el adaptador
                        //si no se puede se loggea el mensaje de error
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String userId = dataSnapshot.getKey();

                            reference.child("users").child(userId)
                                    .addListenerForSingleValueEvent(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot)
                                        {
                                            User user = snapshot.getValue(User.class);

                                            if (user != null)
                                            {
                                                registeredUsers.add(user);
                                                registeredUsers.sort(new UserRoleComparator(roles));
                                                adapter.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.println(Log.INFO, "Info", error.getMessage());
                                        }
                                    });
                        }
                    }

                    /**
                     * Si no se puede se loggea el mensaje
                     * */

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.println(Log.INFO, "Info", error.getMessage());
                    }
                });
    }

    /**
     * Método para que un usuario abandone un evento
     *
     * @param eventId el id del evento
     */

    private void removeUserFromEvent(String eventId) 
    {
        //Cogemos el id del usuario
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Cogemos la referencia del usuario en los usuarios registrados al evento
        DatabaseReference eventRegistered = reference.child("events").child(eventId)
                .child("registeredUsers").child(userId);

        //Se pregunta al usuario si de verdad quiere salirse de un evento
        //Si es afirmativo se procede al borrado
        Snackbar.make(findViewById(android.R.id.content),
                        R.string.leaveeventquestion, Snackbar.LENGTH_LONG)
                .setAction(R.string.leave, var ->
                {
                    //Eliminamos al usuario
                    eventRegistered.removeValue().addOnCompleteListener(task ->
                    {
                        if (task.isSuccessful()) 
                        {
                            Toast.makeText(this, R.string.registereddelete
                                    , Toast.LENGTH_SHORT).show();

                            //Comprobamos si hay administrados, si no es así
                            //se intenta asignar uno nuevo
                            checkAndPromoteToAdmin(userId);

                            //Checkeamos si hay usuarios registrados, si no los hay se elimina
                            noRegisteredUsersDelete(eventId);

                            //Cancelamos todos las notificaciones programadas para dicho evento
                            NotificationHelper.cancelAllWorkRequests(eventId);

                            //cerramos la vista del evento
                            finish();
                        }

                        //Si no se ha podido se manda un Toast
                        else 
                        {
                            Toast.makeText(this, R.string.registereddeleteerror,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                  
                }).show();
    }

    /**
     * Método para verificar si quedan usuarios registrados
     * y eliminar el evento si no hay usuario registrados a él
     */

    private void noRegisteredUsersDelete(String eventId)
    {
        //cogemos una referencia a los usuarios registrados al evento
        DatabaseReference registeredUsersRef = reference.child("events").child(eventId)
                .child("registeredUsers");

        registeredUsersRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0)
                {
                    //Si no quedan usuarios registrados, eliminamos el evento
                    reference.child("events").child(eventId).removeValue()
                            .addOnCompleteListener(task ->
                            {
                                if (task.isSuccessful())
                                {
                                    Toast.makeText(EventViewerActivity.this,
                                            R.string.eventdeleted,
                                            Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Toast.makeText(EventViewerActivity.this,
                                            R.string.eventdeletederror,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            /**
             * Si no se puede borrar el evento se guarda un log
             * */
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error",
                        "Error al verificar usuarios registrados en el evento",
                        error.toException());
            }
        });
    }

    /**
     * Método que comprueba si hay administradores al salirse un usuario del evento
     * Si no hay se promueve a un usuario registrado en el evento a administrador
     *
     * @param removedUserId id del usuario que se ha salido del evento
     */
    private void checkAndPromoteToAdmin(String removedUserId)
    {
        //Cogemos una referencia de los usuarios registrado en la base de datos
        DatabaseReference eventRegister = reference.child("events").child(eventId)
                .child("registeredUsers");

        //Añadimos un escuchador para encontrar si alguien es admin
        eventRegister.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                //Creamos dos variables
                //la primera guarda si hay ya un rol de admin+
                //la segunda guarda el id del usuario que será el nuevo administrador
                boolean adminRole = false;
                String newAdminId = null;

                //En el bucle:
                //Se mira si un usuario tiene el rol de administrador, si es así se sale del bucle
                //y no se asigna ningún nuevo rol
                //si no nadie tiene un rol de admin se guarda el booleano como true y el id del nuevo
                //admin
                for (DataSnapshot userSnapshot : snapshot.getChildren())
                {
                    String userId = userSnapshot.getKey();
                    String role = userSnapshot.getValue(String.class);

                    if (role != null && role.equals("admin"))
                    {
                        adminRole = true;
                        break;
                    }

                    if (newAdminId == null && !userId.equals(removedUserId))
                    {
                        newAdminId = userId;
                    }
                }

                //Si no hay administrador y hay alguien a quien poder promover a administrador
                //se settea a la nueva persona como administrador
                //si no se ha podido hacer se hace un log del error
                if (!adminRole && newAdminId != null)
                {
                    eventRegister.child(newAdminId).setValue("admin").addOnCompleteListener(task ->
                    {
                        if (task.isSuccessful())
                        {
                            Log.e("INFO", "Un nuevo usuario ha sido promovido a admin");
                        }
                        else
                        {
                            Log.e("INFO", "No se ha podido promover a nadie a admin");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Log.e("WARNING", "Error al realizar el cambio de admin");
            }
        });
    }

    /**
     * Método para comprobar si el usuario que abre el evento es admin
     * Si el usuario es administrador se le permitirá modificar un evento y añadir amigos a él
     */

    private void checkAdmin(String eventId)
    {
        //Cogemos el id del usuario
        String userId = FirebaseAuth.getInstance().getUid();

        //buscamos al usuario en el evento
        reference.child("events").child(eventId).child("registeredUsers")
                .child(userId).addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        //guardamos el rol del usuario
                        String role = snapshot.getValue(String.class);

                        //Si tiene rol y es administrador, podrá editar el evento e invitar amigos
                        //Si no, no le aparecerá los botones en el layout
                        if (role != null && role.equals("admin"))
                        {
                            fbEditEvent.setVisibility(View.VISIBLE);
                            fbAddFriends.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            fbEditEvent.setVisibility(View.INVISIBLE);
                            fbAddFriends.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        Log.e("Info", "No se ha encontrado el rol del usuario");
                    }
                });
    }

    /**
     * Método para poder mostrar un context menú para la pulsación larga en un usuario
     *
     * @param view vista donde cargar el menú
     * @param user usuario selección en el que se hace la pulsación larga
     */

    private void showContextMenu(View view, User user) {
        //Cogemos el id del usuario actual
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Cogemos la referencia del usuario activo en la base de datos
        reference.child("events").child(eventId).child("registeredUsers").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //miramos si el usuarios es un administrador
                        String role = snapshot.getValue(String.class);

                        //Si el usuario es administrador se infla el menú contextual y se muestra
                        if ("admin".equals(role)) {
                            PopupMenu popup = new PopupMenu(EventViewerActivity.this, view);

                            popup.getMenuInflater().inflate(R.menu.event_admin_context_menu, popup.getMenu());

                            //Se le asigna función a la pulsación de las distintas opciones del menú
                            popup.setOnMenuItemClickListener(item -> handleContextItemSelected(item, user));

                            popup.show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Error", "Error al verificar el rol del usuario");
                    }
                });
    }

    /**
     * Método para poder dar funcionalidad a la opción seleccionada en el menú contextual
     *
     * @param item la opción del menú contextual que se elige
     * @param user usuario en el que se ha desplegado el menú contextual
     */

    private boolean handleContextItemSelected(MenuItem item, User user) {
        //dependiendo de la opción seleccionada se ejecuta el método correspondiente
        //para quitar a una persona del evento o hacerle admin
        if (item.getItemId() == R.id.menu_remove_user)
        {
            removeUserAsAdmin(user);

            return true;
        }
        else if (item.getItemId() == R.id.menu_make_admin)
        {
            makeUserAdmin(user);

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Método para quitar a un usuario del evento
     * esto solo podrá hacerse en caso de ser administrador
     *
     * @param user el usuario que se desea eliminar
     */

    private void removeUserAsAdmin(User user)
    {
        //Se coge la referencia en la bdd de la persona, en el evento,
        //en la que se ha desplegado el menú contextual
        DatabaseReference userReference = reference.child("events").child(eventId)
                .child("registeredUsers").child(user.getId());

        //Se elimina a ese usuario de la base de datos y se manda un toast informativo
        //tanto si se ha podido como si ha ocurrido un error
        //y cancelamos todos las notificaciones programadas para dicho evento
        userReference.removeValue().addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                Toast.makeText(this, R.string.userremovedfromevent
                        , Toast.LENGTH_SHORT).show();

                NotificationHelper.cancelAllWorkRequests(eventId);
            }
            else
            {
                Toast.makeText(this, R.string.frienddeletederror, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Método para promover a un usuario a ser administrador
     *
     * @param user el usuario que se desea promover
     */
    private void makeUserAdmin(User user)
    {
        //Se coge la referencia en la bdd de la persona, en el evento,
        //en la que se ha desplegado el menú contextual
        DatabaseReference userReference = reference.child("events").child(eventId)
                .child("registeredUsers").child(user.getId());

        //Se añade un escuchador de eventos para comprobar si el usuario ya es un admin
        userReference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                //cogemos el rol
                String role = snapshot.getValue(String.class);

                //si es admin se manda un Toast indicandolo
                if (role != null && role.equals("admin"))
                {
                    Toast.makeText(EventViewerActivity.this, R.string.alreadyadmin
                            , Toast.LENGTH_SHORT).show();
                }

                //Si no, se pone admin como rol del usuario en la bdd y se manda un toast informativo
                //tanto si se ha podido como si ha ocurrido un error
                else
                {
                    userReference.setValue("admin").addOnCompleteListener(task ->
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(EventViewerActivity.this
                                    , R.string.userpromotedtoadmin, Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(EventViewerActivity.this
                                    , R.string.registererror, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Log.e("Error", "No se ha podido hacer administrador al usuario");
            }
        });
    }

    /**
     * Método para crear el texto que se muestra con los datos del eventos
     * dependiendo del idioma del dispositivo
     * @param event el evento que hemos seleccionado de la lista
     * */

    public String formatDate(Event event)
    {
        String formattedDate = "";

        try {
            //Creamos un formato para transformar la fecha guardad en String en fecha
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");

            //Creamos una fecha parseando el string guardado del evento
            Date date = inputDateFormat.parse(event.getDate());

            //Hacemos una instancia de Calendar
            Calendar calendar = Calendar.getInstance();

            //Le decimos que se settee como el día del evento
            calendar.setTime(date);

            String[] monthsArray = getApplicationContext()
                    .getResources().getStringArray(R.array.material_calendar_months_array);

            //Formateamos la fecha dependiendo de si está el dispositivo en ingles o en español
            //más parecido a los carteles de eventos
            if (Locale.getDefault().getLanguage().equals(new Locale("es").getLanguage()))
            {
                formattedDate = String.format("%02d de %s de %d a las %s en %s"
                        , calendar.get(Calendar.DAY_OF_MONTH)
                        , monthsArray[calendar.get(Calendar.MONTH)]
                        , calendar.get(Calendar.YEAR)
                        , event.getHour()
                        , event.getPlace());
            }
            else
            {
                formattedDate = String.format("%02d %s %d at %s in %s"
                        , calendar.get(Calendar.DAY_OF_MONTH)
                        , monthsArray[calendar.get(Calendar.MONTH)]
                        , calendar.get(Calendar.YEAR)
                        , event.getHour()
                        , event.getPlace());
            }

            return formattedDate;
        }

        //Si no se puede parsear se recoge una excepción
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Clase comparadora para ordenar en la vista detalle de un evento quien es administrador
     * Poninedolos primero en la lista
     * */

    public class UserRoleComparator implements Comparator<User>
    {
        private HashMap<String, String> compareRoles;

        public UserRoleComparator(HashMap<String, String> compareRoles)
        {
            this.compareRoles = compareRoles;
        }

        @Override
        public int compare(User u1, User u2)
        {
            String role1 = compareRoles.get(u1.getId());
            String role2 = compareRoles.get(u2.getId());

            //Si es administrador le asigna un valor menor para ponerlo más arriba en la lista
            //Si es invitado lo coloca después
            //y si son el mismo rol no pone a ninguna encima de otro
            if ("admin".equals(role1) && !"admin".equals(role2))
            {
                return -1;
            }
            else if (!"admin".equals(role1) && "admin".equals(role2))
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
    }


}