package com.proyect;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendEventListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendEventListFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    RecyclerView rvEvents;
    EventListAdapter listAdapter;
    ArrayList<Event> events;
    DatabaseReference databaseReference;

    public FriendEventListFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendEventListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendEventListFragment newInstance(String param1, String param2) {
        FriendEventListFragment fragment = new FriendEventListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friend_event_list, container, false);

        rvEvents = view.findViewById(R.id.rv_event_list);
        rvEvents.setLayoutManager(new LinearLayoutManager(view.getContext()));

        events = new ArrayList<Event>();

        listAdapter = new EventListAdapter(events);

        rvEvents.setAdapter(listAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("events");

        String currentUserId = FirebaseAuth.getInstance().getUid();

        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                events.clear();

                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Event event = dataSnapshot.getValue(Event.class);

                    if(event != null && dataSnapshot.child("registeredUsers").hasChild(currentUserId))
                    {
                        events.add(event);
                    }
                }

                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(view.getContext(), "Error al cargar eventos", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}