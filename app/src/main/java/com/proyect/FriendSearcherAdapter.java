package com.proyect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FriendSearcherAdapter extends RecyclerView.Adapter<FriendSearcherViewHolder>
{
    private ArrayList<User> users;
    private OnUserClickListener clickListener;

    public FriendSearcherAdapter(ArrayList<User> users, OnUserClickListener clickListener)
    {
        this.users = users;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public FriendSearcherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_searcher,
                parent, false);

        return new FriendSearcherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendSearcherViewHolder holder, int position)
    {
        User user = users.get(position);

        holder.tvName.setText(user.getName());
        holder.tvFriendId.setText(user.getId());

        holder.itemView.setOnClickListener(v -> clickListener.onUserClick(user));
    }

    @Override
    public int getItemCount()
    {
        return users.size();
    }

    public interface OnUserClickListener
    {
        void onUserClick(User user);
    }
}
