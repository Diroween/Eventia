package com.proyect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class EventListAdapter extends RecyclerView.Adapter<EventListViewHolder>
{
    ArrayList<Event> events;

    public EventListAdapter (ArrayList<Event> events)
    {
        this.events = events;
    }

    @NonNull
    @Override
    public EventListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_item, parent, false);

        return new EventListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventListViewHolder holder, int position)
    {
        Event event = events.get(position);

        holder.tvEventName.setText(event.getName());
        holder.tvEventDate.setText(event.getDate());
        holder.tvEventPlace.setText(event.getPlace());

        if(!event.getImage().isEmpty())
        {
            Glide.with(holder.itemView.getContext())
                    .load(event.getImage())
                    .placeholder(R.drawable.ic_event_list)
                    .into(holder.ivEventImage);
        }
    }

    @Override
    public int getItemCount()
    {
        return events.size();
    }
}
