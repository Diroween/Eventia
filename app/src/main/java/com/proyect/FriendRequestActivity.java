package com.proyect;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

/**
 * Actividad que sirve para aceptar y rechazar peticiones de amistad
 *
 * *-Yosef-* Solo falta la forma de rechazar las peticiones, que no me da la vida ajajaja
 * */

public class FriendRequestActivity extends AppCompatActivity implements FriendRequestAdapter.OnFriendRequestActionListener
{
    RecyclerView rvFriends;
    FriendRequestAdapter friendRequestAdapter;
    ArrayList<FriendRequest> friendRequests;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_friend_request);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvFriends = findViewById(R.id.rv_friends);
        rvFriends.setLayoutManager(new LinearLayoutManager(this));

        friendRequests = new ArrayList<FriendRequest>();

        friendRequestAdapter = new FriendRequestAdapter(friendRequests, this);

        rvFriends.setAdapter(friendRequestAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("users").child(FirebaseAuth.getInstance().getUid()).child("friend_requests")
                .addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        friendRequests.clear();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            FriendRequest request = dataSnapshot.getValue(FriendRequest.class);
                            friendRequests.add(request);
                        }
                        friendRequestAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        Toast.makeText(getApplicationContext(), "no se han podido cargar datos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onAccept(String userId)
    {
        acceptFriendRequest(FirebaseAuth.getInstance().getUid(), userId);
    }

    @Override
    public void onReject(String userId)
    {

    }

    private void acceptFriendRequest(String currentUserId, String targetUserId)
    {
        DatabaseReference database = databaseReference.child("users")
                .child(FirebaseAuth.getInstance().getUid()).child("friend_requests")
                .child(targetUserId);

        database.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.exists())
                {
                    String name = snapshot.child("name").getValue(String.class);

                    User friend = new User(targetUserId, name);

                    databaseReference.child("users").child(currentUserId)
                            .child("friends").child(targetUserId)
                            .setValue(friend);
                    databaseReference.child("users").child(targetUserId)
                            .child("friends").child(currentUserId)
                            .setValue(new User(currentUserId, FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));

                    database.removeValue();

                    for (int i = 0; i < friendRequests.size(); i++)
                    {
                        if (friendRequests.get(i).getFrom().equals(targetUserId))
                        {
                            friendRequests.remove(i);
                            friendRequestAdapter.notifyItemRemoved(i);
                            break;
                        }
                    }

                    Toast.makeText(getApplicationContext(), "Amigo aceptado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(getApplicationContext(), "Error al aceptar amigo", Toast.LENGTH_SHORT).show();
            }


        });
    }

}