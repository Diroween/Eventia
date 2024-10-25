package com.proyect;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Fragment friends creado para contener el registro de amistades de la app
 */
public class FriendsFragment extends Fragment
{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RecyclerView rvFriends;
    private FriendsAdapter friendsAdapter;
    private ArrayList<User> friends;
    private SwipeRefreshLayout srlFriends;
    private DatabaseReference databaseReference;

    FloatingActionButton fbFriendRequests;
    FloatingActionButton fbAddFriend;

    public FriendsFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsFragment newInstance(String param1, String param2)
    {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /**
     * Método necesario para el correcto funcionamiento del fragment
     * */

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        rvFriends = view.findViewById(R.id.rv_friends);

        rvFriends.setLayoutManager(new LinearLayoutManager(view.getContext()));

        friends = new ArrayList<User>();

        friendsAdapter = new FriendsAdapter(friends);

        rvFriends.setAdapter(friendsAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference();;

        databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("friends").addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        friends.clear();
                        for(DataSnapshot dataSnapshot: snapshot.getChildren())
                        {
                            User friend = dataSnapshot.getValue(User.class);

                            friends.add(friend);
                        }
                        friendsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        Toast.makeText(view.getContext(), "No se han podido cargar amigos", Toast.LENGTH_SHORT).show();
                    }
                });

        fbFriendRequests = view.findViewById(R.id.fb_requests);

        fbFriendRequests.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(view.getContext(), FriendRequestActivity.class);

                startActivity(i);
            }
        });

        fbAddFriend = view.findViewById(R.id.fb_addFriends);

        fbAddFriend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(view.getContext(), FriendSearcherActivity.class);

                startActivity(i);
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }


}