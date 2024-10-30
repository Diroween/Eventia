package com.proyect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UserSettings extends AppCompatActivity
{
    /**
     * Declaramos variables de clase de los elementos:
     * La imagen del usuario
     * Su id, nombre y email
     * el textview que funcionará como botón
     * */

    ImageView ivUserImage;
    TextView tvUserId;
    TextView tvUserName;
    TextView tvUserEmail;
    TextView tvTxtbtnChangeImage;

    /**
     * Las variables que necesitamos para poder subir la foto y cargarla
     * */

    private static final int REQUEST_CODE = 2;
    private Uri userImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
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

        ivUserImage = findViewById(R.id.iv_user_image);
        tvUserId = findViewById(R.id.tv_user_id);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        tvTxtbtnChangeImage = findViewById(R.id.tv_txtbtn_change_image);

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

        tvUserId.setText(FirebaseAuth.getInstance().getCurrentUser().getUid());

        tvUserName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

        tvUserEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

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

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult
            (new ActivityResultContracts.StartActivityForResult(), result ->
            {
                if(result.getResultCode() == RESULT_OK && result.getData() != null)
                {
                    userImageUri = result.getData().getData();
                    ivUserImage.setImageURI(userImageUri);
                    uploadImage();
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openFileChooser();
                } else {
                    Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
                }
            });

    private void requestPermissions()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            // Para Android 13 y versiones superiores
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]
                        {Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE);
            }
            else
            {
                openFileChooser();
            }
        }
        else
        {
            // Para versiones anteriores
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
            else
            {
                openFileChooser();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode
            , @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                openFileChooser();
            }
            else
            {
                Toast.makeText(this, R.string.permissiondenied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openFileChooser()
    {
        Intent intent = new Intent();

        intent.setType("image/*");

        intent.setAction(Intent.ACTION_GET_CONTENT);

        pickImageLauncher.launch(intent);
    }

    private void uploadImage()
    {
        if(userImageUri != null)
        {
            StorageReference fileStorageReference = FirebaseStorage.getInstance().getReference
                    ("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid()
                    + "/profile.jpg");

            fileStorageReference.putFile(userImageUri).addOnSuccessListener(taskSnapshot ->
            {
               fileStorageReference.getDownloadUrl().addOnSuccessListener(uri ->
               {
                  updateProfile(uri.toString());
               });

               Toast.makeText(this, "Se ha subido el fichero satisfactoriamente",
                       Toast.LENGTH_SHORT).show();

            }).addOnFailureListener(e ->
            {
                Toast.makeText(this, "La subida ha fallado: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
        }
        else
        {
            Toast.makeText(this, "No se ha seleccionado imagen",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfile(String imageUri)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(imageUri)).build();

        if(user != null)
        {
            user.updateProfile(profileUpdate).addOnCompleteListener(task ->
            {
               if(task.isSuccessful())
               {
                   Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
               }
               else
               {
                   Toast.makeText(this, "No se ha podido actualizar", Toast.LENGTH_SHORT).show();
               }
            });
        }
    }

}