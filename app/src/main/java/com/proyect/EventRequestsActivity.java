package com.proyect;

import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EventRequestsActivity extends AppCompatActivity
{
    RecyclerView rvEventRequests;
    EventRequestAdapter eventRequestAdapter;
    ArrayList<EventRequest> eventRequests;
    DatabaseReference databaseReference;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_requests);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvEventRequests = findViewById(R.id.rv_events);

        rvEventRequests.setLayoutManager(new LinearLayoutManager(this));

        eventRequests = new ArrayList<EventRequest>();

        eventRequestAdapter = new EventRequestAdapter(eventRequests,
                new EventRequestAdapter.OnRequestActionListener()
                {
                    @Override
                    public void onAccept(String eventId)
                    {
                        acceptEventRequest(eventId);
                    }

                    @Override
                    public void onReject(String eventId)
                    {
                        declineEventRequest(eventId);
                    }
        });

        rvEventRequests.setAdapter(eventRequestAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        loadEventRequests();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {

            @Override
            public void handleOnBackPressed()
            {
                finish();
            }
        });
    }

    private void loadEventRequests()
    {
        databaseReference.child("users").child(currentUser.getUid())
                .child("eventsRequests")
                .addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        eventRequests.clear();

                        for(DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            String eventId = dataSnapshot.getKey();
                            String status = dataSnapshot.getValue(String.class);

                            eventRequests.add(new EventRequest(eventId, status));
                        }

                        eventRequestAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        //Toast.makeText(EventRequestsActivity.this,
                                //"Failed to register for event", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void acceptEventRequest(String eventId)
    {
        DatabaseReference userRef = databaseReference.child("users")
                .child(currentUser.getUid());

        DatabaseReference eventRef = databaseReference.child("events").child(eventId)
                .child("registeredUsers");

        eventRef.child(currentUser.getUid()).setValue(true).addOnCompleteListener(
                task ->
                {
                    if (task.isSuccessful())
                    {
                        userRef.child("eventsRequests").child(eventId).removeValue()
                                .addOnCompleteListener(t ->
                                {
                                    if(t.isSuccessful())
                                    {
                                        Toast.makeText(EventRequestsActivity.this,
                                                "Te has unido al evento", Toast.LENGTH_SHORT).show();
                                        loadEventRequests();
                                    }
                                    else
                                    { Toast.makeText(EventRequestsActivity.this,
                                            "Failed to remove event request",
                                            Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                    else
                    {
                        Toast.makeText(EventRequestsActivity.this,
                            "Failed to register for event", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void declineEventRequest(String eventId)
    {
        DatabaseReference userRef = databaseReference.child("users")
            .child(currentUser.getUid()); userRef.child("eventsRequests")
            .child(eventId).removeValue()
            .addOnCompleteListener(task ->
            {
                if (task.isSuccessful())
                { Toast.makeText(EventRequestsActivity.this
                        , "Event request declined", Toast.LENGTH_SHORT).show();

                    loadEventRequests();

                }
                else
                {
                    Toast.makeText(EventRequestsActivity.this
                            , "Failed to decline event request", Toast.LENGTH_SHORT).show();
                }
            });
    }
}