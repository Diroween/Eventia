package com.proyect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Timer;
import java.util.TimerTask;

public class Splash extends AppCompatActivity {

    /**
     * Variables de clase de los elementos que se van a ver
     * y también lo necesario para el inicio de sesión automático
     * */

    public ImageView ivSplash;
    public AnimationDrawable animation;


    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Métodos necesarios para las activities
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //iniciamos la base de datos
        FirebaseApp.initializeApp(this);

        //Hacemos instancia de la base de datos de autenticación
        firebaseAuth = FirebaseAuth.getInstance();

        //Se fuerza a la aplicación a mostrarse en vertical
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Inicializamos iv y le asigno un background
        ivSplash = findViewById(R.id.ivsplash);

        //Le asignamos como recurso de background la animación
        ivSplash.setBackgroundResource(R.drawable.animation_splash);

        //inicializamos el AnimationDrawable con el background de ivSplash y la inicio
        animation = (AnimationDrawable) ivSplash.getBackground();

        animation.start();

        //Creamos un timertask para que se inicie la activity main una vez termine la splash
        //Aquí hacemos que el hilo inicie la activity main y le decimos que espere 1600ms
        //para ello, que es el tiempo que tarda la animación en completarse

        new Timer().schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                //Paramos la animación
                animation.stop();

                //Creamos un intent que será la activity main pasandole el contexto de la splash
                //y la activity que va a ser
                Intent i = new Intent(Splash.this, LoginActivity.class);

                //cogemos el contenedor de login
                SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);

                //recogemos el valor booleano que nos indica si el usuario quiso que se le recordase
                boolean rememberme = sharedPreferences.getBoolean("rememberme", false);

                //si el valor es verdadero
                if(rememberme)
                {
                    //recogemos las variables de login del usuario
                    String storedEmail = sharedPreferences.getString("email", "");
                    String storedPassword = sharedPreferences.getString("password", "");

                    //si están correctamente guardadas se inicia sesión directamente
                    if(!storedEmail.isEmpty() && !storedPassword.isEmpty())
                    {
                        rememberLogIn(storedEmail, storedPassword);
                    }
                }

                else
                {
                    //Iniciamos la activity
                    startActivity(i);

                    //Finalizamos la activity para que no vuelva a saltar
                    finish();
                }

            }
        }, 1600);

    }

    /**
     * Método para iniciar el la sesión de un usuario automáticamente
     *
     * @param email email del usuario, único para cada usuario
     * @param password contraseña del usuario
     * */

    private void rememberLogIn(String email, String password)
    {
        //usamos el método de inicio de firebase para iniciar a un usuario
        //le pasamos un escuchador para que cuando se complete la acción siga
        //realizando las acciones necesarias, pero solo cuando se ha realizado
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    /**
                     * Método onComplete para que cuando se complete la tarea de iniciar un usuario
                     * se abra la pantalla principal de la app
                     * */
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if(task.isSuccessful())
                        {
                            //Creamos un intent de la actividad principal y cerramos esta
                            //y el registro en caso de que estuviese abierta
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);

                            startActivity(i);

                            finishAffinity();
                        }
                        //Si no se muestra un Toast al usuario
                        else
                        {
                            Toast.makeText(getApplicationContext(),
                                    R.string.accountnotfound,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}