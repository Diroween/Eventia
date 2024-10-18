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

public class NotesActivity extends AppCompatActivity implements View.OnClickListener
{

    Button btnExit;
    Button btnSave;
    EditText etTitle;
    EditText etBody;

    String noteName;
    String noteBody;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
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

        btnSave = findViewById(R.id.btn_save);
        btnExit = findViewById(R.id.btn_exit);
        etTitle = findViewById(R.id.et_note_title);
        etBody = findViewById(R.id.et_note_body);

        Intent intent = getIntent();

        if(intent != null && intent.getExtras() != null)
        {
            noteName = intent.getStringExtra("note_name");
            noteBody = intent.getStringExtra("note_body");

            etTitle.setText(noteName);
            etBody.setText(noteBody);
        }

        btnSave.setOnClickListener(this);
        btnExit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        String noteStore = "noteStore";

        if(view.getId() == R.id.btn_save)
        {
            SharedPreferences sharedNotes = view.getContext().getSharedPreferences(noteStore, Context.MODE_PRIVATE);

            sharedNotes.edit().putString(etTitle.getText().toString(), etBody.getText().toString()).apply();

            Toast.makeText(view.getContext(), getString(R.string.savednote), Toast.LENGTH_SHORT).show();
        }

        else
        {
            finish();
        }
    }
}