package com.proyect.event;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.proyect.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Clase para crear un evento en la aplicación y que se registre en la base de datos
 * */

public class EventCreationActivity extends AppCompatActivity
{
    /**
     * Creamos tantas variables de clase como elementos queremos tratar
     * durante la creación del evento:
     * Un textview para mostrar y cargar la fecha
     * Dos campos de texto para el nombre y el lugar del evento
     * Un textview para lanzar el selector de horas cuando se le pulse
     * Un visualizador de imagen que vamos a usar para cargar y mostrar la imagen del evento
     * */

    TextView tvEventDate;
    EditText etEventName;
    EditText etEventPlace;
    TextView tvEventHour;
    ImageView ivEventImage;
    Button btnCreateEvent;

    /**
     * Creamos varias variables de clase necesarias para el funcionamiento de las subidas a la bdd
     * El código necesario para pedir los permisos al usuario
     * La dirección donde estará la imagen
     * Un evento, que será el que luego se subirá a la bdd
     * El día pasado como extra, que es el día seleccionado por el usuario
     * Una referencia a la base de datos
     * */

    private static final int REQUEST_CODE = 2;
    private Uri eventImageUri;
    private Event event;
    private String dateString;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Métodos necesarios para que la cativity se muestre correctamente en pantalla
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_creation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Se fuerza a la aplicación a mostrarse en vertical
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Asignamos las variables a sus elementos en pantalla
        tvEventHour = findViewById(R.id.tv_event_hour);
        tvEventDate = findViewById(R.id.tv_event_date);
        etEventName = findViewById(R.id.et_event_name);
        etEventPlace = findViewById(R.id.et_event_place);
        ivEventImage = findViewById(R.id.iv_event_image);
        btnCreateEvent = findViewById(R.id.btn_create_event);

        //Creamos un intent
        Intent intent = getIntent();

        //Recogemos el dato del día seleccionado
        dateString = intent.getStringExtra("date");

        //Creamos una instancia de calendario
        Calendar calendar = Calendar.getInstance();

        //En el try:
        //hacemos que la fecha pase del String recogido a Date
        //ponemos en el calendario el día seleccionado pasado
        //pasamos ese día a un formato más visual para el usuario
        //ponemos en el campo para mostrar la fecha la del formato amigable
        //Si hay un error se recoge
        try
        {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);

            calendar.setTime(date);

