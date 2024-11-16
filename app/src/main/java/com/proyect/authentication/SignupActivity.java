package com.proyect.authentication;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Patterns;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.proyect.R;
import com.proyect.user.User;

/**
 * Actividad para que un usuario pueda registrarse en la app
 * Implementa OnClickListener para poder dar funcionalidades de click
 * */

public class SignupActivity extends AppCompatActivity implements View.OnClickListener
{
    /**
     * Variables de clase que son los campos de texto que se deben introducir y el botón de registro
     * */

    EditText etEmail;
    EditText etPassword;
    EditText etConfirmPass;
    EditText etUsername;
    Button btnSignup;

    /**
     * Variables de clase privadas que serán instancias para cargar la base de datos
     * */

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseUser user;

    /**
     * Método para la creación de la actividad
     * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        //Se hace que los botones en pantalla no oculten las funcionalidad de la app
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Hacemos instancias de la base de datos de autenticación y datos de usuarios
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        //Se fuerza a la aplicación a mostrarse en vertical
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Asignamos los elementos a cada uno de los campos
        etEmail = (EditText) findViewById(R.id.et_email);
        etPassword = (EditText) findViewById(R.id.et_password);
        etConfirmPass = (EditText) findViewById(R.id.et_confirm_pass);
        etUsername = (EditText) findViewById(R.id.et_username);
        btnSignup = (Button) findViewById(R.id.btn_signup);

        //Le damos la funcionalidad al botón de registro
        btnSignup.setOnClickListener(this);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {
            /**
             * Le damos funcionalidad al botón de volver atrás
             * En este caso queremos que cuando se de atrás se cargue el login
             * */

            @Override
            public void handleOnBackPressed()
            {
                //Creamos un intent de la actividad login
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);

                //iniciamos la actividad
                startActivity(i);

                //finalizamos la actividad de registro de usuarios
                finish();
            }
        });
    }

    /**
     * Sobreescribimos el método onClick para dar funcionalidad al botón
     * */
    @Override
    public void onClick(View view)
    {
        //Guardamos los datos de los campos de texto en strings
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String confirmPass = etConfirmPass.getText().toString();
        String username = etUsername.getText().toString();

        //Si ninguno de los campos está vacío pasamos a comprobar si el email está bien formado
        if (!email.isEmpty() && !username.isEmpty() &&
                !password.isEmpty() && !confirmPass.isEmpty())
        {
            //Si el email está bien formado, pasamos a comprobar si las contraseñas coinciden
            if (email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches())
            {
                //Si las contraseñas coinciden, se crea la cuenta
                if (password.equals(confirmPass))
                {
                    createAccount(email, password, username);
                }
                //Si no lo indicamos con un mensaje al usuario con un toast
                else
                {
                    Toast.makeText(getApplicationContext(), R.string.passdontmatch,
                            Toast.LENGTH_SHORT).show();
                }
            }
            //Si no lo indicamos con un mensaje al usuario con un toast
            else
            {
                Toast.makeText(getApplicationContext(),
                        R.string.emaildoesntmatch,
                        Toast.LENGTH_SHORT).show();
            }
        }

        //Si no lo indicamos con un mensaje al usuario con un toast
        else
        {
            Toast.makeText(getApplicationContext(), R.string.loginNoText,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Método createAccount que sirve para crear una nueva cuenta y registrarlas
     * en las bases de datos
     *
     * @param email email del usuario, único para cada usuario
     * @param password password de cada usuario
     * @param username el nombre de cada usuario para la aplicación
     * */

    private void createAccount(String email, String password, String username)
    {
        //usamos el método de creación de firebase para crear un usuario nuevo
        //le pasamos un escuchador para que cuando se complete la acción siga
        //realizando las acciones necesarias, pero solo cuando se ha realizado
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    /**
                     * Método onComplete para que cuando se complete la tarea de crear un usuario
                     * le ponga el displayname elegido por el usuario
                     * */
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        //si la tarea se cumple correctamente se pone el nombre de usuario
                        if (task.isSuccessful())
                        {
                            //cogemos el usuario actual, que es el recién creado
                            user = firebaseAuth.getCurrentUser();

                            //indicamos que queremos cambiar el nombre de usuario pasándole el username
                            UserProfileChangeRequest profileChange = new UserProfileChangeRequest.Builder()
                                   .setDisplayName(username).build();

                            //decimos a la base que realice los cambios indicados
                           user.updateProfile(profileChange).addOnCompleteListener(new OnCompleteListener<Void>()
                           {
                               /**
                                * Metodo onComplete para que cuando se hayan hecho los cambios
                                * se meta el usuario en la base de datos, para registrar todos los
                                * usurios y que se puedan hacer amigos
                                * */
                               @Override
                               public void onComplete(@NonNull Task<Void> task)
                               {
                                   //Escribimos el usuario con los datos necesarios
                                   writeNewUser(user.getUid(), user.getDisplayName(), user.getEmail());

                                   //Hacemos un intent de la actividad login
                                   Intent i = new Intent(getApplicationContext(), LoginActivity.class);

                                   //iniciamos la actividad
                                   startActivity(i);

                                   //finalizamos la actividad de registro
                                   finish();
                               }
                           });

                        }
                        //sino se indica que no se pudo crear la cuenta
                        else
                        {
                            Toast.makeText(getApplicationContext(), R.string.accountnotcreated,
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    /**
     * Método para escribir un usuario en la base de datos, para que pueda tener amigos
     * */

     private void writeNewUser (String userId, String name, String email)
    {
        //Creamos un usuario con los datos de nombre y email
        User user = new User(name, email, userId);

        //incluimos en la base datos, en el json de la base, los datos del nuevo usuario
        //le añadimos un escuchador para cuando se haya completado la tarea
        databaseReference.child("users").child(userId).setValue(user)
                .addOnCompleteListener(task ->
                {
                    //Si se ha podido registrar sale un Toast indicandolo
                    if (task.isSuccessful())
                    {
                        Toast.makeText(getApplicationContext(), R.string.userregistered,
                                Toast.LENGTH_SHORT).show();
                    }
                    //Si no se incluye un log en la consola y se lanza un Toast al usuario
                    else
                    {
                        Log.d("SignupActivity", "Error al registrar usuario: " + task.getException().getMessage());

                        Toast.makeText(getApplicationContext(), R.string.registererror,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}