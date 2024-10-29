package com.proyect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
     * Botones para salvar la nota y para salir de ella
     * //
     * *-Yosef-* igual debería implementar que se saliese de la nota directamente al dar en guardar
     * así sería más rapido de crear y destruir notas que es lo que el profe quería ¿Que os parece?
     * //
     * Dos edittext en los que pondremos título y cuerpo a la nota
     * Variables String para contener el nombre de la nota que se pasa desde otras activities
     * o fragments así como el contenedor de las notas.
     * El noteStore es para que cada contenedor aun siendo local sea exclusivo para cada usuario
     * */
    Button btnExit;
    Button btnSave;
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

        //Se inicializan las variables asignandolas a sus elementos visuales
        btnSave = findViewById(R.id.btn_save);
        btnExit = findViewById(R.id.btn_exit);
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

        //Le ponemos un escuchador de click a cada uno de los botones
        btnSave.setOnClickListener(this);
        btnExit.setOnClickListener(this);
    }

    /**
     * Método OnClick sobreescrito de la interfaz para poder dar funcionaldiad a los botones
     * */

    @Override
    public void onClick(View view)
    {
        //Si el botón pulsado es guardar
        if(view.getId() == R.id.btn_save)
        {
            //cogemos el contenedor de las sharedprefences
            SharedPreferences sharedNotes = view.getContext().getSharedPreferences(noteStore, Context.MODE_PRIVATE);

            //Incluimos al nota al contenedor
            sharedNotes.edit().putString(etTitle.getText().toString(), etBody.getText().toString()).apply();

            //mostramos un Toast de feedback
            Toast.makeText(view.getContext(), getString(R.string.savednote), Toast.LENGTH_SHORT).show();
        }

        //Si es el de salir se cierra la actividad
        else
        {
            finish();
        }
    }
}