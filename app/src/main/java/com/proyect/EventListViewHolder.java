package com.proyect;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EventListViewHolder extends RecyclerView.ViewHolder
{

    TextView tvEventName;
    TextView tvEventDate;
    TextView tvEventPlace;
    ImageView ivEventImage;

    public EventListViewHolder(@NonNull View itemView)
    {
        super(itemView);

        tvEventDate = itemView.findViewById(R.id.tv_event_date);
        tvEventName = itemView.findViewById(R.id.tv_event_name);
        tvEventPlace = itemView.findViewById(R.id.tv_event_place);
        ivEventImage = itemView.findViewById(R.id.iv_event_image);
    }
}
