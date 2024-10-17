package com.proyect;

import android.content.Intent;
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

public class SignupActivity extends AppCompatActivity implements View.OnClickListener
{

    EditText etUsername;
    EditText etPassword;
    EditText etConfirmPass;
    Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Se fuerza a la aplicación a mostrarse en vertical
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        etUsername = (EditText)findViewById(R.id.et_username);
        etPassword = (EditText)findViewById(R.id.et_password);
        etConfirmPass = (EditText)findViewById(R.id.et_confirm_pass);
        btnSignup = (Button)findViewById(R.id.btn_signup);

        btnSignup.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        Intent i;

        if(!etUsername.getText().toString().isEmpty() &&
                !etPassword.getText().toString().isEmpty() && !etConfirmPass.toString().isEmpty())
        {
            if(etPassword.getText().toString().equals(etConfirmPass.getText().toString()))
            {
                i = new Intent(getApplicationContext(), LoginActivity.class);

                startActivity(i);

                finish();
            }
            else
            {
                Toast.makeText(getApplicationContext(), R.string.passdontmatch, Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), R.string.loginNoText, Toast.LENGTH_SHORT).show();
        }
    }
}