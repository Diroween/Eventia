package com.proyect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UserSettings extends AppCompatActivity
{
    /**
     * Declaramos variables de clase de los elementos:
     * La imagen del usuario
     * Su id, nombre y email
     * el textview que funcionará como botón
     * un imageview para hacer logout
     * */

    ImageView ivUserImage;
    TextView tvUserId;
    TextView tvUserName;
    TextView tvUserEmail;
    TextView tvTxtbtnChangeImage;
    ImageView ivLogout;

    /**
     * Las variables que necesitamos para poder subir la foto y cargarla
     * */

    private static final int REQUEST_CODE = 2;
    private Uri userImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //metodos necesarios para que se muesten los elementos por pantalla correctamente
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Se fuerza a la aplicación a mostrarse en vertical
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Asignamos las variables a sus elementos gráficos
        ivUserImage = findViewById(R.id.iv_user_image);
        tvUserId = findViewById(R.id.tv_user_id);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        tvTxtbtnChangeImage = findViewById(R.id.tv_txtbtn_view_events);
        ivLogout = findViewById(R.id.iv_delete);

        //le asignamos la función de logout al botón
        ivLogout.setOnClickListener(new View.OnClickListener()
        {
            /**
             * Sobreescribimos el método de click para poder hace logut
             * */

            @Override
            public void onClick(View view)
            {
                //Cogemos la instacia de firebase
                FirebaseAuth.getInstance().signOut();

                //Creamos un intent de la login activity
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);

                //Le decimos al intent que queremos que se nos abra en una nueva tarea
                //limpiamos las tareas que hayan abierta para que no podamos volver a ellas
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                //Iniciamos la actividad
                startActivity(intent);

                //Cerramos la actividad actual
                finish();
            }
        });

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

        //Asignamos los valores de texto correspondientes al usuario activo
        tvUserId.setText(FirebaseAuth.getInstance().getCurrentUser().getUid());
        tvUserName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        tvUserEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        //Le ponemos un escuchador al botón de cambiar imagen para que pueda saltar cuando se pulsa
        tvTxtbtnChangeImage.setOnClickListener(v ->
        {
            requestPermissions();
        });

        //Añadimos este método para asegurarnos que cuando se pulse atrás nos cierre esta activity,
        //para que no consuma recursos inncesariamente
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed()
            {
                finish();
            }
        });
    }

    /**
     *  Creamos un objeto como final para gestionar la subida de una imagen
     * */

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult
            (new ActivityResultContracts.StartActivityForResult(), result ->
            {
                //Si se tienen permisos en la app para subir imágenes
                if(result.getResultCode() == RESULT_OK && result.getData() != null)
                {
                    //Se recoge la uri de la imagen, se pone en el imageview y se sube a la bdd
                    userImageUri = result.getData().getData();
                    Glide.with(this).load(userImageUri)
                            .placeholder(R.drawable.ic_event_list)
                            .transform(new CircleCrop())
                            .into(ivUserImage);
                    uploadImage();
                    uploadImage();
                }
            });

    /**
     * Método para comprobar si se tienen los permisos para cargar y subir imágenes
     * En caso de que no, se le piden al usuario explicitamente
     * */

    private void requestPermissions()
    {
        //Para Android 13 y versiones superiores
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            //Si no se tiene permisos, se piden
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]
                        {Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE);
            }

            //Si se tienen, se abre el seleccionador de ficheros
            else
            {
                openFileChooser();
            }
        }

        //Para versiones anteriores a Android 13
        else
        {
            //Si no se tiene permisos, se piden
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
            }

            //Si se tienen, se abre el seleccionador de ficheros
            else
            {
                openFileChooser();
            }
        }
    }

    /**
     * Sobreescritura del método para comprobar los resultados de los permisos requeridos
     * */

    @Override
    public void onRequestPermissionsResult(int requestCode
            , @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Si el código de permiso coincide con el requerido
        if(requestCode == REQUEST_CODE)
        {
            //Si se ha decidido dar permisos de lectura
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //Se abre el selector de imágenes
                openFileChooser();
            }

            //Si no se manda un Toast indicando que no se han concedido
            else
            {
                Toast.makeText(this, R.string.permissiondenied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Método para abrir el selector de imágenes
     * */

    private void openFileChooser()
    {
        //Creamos un intent
        Intent intent = new Intent();

        //Le setteamos el tipo de selector que queremos que sea
        intent.setType("image/*");

        //Le setteamos que sea un selector
        intent.setAction(Intent.ACTION_GET_CONTENT);

        //le decimos al lanzador que lance el intent
        pickImageLauncher.launch(intent);
    }

    /**
     * Método para poder subir una imagen a la base de datos
     * */

    private void uploadImage()
    {
        //Si la uri de la imagen no es nula
        if(userImageUri != null)
        {
            //Se abre una referencia a la base de datos y en la carpeta de usuarios
            //con el id del usuario se guarda su foto de perfil
            StorageReference fileStorageReference = FirebaseStorage.getInstance().getReference
                    ("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid()
                    + "/profile.jpg");

            //Le incluimos un escuchador de eventos para que cuando se complete la tarea
            //de subir la imagen se cambie en la base de FirebaseAuth y mande un Toast informativo
            //si falla se manda un Toast indicándolo también
            fileStorageReference.putFile(userImageUri).addOnSuccessListener(taskSnapshot ->
            {
               fileStorageReference.getDownloadUrl().addOnSuccessListener(uri ->
               {
                  updateProfile(uri.toString());
               });

               Toast.makeText(this, R.string.uploadsuccess,
                       Toast.LENGTH_SHORT).show();

            }).addOnFailureListener(e ->
            {
                Toast.makeText(this, R.string.uploadfailed + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
        }

        //Si no se ha seleccionado una imagen se indica con un Toast
        else
        {
            Toast.makeText(this, R.string.imagenotselected,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Método para poder actualizar la base de datos de usuarios con la imagen de usuario
     * Se actualiza tnato la FirebaseAuth como la FirebaseDatabase
     *
     * @param imageUri La dirección de la imagen
     * */

    private void updateProfile(String imageUri)
    {
        //Cogemos el usuario actual de la base de datos
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //Hacemos una petición de cambio para su foto de perfil
        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(imageUri)).build();

        //Si el usuario seleccionado no es nulo
        if(user != null)
        {
            //Se lanza la petición de actualizar su perfil con la petición creada
            //y se le asigna un escuchador para cuando se complete la tarea
            user.updateProfile(profileUpdate).addOnCompleteListener(task ->
            {
                //Si la tarea se completa satisfactoriamente
               if(task.isSuccessful())
               {
                   //cogemos la referencia de nuestra base de datos
                   DatabaseReference databaseReference = FirebaseDatabase
                           .getInstance().getReference();

                   //Le añadimos al usuario el hijo imageUrl con la url de la imágen
                   databaseReference.child("users").child(user.getUid())
                           .child("imageUrl").setValue(imageUri);

                   //Mandamos un Toast indicándolo
                   Toast.makeText(this, R.string.updatedprofile, Toast.LENGTH_SHORT).show();

               }

               //Si no se puede lo que se hace es indicarlo en un Toast
               else
               {
                   Toast.makeText(this, R.string.updateprofileerror,
                           Toast.LENGTH_SHORT).show();
               }
            });
        }
    }

}