            String showDate = new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime());

            tvEventDate.setText(showDate);
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }

        //Le ponemos un escuchador de click al textview de la hora
        tvEventHour.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Creamos un objeto timepickerdialog con el contexto
                //y le ponemos un escuchador de eventos
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        EventCreationActivity.this,
                        new TimePickerDialog.OnTimeSetListener()
                {
                    /**
                     * Método para dar funcionalidad a la selección de una hora
                     * */
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1)
                    {
                        //le seleccionamos un formato para la hora y minutos
                        String hour = String.format("%02d:%02d", i, i1);

                        //ponemos la hora en el textview
                        tvEventHour.setText(hour);
                    }

                } //Indicamos que queremos que sea una hora en formato 24 horas
                , calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                        calendar.getInstance().get(Calendar.MINUTE),
                        true);

                //Poppeamos el timepicker
                timePickerDialog.show();
            }
        });

        //Creamos una referencia a la base de datos
        databaseReference = FirebaseDatabase.getInstance().getReference();

        //Hacemos que la base de datos nos genere un id aleatorio
        String eventId = databaseReference.push().getKey();

        //Inicializamos el evento
        event = new Event();

        //le ponemos su id
        event.setId(eventId);

        //Le ponemos un escuchador de click a la imagen
        //para que se pueda subir una imagen al pinchar en él
        ivEventImage.setOnClickListener(l ->
        {
            requestPermissions();
        });

        //Ponemos un escuchador de eventos para el botón de crear evento
        //si se han  introducido todos los campos necesarios, deja crear un evento
        btnCreateEvent.setOnClickListener(l ->
        {
            //Si los campos a introducir están rellenos
            if(checkEmptyFields())
            {
                //Si se ha subido una imagen
                if(eventImageUri != null)
                {
                    //pasamos a registrar el evento
                    saveEvent(eventImageUri.toString());
                }
                //Si no se ha subido una imagen
                else
                {
                    //inicializamos la uri con un string vacío
                    eventImageUri = Uri.parse("");

                    //Pasamos a registrar el evento
                    saveEvent(eventImageUri.toString());
                }

            }

            //Si no se han completado todos los campos
            else
            {
                //Se manda un toast informativo indicando al incidencia
                Toast.makeText(this, R.string.completefields
                        , Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Método para checkear que todos los campos necesarios han sido rellenados
    private boolean checkEmptyFields()
    {
        return !etEventName.getText().toString().isEmpty()
                && !etEventPlace.getText().toString().isEmpty()
                && !tvEventDate.getText().toString().isEmpty()
                && !tvEventHour.getText().toString().isEmpty();
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
                    eventImageUri = result.getData().getData();
                    Glide.with(this).load(eventImageUri)
                            .placeholder(R.drawable.ic_event_list)
                            .transform(new CircleCrop())
                            .into(ivEventImage);
                    uploadImage();
                }
            });


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
        if(eventImageUri != null)
        {
            //Se abre una referencia a la base de datos y en la carpeta de usuarios
            //con el id del usuario se guarda su foto de perfil
            StorageReference fileStorageReference = FirebaseStorage.getInstance().getReference
                    ("events/" + event.getId()
                            + "/eventImage.jpg");

            //Le incluimos un escuchador de eventos para que cuando se complete la tarea
            //de subir la imagen, se settea eventImageUri con la dirección de la bdd
            // y se mande un Toast informativo
            //si falla se manda un Toast indicándolo también
            fileStorageReference.putFile(eventImageUri).addOnSuccessListener(taskSnapshot ->
            {
                fileStorageReference.getDownloadUrl().addOnSuccessListener(uri ->
                {
                    eventImageUri = uri;

                    Toast.makeText(this, R.string.uploadsuccess,
                            Toast.LENGTH_SHORT).show();
                });

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
     * Método para poder actualizar la base de datos de eventos
     * con el nuevo evento creado
     *
     * @param imageUri La dirección de la imagen
     * */

    private void saveEvent(String imageUri)
    {
        //Cogemos el usuario actual de la base de datos
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //Setteamos el resto de datos del evento
        event.setName(etEventName.getText().toString());
        event.setPlace(etEventPlace.getText().toString());
        event.setDate(dateString);
        event.setHour(tvEventHour.getText().toString());
        event.setImage(imageUri);

        //Cogemos una referencia a la base de datos de eventos y ahijamos
        //el nuevo evento, usando un escuchador de eventos para cuando se realiza
        databaseReference.child("events").child(event.getId()).setValue(event)
                .addOnCompleteListener(task ->
                {
                    //Si la tarea se completa satisfactoriamente
                   if (task.isSuccessful())
                   {
                       //Nos metemos en el evento y ahijamos al usuario que lo crea
                       //como integrante del evento, se le añade un escuchador
                        databaseReference.child("events").child(event.getId())
                                .child("registeredUsers").child(user.getUid())
                                .setValue("admin").addOnCompleteListener(task1 ->
                                {
                                    //si se ha conseguido ahijar se manda un toast indicando
                                    //se he podido hacer el proceso satisfactoriamente
                                    if (task1.isSuccessful())
                                    {
                                        Toast.makeText(this,
                                                R.string.eventcreated,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    //Si no, se indica con un toast
                                    else
                                    {
                                        Toast.makeText(this,
                                                R.string.registererror,
                                                Toast.LENGTH_SHORT).show();
                                    }

                                });
                   }
                   //Si no se indica que ha habido un error al crear el evento
                   else
                   {
                       Toast.makeText(this,
                               R.string.createeventerror,
                               Toast.LENGTH_SHORT).show();
                   }
                });

        //Se cierra la actividad
        finish();
    }
}