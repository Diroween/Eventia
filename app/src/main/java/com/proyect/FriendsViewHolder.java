package com.proyect;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FriendsViewHolder extends RecyclerView.ViewHolder
{
    TextView tvName;
    ImageView ivUser;

    public FriendsViewHolder(@NonNull View itemView)
    {
        super(itemView);

        tvName = itemView.findViewById(R.id.tv_name);
        ivUser = itemView.findViewById(R.id.iv_user);
    }
}
