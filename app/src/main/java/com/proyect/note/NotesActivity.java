package com.proyect.note;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.proyect.R;
import com.proyect.user.UserSettings;

/**
 * Actividad para crear y modificar notas rápidas de manera local para cada usuario
 * Extiende la clase View.OnClickListener para dar funcionalidad a los botones
 * //
 * *-Yosef-* falta por implementar lo ultimo que dijo el profe que es eliminarlas con un swipe
 * he estado mirando y ya sé como hacerlo, en cuanto pueda lo implemento, pero no lo veo
 * lo más prioritario ahora mismo 29/10/2024
 * */

public class NotesActivity extends AppCompatActivity implements View.OnClickListener
{
    /**
     * Creamos las variables de clase que necesitaremos
     * //
     * Un imageview para la imagen del usuario
     * un textview para mostrar el nombre del usuario
     * //
     * Botones para salvar la nota
     * //
     * *-Yosef-* igual debería implementar que se saliese de la nota directamente al dar en guardar
     * así sería más rapido de crear y destruir notas que es lo que el profe quería ¿Que os parece?
     * lo voy a dejar implementado, lo probais y me decis que os parece esta funcionalidad
     * //
     * Dos edittext en los que pondremos título y cuerpo a la nota
     * Variables String para contener el nombre de la nota que se pasa desde otras activities
     * o fragments así como el contenedor de las notas.
     * El noteStore es para que cada contenedor aun siendo local sea exclusivo para cada usuario
     * */

    ImageView ivUserImage;
    TextView tvDisplayname;

    ImageView btnSave;
    EditText etTitle;
    EditText etBody;

    String noteName;
    String noteBody;
    String noteStore;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Métodos necesarios para que se muestren correctamente los elementos en pantalla
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Se fuerza a la aplicación a mostrarse en vertical
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Creamos una toolbar para el main de la aplicación
        Toolbar toolbar = (Toolbar)findViewById(R.id.tb_notes);

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

        //Se inicializan las variables asignandolas a sus elementos visuales
        btnSave = findViewById(R.id.btn_save);
        etTitle = findViewById(R.id.et_note_title);
        etBody = findViewById(R.id.et_note_body);

        //Cogemos el intent que inició la actividad

        Intent intent = getIntent();

        //Si el intent no es nulo y tiene extras
        if(intent != null && intent.getExtras() != null)
        {
            //Asignamos cada valor a los strings que ya habíamos previamente creado
            noteStore = intent.getStringExtra("note_store");
            noteName = intent.getStringExtra("note_name");
            noteBody = intent.getStringExtra("note_body");

            //ponemos los textos, en caso de que tuviera, para poder modificar la nota
            etTitle.setText(noteName);
            etBody.setText(noteBody);
        }

        //Le ponemos un escuchador de click al botón
        btnSave.setOnClickListener(this);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {
            /**
             * Le damos funcionalidad al botón de volver atrás
             * En este caso queremos que cuando se de atrás se guarde la nota
             * de la misma manera que si hubiese pulsado en el botón de guardar
             * */

            @Override
            public void handleOnBackPressed()
            {
                //Creamos una vista cogiendo el contexto de la aplicación
                View view = new View(getApplicationContext());

                //Ejecutamos el método onClick, al igual que
                //si se hubiese el botón de guardar si el usuario
                //ha puesto algún título a la nota
                //sino se cierra como antes
                if(!etTitle.getText().toString().isEmpty())
                {
                    onClick(view);
                }
                else
                {
                    finish();
                }
            }
        });
    }

    /**
     * Método OnClick sobreescrito de la interfaz para poder dar funcionaldiad a los botones
     * */

    @Override
    public void onClick(View view)
    {
        if(!etTitle.getText().toString().isEmpty())
        {
            //cogemos el contenedor de las sharedprefences
            SharedPreferences sharedNotes = view.getContext().getSharedPreferences(noteStore, Context.MODE_PRIVATE);

            //Incluimos al nota al contenedor
            sharedNotes.edit().putString(etTitle.getText().toString(), etBody.getText().toString()).apply();

            //mostramos un Toast de feedback
            Toast.makeText(view.getContext(), getString(R.string.savednote), Toast.LENGTH_SHORT).show();

            //Cerramos al activity al guardar la nota para que vuelva
            finish();
        }

        else
        {
            Toast.makeText(view.getContext(), getString(R.string.savenoteerror), Toast.LENGTH_SHORT).show();
        }

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