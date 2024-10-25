package com.proyect;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FriendSearcherActivity extends AppCompatActivity
{
    private EditText etUsername;
    private Button btnSearch;
    private Button btnSendRequest;
    private RecyclerView rvUsers;
    private FriendSearcherAdapter searcherAdapter;
    private ArrayList<User> users;
    private DatabaseReference databaseReference;
    private User selectedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_friend_searcher);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUsername = findViewById(R.id.et_username);
        btnSearch = findViewById(R.id.btn_search);
        btnSendRequest = findViewById(R.id.btn_sendRequest);
        rvUsers = findViewById(R.id.rv_users);

        rvUsers.setLayoutManager(new LinearLayoutManager(this));

        users = new ArrayList<User>();

        searcherAdapter = new FriendSearcherAdapter(users, user ->
        {
            selectedUser = user;
            btnSendRequest.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), "Usuario seleccionado: "
                    + user.getName(), Toast.LENGTH_SHORT).show();

        });

        rvUsers.setAdapter(searcherAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        btnSearch.setOnClickListener(v -> searchUsers(etUsername.getText().toString()));

        btnSendRequest.setOnClickListener(v ->
        {
            if(selectedUser != null)
            {
                sendFriendRequest(FirebaseAuth.getInstance().getUid(), selectedUser.getId());
            }
            else
            {
                Toast.makeText(getApplicationContext(), "No se ha elegido un usuario",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchUsers(String username)
    {
        databaseReference.child("users").orderByChild("name").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        users.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            User user = dataSnapshot.getValue(User.class);

                            if(user!=null)
                            {
                                users.add(user);
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(), "No se han encontrado usuarios",
                                        Toast.LENGTH_SHORT).show();

                                Log.d("FriendSearcher", "User es null en el DataSnapshot: " + dataSnapshot);
                            }

                        }
                        searcherAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        Toast.makeText(getApplicationContext(), "error al buscar usuarios",
                                Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void sendFriendRequest(String currentUserId, String targetUserId)
    {
        if(!currentUserId.equals(targetUserId))
        {
            DatabaseReference database = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(currentUserId).child("friends")
                    .child(targetUserId);

            database.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if(snapshot.exists())
                    {
                        Toast.makeText(getApplicationContext(), "Ya sois amigos", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        DatabaseReference dataref = FirebaseDatabase.getInstance().getReference()
                                .child("users").child(targetUserId).child("friend_requests")
                                .child(currentUserId);

                        dataref.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {
                                if(snapshot.exists())
                                {
                                    FriendRequest existingRequest = snapshot.getValue(FriendRequest.class);

                                    if(existingRequest != null && existingRequest.getStatus()
                                            .equals("pending"))
                                    {
                                        Toast.makeText(getApplicationContext(),
                                                "Ya has enviado una solicitud de amistad",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        FriendRequest request = new FriendRequest(currentUserId,
                                                FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                                                "pending");

                                        dataref.setValue(request).addOnCompleteListener(new OnCompleteListener<Void>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if(task.isSuccessful())
                                                {
                                                    Toast.makeText(getApplicationContext(),
                                                            "Solicitud de amistad enviada",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                                else
                                                {
                                                    Toast.makeText(getApplicationContext(),
                                                            "Error al enviar la solicitud",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }
                                else
                                {
                                    FriendRequest request = new FriendRequest(currentUserId,
                                            FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                                            "pending");

                                    dataref.setValue(request).addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(getApplicationContext(),
                                                        "Solicitud de amistad enviada",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                Toast.makeText(getApplicationContext(),
                                                        "Error al enviar la solicitud",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error)
                            {
                                Toast.makeText(getApplicationContext(),
                                        "Error al comprobar la solicitud existente",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    Toast.makeText(getApplicationContext(),
                            "Error al comprobar si ya sois amigos",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No puedes ser tu propio amigo",
                    Toast.LENGTH_SHORT).show();
        }

    }
}