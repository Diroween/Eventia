package com.proyect;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener
{

    EditText etUsername;
    EditText etPassword;
    Button btnLogin;
    TextView tvSignUpTxtBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

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
        btnLogin = (Button)findViewById(R.id.btn_login);
        tvSignUpTxtBtn = (TextView)findViewById(R.id.tv_signup_txtBtn);

        btnLogin.setOnClickListener(this);
        tvSignUpTxtBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        Intent i;

        if(id == R.id.btn_login)
        {
            if(!etUsername.getText().toString().isEmpty() && !etPassword.getText().toString().isEmpty())
            {
                i = new Intent(getApplicationContext(), MainActivity.class);

                i.putExtra("noteStore", etUsername.getText().toString());

                startActivity(i);

                finish();
            }
            else
            {
                Toast.makeText(getApplicationContext(), R.string.loginNoText, Toast.LENGTH_SHORT).show();
            }
        }

        if(id == R.id.tv_signup_txtBtn)
        {
            i = new Intent(getApplicationContext(), SignupActivity.class);

            startActivity(i);

            finish();
        }

    }
}