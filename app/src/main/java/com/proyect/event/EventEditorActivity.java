package com.proyect.event;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.proyect.R;
import com.proyect.notification.NotificationHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EventEditorActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 2;
    TextView tvEventDate;
    TextView tvEventTitle;
    EditText etEventName;
    EditText etEventPlace;
    TextView tvEventHour;
    ImageView ivEventImage;
    Button btnEditEvent;
    private Uri eventImageUri;
    private Event event;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult
            (new ActivityResultContracts.StartActivityForResult(), result ->
            {
                //Si se tienen permisos en la app para subir imágenes
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    //Se recoge la uri de la imagen, se pone en el imageview y se sube a la bdd
                    eventImageUri = result.getData().getData();
                    Glide.with(this).load(eventImageUri)
                            .placeholder(R.drawable.ic_event_list)
                            .transform(new CircleCrop())
                            .into(ivEventImage);
                    uploadImage();
                }
            });
    private String dateString;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_creation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Se fuerza a la aplicación a mostrarse en vertical
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = getIntent();

        //Recogemos los datos del evento que pasamos desde EventViewerActivity
        String eventName = intent.getStringExtra("event_name");
        String eventId = intent.getStringExtra("event_id");
        String eventPlace = intent.getStringExtra("event_place");
        String eventDate = intent.getStringExtra("event_date");
        String eventHour = intent.getStringExtra("event_hour");
        String eventImage = intent.getStringExtra("event_image");

        ivEventImage = findViewById(R.id.iv_event_image);
        ivEventImage.setContentDescription(eventImage);

        tvEventTitle = findViewById(R.id.tv_title);
        tvEventTitle.setText(R.string.eventedit);

        btnEditEvent = findViewById(R.id.btn_create_event);
        btnEditEvent.setText(R.string.savechanges);

        View view = findViewById(R.id.main);
        view.setBackgroundResource(R.drawable.edit_bckground);

        etEventName = findViewById(R.id.et_event_name);
        etEventName.setText(eventName);

        etEventPlace = findViewById(R.id.et_event_place);
        etEventPlace.setText(eventPlace);

        tvEventDate = findViewById(R.id.tv_event_date);
        tvEventDate.setText(eventDate);

        tvEventHour = findViewById(R.id.tv_event_hour);
        tvEventHour.setText(eventHour);

        Glide.with(this)
                .load(eventImage)
                .placeholder(R.drawable.ic_event_list)
                .transform(new CircleCrop())
                .into(ivEventImage);

        // Si el evento cuenta con una imagen subida,
        // establecemos su propia URI para cargarlo en el ImageView del editor
        eventImageUri = Uri.parse(eventImage);

        dateString = eventDate;

        String[] dpdDate = eventDate.split("-");

        tvEventDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        EventEditorActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

                                String date = String.format("%02d/%02d/%02d", i2, i1 + 1, i);
                                tvEventDate.setText(date);
                                dateString = String.format("%02d-%02d-%02d", i, i1 + 1, i2);
                            }
                        }, Integer.parseInt(dpdDate[0]), Integer.parseInt(dpdDate[1]) - 1,
                        Integer.parseInt(dpdDate[2])
                );
                datePickerDialog.show();
            }
        });

        tvEventHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Creamos un objeto timepickerdialog con el contexto
                //y le ponemos un escuchador de eventos
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        EventEditorActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            /**
                             * Método para dar funcionalidad a la selección de una hora
                             * */
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                //le seleccionamos un formato para la hora y minutos
                                String hour = String.format("%02d:%02d", i, i1);

                                //ponemos la hora en el textview
                                tvEventHour.setText(hour);

                            }

                        } //Indicamos que queremos que sea una hora en formato 24 horas, establecemos la hora/minutos del evento
                        , Integer.parseInt(eventHour.split(":", 2)[0]),
                        Integer.parseInt(eventHour.split(":", 2)[1]),
                        true);

                //Poppeamos el timepicker
                timePickerDialog.show();
            }
        });

        //Creamos una referencia a la base de datos
        databaseReference = FirebaseDatabase.getInstance().getReference();

        //Inicializamos el evento
        event = new Event();

        //le ponemos su id
        event.setId(eventId);

        //Creamos una instancia de calendario
        Calendar calendar = Calendar.getInstance();

        //En el try:
        //hacemos que la fecha pase del String recogido a Date
        //ponemos en el calendario el día seleccionado pasado
        //pasamos ese día a un formato más visual para el usuario
        //ponemos en el campo para mostrar la fecha la del formato amigable
        //Si hay un error se recoge
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);

            String showDate = new SimpleDateFormat("dd/MM/yyyy").format(date);

            tvEventDate.setText(showDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        //Le ponemos un escuchador de click a la imagen
        //para que se pueda subir una imagen al pinchar en él
        ivEventImage.setOnClickListener(l ->
        {
            requestPermissions();
        });

        //Ponemos un escuchador de eventos para el botón de crear evento
        //si se han  introducido todos los campos necesarios, deja crear un evento
        btnEditEvent.setOnClickListener(l ->
        {
            //Si los campos a introducir están rellenos
            if (checkEmptyFields()) {
                //Si se ha subido una imagen
                if (eventImageUri != null) {
                    //pasamos a registrar el evento
                    saveEvent(eventImageUri.toString());
                }
                //Si no se ha subido una imagen
                else {
                    //inicializamos la uri con un string vacío
                    eventImageUri = Uri.parse("");

                    //Pasamos a registrar el evento
                    saveEvent(eventImageUri.toString());
                }

                //Cancelamos todos los trabajos programados para dicho evento
                NotificationHelper.cancelAllWorkRequests(event.getName());

                //Lanzamos todos los trabajos necesarios basándonos en la nueva fecha del evento
                //Esto principalmente es para que en caso de adelantar o atrasar un evento
                // no se quedasen trabajos antiguos activos o no se lanzasen trabajos nuevos.
                NotificationHelper.enqueueNotifications(getApplicationContext(), event, NotificationHelper.getSecondsUntilEvent(event));

            }

            //Si no se han completado todos los campos
            else {
                //Se manda un toast informativo indicando al incidencia
                Toast.makeText(this, R.string.completefields
                        , Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkEmptyFields() {
        return !etEventName.getText().toString().isEmpty()
                && !etEventPlace.getText().toString().isEmpty()
                && !tvEventDate.getText().toString().isEmpty()
                && !tvEventHour.getText().toString().isEmpty();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode
            , @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Si el código de permiso coincide con el requerido
        if (requestCode == REQUEST_CODE) {
            //Si se ha decidido dar permisos de lectura
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Se abre el selector de imágenes
                openFileChooser();
            }

            //Si no se manda un Toast indicando que no se han concedido
            else {
                Toast.makeText(this, R.string.permissiondenied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestPermissions() {
        //Para Android 13 y versiones superiores
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //Si no se tiene permisos, se piden
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]
                        {Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE);
            }

            //Si se tienen, se abre el seleccionador de ficheros
            else {
                openFileChooser();
            }
        }

        //Para versiones anteriores a Android 13
        else {
            //Si no se tiene permisos, se piden
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
            }

            //Si se tienen, se abre el seleccionador de ficheros
            else {
                openFileChooser();
            }
        }
    }

    private void openFileChooser() {
        //Creamos un intent
        Intent intent = new Intent();

        //Le setteamos el tipo de selector que queremos que sea
        intent.setType("image/*");

        //Le setteamos que sea un selector
        intent.setAction(Intent.ACTION_GET_CONTENT);

        //le decimos al lanzador que lance el intent
        pickImageLauncher.launch(intent);
    }

    private void uploadImage() {
        //Si la uri de la imagen no es nula
        if (eventImageUri != null && !eventImageUri.toString().contentEquals(ivEventImage.getContentDescription())) {
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
        else {
            Toast.makeText(this, R.string.imagenotselected,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Método para poder guardar un evento modificado
     */

    private void saveEvent(String imageUri) {
        //Setteamos el resto de datos del evento
        event.setName(etEventName.getText().toString());
        event.setPlace(etEventPlace.getText().toString());
        event.setDate(dateString);
        event.setHour(tvEventHour.getText().toString());
        event.setImage(imageUri);

        // Referencia al evento en la base de datos
        DatabaseReference eventReference = databaseReference.child("events")
                .child(event.getId());

        //Recogemos de la base de datos los usuarios ya registrados en el evento
        eventReference.child("registeredUsers").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //Guardamos todos los usuarios registrados en una lista temporal
                        //para ello iteramos tantas veces como usuarios haya
                        Map<String, Object> registeredUsersMap = new HashMap<>();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            registeredUsersMap.put(snapshot.getKey(), snapshot.getValue());
                        }

                        //Actualizamos el evento sobreescribiendo los datos anteriores
                        //Si no se ha podido actualizar se manda un toast informativo
                        eventReference.setValue(event).addOnCompleteListener(task ->
                        {
                            if (task.isSuccessful()) {
                                //Una vez se actualizado los datos añadimos los usuarios que ya estaban
                                //registrados anteriormente y mostramos un Toast informativo
                                eventReference.child("registeredUsers").updateChildren(registeredUsersMap)
                                        .addOnCompleteListener(task1 ->
                                        {
                                            if (task1.isSuccessful()) {
                                                Toast.makeText(EventEditorActivity.this,
                                                        R.string.eventedited,
                                                        Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(EventEditorActivity.this,
                                                        R.string.eventediterror,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(EventEditorActivity.this,
                                        R.string.eventediterror,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(EventEditorActivity.this,
                                R.string.eventloaderror,
                                Toast.LENGTH_SHORT).show();
                    }
                });

        //Se cierra la actividad
        finish();
    }


}