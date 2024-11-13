package com.proyect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EventViewerActivity extends AppCompatActivity
{
    RecyclerView rv_users;
    ArrayList<User> registeredUsers;
    FriendsAdapter adapter;

    TextView tvEventName;
    TextView tvEventData;
    ImageView ivEventImage;

    FloatingActionButton fbAddFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_viewer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvEventName = findViewById(R.id.tv_event_name);
        tvEventData = findViewById(R.id.tv_event_data);
        ivEventImage = findViewById(R.id.iv_event_image);

        fbAddFriends = findViewById(R.id.fb_addFriends);

        Intent intent = getIntent();

        String eventId = intent.getStringExtra("event_id");
        String eventName = intent.getStringExtra("event_name");
        String eventData = intent.getStringExtra("event_data");
        String eventImage = intent.getStringExtra("event_image");

        tvEventName.setText(eventName);
        tvEventData.setText(eventData);

        Glide.with(this)
                .load(eventImage)
                .placeholder(R.drawable.ic_event_list)
                .transform(new CircleCrop())
                .into(ivEventImage);

        rv_users = findViewById(R.id.rv_registered_users);

        rv_users.setLayoutManager(new LinearLayoutManager(this));

        registeredUsers = new ArrayList<User>();

        adapter = new FriendsAdapter(registeredUsers);

        rv_users.setAdapter(adapter);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        fbAddFriends.setOnClickListener(l ->
        {
            Intent intentInvite = new Intent(this, EventInviterActivity.class);

            intentInvite.putExtra("event_id", eventId);
            intentInvite.putExtra("event_name", eventName);

            startActivity(intentInvite);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {

            @Override
            public void handleOnBackPressed()
            {
                finish();
            }
        });

        reference.child("events").child(eventId).child("registeredUsers")
                .addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        registeredUsers.clear();

                        for(DataSnapshot dataSnapshot: snapshot.getChildren())
                        {
                            String userId = dataSnapshot.getKey();

                            reference.child("users").child(userId)
                                    .addListenerForSingleValueEvent(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot)
                                        {
                                            User user = snapshot.getValue(User.class);

                                            if(user != null)
                                            {
                                                registeredUsers.add(user);
                                                adapter.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error)
                                        {
                                            Log.println(Log.INFO, "Info", error.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        Log.println(Log.INFO, "Info", error.getMessage());
                    }
                });
    }
}