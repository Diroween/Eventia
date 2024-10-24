package com.proyect;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FriendSearcherViewHolder extends RecyclerView.ViewHolder
{
    TextView tvName;
    TextView tvFriendId;

    public FriendSearcherViewHolder(@NonNull View itemView)
    {
        super(itemView);

        tvName = itemView.findViewById(R.id.tv_name);
        tvFriendId = itemView.findViewById(R.id.tv_friendId);
    }
}
