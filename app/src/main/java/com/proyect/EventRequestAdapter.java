package com.proyect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EventRequestAdapter extends RecyclerView.Adapter<EventRequestViewHolder>
{
    private ArrayList<EventRequest> eventRequests;
    private OnRequestActionListener listener;

    public EventRequestAdapter(ArrayList<EventRequest> eventRequests, OnRequestActionListener listener)
    {
        this.eventRequests = eventRequests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_request, parent, false);

        return new EventRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventRequestViewHolder holder, int position)
    {
        EventRequest eventRequest = eventRequests.get(position);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("events").child(eventRequest.getEventId())
                .addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        String eventName = snapshot.child("name").getValue(String.class);

                        holder.tvEventName.setText(eventName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        holder.tvEventName.setText("Loading error");
                    }
                });

        holder.btnAcccept.setOnClickListener(l -> listener.onAccept(eventRequest.getEventId()));
        holder.btnReject.setOnClickListener(l -> listener.onReject(eventRequest.getEventId()));
    }

    @Override
    public int getItemCount()
    {
        return eventRequests.size();
    }

    public interface OnRequestActionListener
    {
        void onAccept(String eventId);
        void onReject(String eventId);
    }
}
