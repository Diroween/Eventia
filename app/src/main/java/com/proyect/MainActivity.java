package com.proyect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.proyect.calendar.CalendarFragment;
import com.proyect.friend.FriendsFragment;
import com.proyect.note.NotesFragment;
import com.proyect.notification.NotificationSettingsActivity;
import com.proyect.today.TodayFragment;
import com.proyect.user.UserSettings;

import java.util.ArrayList;

/**
 * Creamos MainActivity con implementación de View.OnClickListener para mejorar la gestión del
 * método onClick
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    /**
     * Creamos las variables de clase necesarias
     * //
     * Un imageview para la imagen del usuario
     * un textview para mostrar el nombre del usuario
     * //
     * Declaramos cuatro variables de imagen que funcionarán como botones
     * para moverse por los fragments
     * //
     * hacemos un arraylist para today
     * *-Yosef-* Creo que esto abrá que borrarlo a futuro, ya que es un método rudimentario
     * y poco eficaz para poder implmentar las cosas de hoy, habrá que hacer otro RecyclerView
     * //
     * Y el manejador de fragments
     */
    private static final int REQUEST_CODE = 100;
    ImageView ivUserImage;
    TextView tvDisplayname;

    ImageView ivCalendar;
    ImageView ivToday;
    ImageView ivNotes;
    ImageView ivFriends;

    ArrayList<String> arrayToday;

    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        //¡Método necesario para que los botones en pantalla no tapen la applicación!
        //¡no borrar!
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Se fuerza a la aplicación a mostrarse en vertical
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Creamos una toolbar para el main de la aplicación
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_main);

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
        if (imageUri != null) {
            //Cargamos la imagen
            Glide.with(this).load(imageUri.toString())
                    .placeholder(R.drawable.baseline_tag_faces_128)
                    .transform(new CircleCrop())
                    .into(ivUserImage);
        }

        //Si no la tiene
        else {
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

        //Inicializamos los imageview que funcionarán como botones
        ivCalendar = findViewById(R.id.btn_events);
        ivToday = findViewById(R.id.btn_today);
        ivNotes = findViewById(R.id.btn_notes);
        ivFriends = findViewById(R.id.btn_friends);

        //les ponemos como recurso una imagen
        ivCalendar.setBackgroundResource(R.drawable.btn_calendar_colored50x50);
        ivToday.setBackgroundResource(R.drawable.btn_today_nocolor50x50);
        ivNotes.setBackgroundResource(R.drawable.btn_notes_nocolor50x50);
        ivFriends.setBackgroundResource(R.drawable.btn_laugh_nocolor50x50);

        //Declaramos un fragment y su manager para ser el principal al abrir la app
        //que será el fragment de calendario
        fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        CalendarFragment calendarFragment = new CalendarFragment();
        fragmentTransaction.add(R.id.ll_fragments_main, calendarFragment);
        fragmentTransaction.commit();

        //le ponemos a cada botón un escuchador de eventos
        ivCalendar.setOnClickListener(this);
        ivToday.setOnClickListener(this);
        ivNotes.setOnClickListener(this);
        ivFriends.setOnClickListener(this);

        requestNotificationPermission();

        arrayToday = new ArrayList<String>();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            /**
             * Le damos funcionalidad al botón de volver atrás
             * En este caso queremos que cuando se de atrás en cualquier fragment cargado
             * Se cargue el fragment principal para que no pase por los demás
             * Y si el Fragment principal está cargado se salga de la aplicación
             * al darle al botón atrás
             * */

            @Override
            public void handleOnBackPressed() {
                //Cogemos el manejador de eventos
                FragmentManager fragmentManager = getSupportFragmentManager();

                //Si el fragment cargado es una instacia del fragment principal
                //El calendarfragment, se vuelve a cargar para actualizarse
                //sino se carga el calendario para mostrar los eventos

                ivCalendar.setBackgroundResource(R.drawable.btn_calendar_colored50x50);
                ivToday.setBackgroundResource(R.drawable.btn_today_nocolor50x50);
                ivNotes.setBackgroundResource(R.drawable.btn_notes_nocolor50x50);
                ivFriends.setBackgroundResource(R.drawable.btn_laugh_nocolor50x50);

                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.ll_fragments_main, new CalendarFragment());
                transaction.commit();
            }
        });
    }

    /**
     * Orverrideamos el método onClick para dar funcionalidad a los iv que funcionan como botones
     */

    @Override
    public void onClick(View view) {
        //Creamos un fragment
        Fragment fragment;

        //Creamos un bundle para pasar datos desde la main
        Bundle bundle = new Bundle();

        //Pasamos al bundle un Arraylist de String
        bundle.putStringArrayList("arrayToday", arrayToday);

        //Dependiendo del botón que pulsemos inicializamos el fragment correspondiente
        //y se cambia el color de los botones
        if (view.getId() == R.id.btn_today) {
            fragment = new TodayFragment();

            ivCalendar.setBackgroundResource(R.drawable.btn_calendar_nocolor50x50);
            ivToday.setBackgroundResource(R.drawable.btn_today_colored50x50);
            ivNotes.setBackgroundResource(R.drawable.btn_notes_nocolor50x50);
            ivFriends.setBackgroundResource(R.drawable.btn_laugh_nocolor50x50);

        } else if (view.getId() == R.id.btn_notes) {
            fragment = new NotesFragment();

            ivCalendar.setBackgroundResource(R.drawable.btn_calendar_nocolor50x50);
            ivToday.setBackgroundResource(R.drawable.btn_today_nocolor50x50);
            ivNotes.setBackgroundResource(R.drawable.btn_notes_colored50x50);
            ivFriends.setBackgroundResource(R.drawable.btn_laugh_nocolor50x50);
        } else if (view.getId() == R.id.btn_friends) {
            fragment = new FriendsFragment();

            ivCalendar.setBackgroundResource(R.drawable.btn_calendar_nocolor50x50);
            ivToday.setBackgroundResource(R.drawable.btn_today_nocolor50x50);
            ivNotes.setBackgroundResource(R.drawable.btn_notes_nocolor50x50);
            ivFriends.setBackgroundResource(R.drawable.btn_laugh_colored50x50);
        } else {
            fragment = new CalendarFragment();

            ivCalendar.setBackgroundResource(R.drawable.btn_calendar_colored50x50);
            ivToday.setBackgroundResource(R.drawable.btn_today_nocolor50x50);
            ivNotes.setBackgroundResource(R.drawable.btn_notes_nocolor50x50);
            ivFriends.setBackgroundResource(R.drawable.btn_laugh_nocolor50x50);
        }

        //Le pasamos como argumentos al fragment nuestro bundle
        fragment.setArguments(bundle);

        //Hacemos la transición del fagment actual al nuevo fragment
        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.ll_fragments_main, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    /**
     * Realizamos un override de los métodos necesarios para crear el menú
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //le decimos al menú que la crearse y use el layout de la carpeta menu
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    /**
     * Este método sirve para darle funcionalidad a los item del menú
     * *-yosef-* no creo que la vayamos a usar pero bueno, ahí está por si acaso
     */

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return false;
    }

    /**
     * Sobreescritura de onDestroy, por si debemos cerrar algo, de momento no es necesario.
     */

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                    showRationaleDialog();
                } else {
                    // Request permission
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE);
                }
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                showRationaleDialog();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showRationaleDialog() {
        Intent intent = new Intent(this, NotificationSettingsActivity.class);
        startActivity(intent);
    }
}