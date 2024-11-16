package com.proyect.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.proyect.MainActivity;
import com.proyect.R;

/**
 * Actividad para que un usuario pueda logearse en la app
 * Implementa OnClickListener para poder dar funcionalidades de click
 * */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener
{
    /**
     * Variable de clase privadas que será instancia para cargar la base de datos
     * */

    private FirebaseAuth firebaseAuth;

    /**
     * Variables de clase que son los campos de texto que se deben introducir, el botón de inicio,
     * el textview que funcionará como un botón y un toggle button para recordar al usuario
     * */

    EditText etEmail;
    EditText etPassword;
    Button btnLogin;
    TextView tvSignUpTxtBtn;
    ToggleButton tgbRememberme;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Métodos necesarios para mostrar correctamente los elementos en pantalla
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Hacemos instancia de la base de datos de autenticación
        firebaseAuth = FirebaseAuth.getInstance();

        //Se fuerza a la aplicación a mostrarse en vertical
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Asignamos los elementos a cada uno de los campos
        etEmail = (EditText)findViewById(R.id.et_email);
        etPassword = (EditText)findViewById(R.id.et_password);
        btnLogin = (Button)findViewById(R.id.btn_login);
        tgbRememberme = findViewById(R.id.tgb_rememberme);
        tvSignUpTxtBtn = (TextView)findViewById(R.id.tv_signup_txtBtn);

        //Le damos la funcionalidad al botón de inicio y al textview
        btnLogin.setOnClickListener(this);
        tvSignUpTxtBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view)
    {
        //guardamos en la variable el id de donde se ha pulsado
        int id = view.getId();

        //pasamos el contenido de los campos de introducción de texto
        //a variables strings
        String username = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        //Si se ha pulsado el botón de login se comprueba si los campos están vacios
        if(id == R.id.btn_login)
        {
                //si los campos no estaban vacios, se comprueba que estén introduciendo bien el email
                if(!username.isEmpty() && !password.isEmpty())
                {
                    //Si el email está bien se inicia la pantalla principal de la app
                    if(username != null && Patterns.EMAIL_ADDRESS.matcher(username).matches())
                    {
                        logIn(username, password);
                    }

                    //Si no se muestra un Toast indicando el error
                    else
                    {
                        Toast.makeText(getApplicationContext(),
                                R.string.emaildoesntmatch,
                                Toast.LENGTH_SHORT).show();
                    }
                }
                //Si no se muestra un Toast indicando el error
                else
                {
                    Toast.makeText(getApplicationContext(), R.string.loginNoText
                            , Toast.LENGTH_SHORT).show();
                }
        }

        //Si se ha pulsado el textview que funciona como botón
        //se abre la actividad de registro y se cierra la de login
        if(id == R.id.tv_signup_txtBtn)
        {
            Intent i = new Intent(getApplicationContext(), SignupActivity.class);

            startActivity(i);

            finish();
        }

    }

    /**
     * Método para iniciar el la sesión de un usuario
     *
     * @param email email del usuario, único para cada usuario
     * @param password contraseña del usuario
     * */

    private void logIn(String email, String password)
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
                            //Cogemos el usuario *-Yosef-* lo comento por que no tiene función de momento
                            //coger al usuario en este método, ya que no hacemos nada con él
                            //FirebaseUser user = firebaseAuth.getCurrentUser();

                            //Si el botón de recuerdame está pulsado
                            if(tgbRememberme.isChecked())
                            {
                                //creamos un contenedor de preferencias
                                SharedPreferences sharedPreferences =
                                        getSharedPreferences("login", MODE_PRIVATE);

                                //Creamos un editor
                                SharedPreferences.Editor editor = sharedPreferences.edit();

                                //Guardamos los datos del usuario y un valor
                                //para volver a iniciar sesión
                                editor.putString("email", email);
                                editor.putString("password", password);
                                editor.putBoolean("rememberme", true);

                                //Aplicamos los cambios
                                editor.apply();
                            }

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