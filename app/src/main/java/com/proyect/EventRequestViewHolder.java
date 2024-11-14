package com.proyect;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ViewHolder personalizado para el EventRequestAdapter
 * */

public class EventRequestViewHolder extends RecyclerView.ViewHolder
{
    TextView tvEventName;
    TextView btnAcccept;
    TextView btnReject;

    public EventRequestViewHolder(@NonNull View itemView)
    {
        super(itemView);

        tvEventName = itemView.findViewById(R.id.tv_name);
        btnAcccept = itemView.findViewById(R.id.btn_accept);
        btnReject = itemView.findViewById(R.id.btn_reject);
    }

}
