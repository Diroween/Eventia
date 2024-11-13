package com.proyect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EventInviterActivity extends AppCompatActivity
{
    FriendInviterAdapter friendsAdapter;
    RecyclerView rvFriends;
    ArrayList<User> friends;
    DatabaseReference databaseReference;
    User selectedFriend;

    Button btnInvite;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_inviter);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();

        String eventId = intent.getStringExtra("event_id");
        String eventName = intent.getStringExtra("event_name");

        rvFriends = findViewById(R.id.rv_friends);
        btnInvite = findViewById(R.id.btn_invite_friend);

        rvFriends.setLayoutManager(new LinearLayoutManager(this));

        friends = new ArrayList<User>();

        friendsAdapter = new FriendInviterAdapter(friends, friend->
        {
            selectedFriend = friend;
        });

        rvFriends.setAdapter(friendsAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        btnInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                DatabaseReference userEventRequestsRef = databaseReference.child("users")
                        .child(selectedFriend.getId()).child("eventsRequests");

                DatabaseReference eventRegisteredUsersRef = databaseReference.child("events")
                        .child(eventId).child("registeredUsers");

                //Verificamos si el usuario ya ha sido invitado al evento
                userEventRequestsRef.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if (snapshot.hasChild(eventId))
                        {
                            Toast.makeText(EventInviterActivity.this
                                    , "Este amigo ya ha sido invitado al evento"
                                    , Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            //Verificamos si el usuario ya está registrado en el evento
                            eventRegisteredUsersRef.addListenerForSingleValueEvent(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot)
                                {
                                    if (snapshot.hasChild(selectedFriend.getId()))
                                    {
                                        Toast.makeText(EventInviterActivity.this
                                                , "El usuario ya está registrado en el evento"
                                                , Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        //Añadimos una petición del evento
                                        userEventRequestsRef.child(eventId)
                                                .setValue("pending").addOnCompleteListener(task ->
                                                {
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(EventInviterActivity.this
                                                        , "Enviada petición de registro al evento: "
                                                                + eventName, Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                Toast.makeText(EventInviterActivity.this
                                                        , "No se pudo enviar la petición"
                                                        , Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error)
                                {
                                    Toast.makeText(EventInviterActivity.this
                                            , R.string.eventloaderror, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        Toast.makeText(EventInviterActivity.this
                                , R.string.loadeventserror, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        databaseReference.child("users").child(FirebaseAuth.getInstance().getUid())
                .child("friends").addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        friends.clear();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            String friendId = dataSnapshot.getKey();

                            databaseReference.child("users").child(friendId)
                                    .addListenerForSingleValueEvent(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot)
                                        {
                                            User friend = snapshot.getValue(User.class);

                                            if (friend != null && friends.add(friend))
                                            {
                                                friendsAdapter.notifyDataSetChanged();
                                            }
                                        }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error)
                                {
                                    Toast.makeText(EventInviterActivity.this,
                                            R.string.nodataload, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        Toast.makeText(EventInviterActivity.this,
                                R.string.nodataload, Toast.LENGTH_SHORT).show();
                    }
                });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {

            @Override
            public void handleOnBackPressed()
            {
                finish();
            }
        });

    }
}