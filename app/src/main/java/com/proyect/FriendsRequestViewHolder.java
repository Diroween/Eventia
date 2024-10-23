package com.proyect;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FriendsRequestViewHolder extends RecyclerView.ViewHolder
{
    TextView tvName;
    Button btnAccept;
    Button btnReject;

    public FriendsRequestViewHolder(@NonNull View itemView)
    {
        super(itemView);

        tvName = itemView.findViewById(R.id.tv_name);
        btnAccept = itemView.findViewById(R.id.btn_accept);
        btnReject = itemView.findViewById(R.id.btn_reject);
    }
}
