package com.proyect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsViewHolder>
{
    private ArrayList<Friend> friends;

    public FriendsAdapter(ArrayList<Friend> friends)
    {
        this.friends = friends;
    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);

        return new FriendsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position)
    {
        Friend friend = friends.get(position);

        holder.tvName.setText(friend.getName());

        //hay que implementar que un usuario tenga una imagen de perfil
        //tiene que ser un submenú dentro de su modificiación de usuario
    }

    @Override
    public int getItemCount()
    {
        return friends.size();
    }
}
