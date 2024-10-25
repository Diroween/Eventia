package com.proyect;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsViewHolder>
{
    private ArrayList<User> friends;
    private int selectedItem = RecyclerView.NO_POSITION;

    public FriendsAdapter(ArrayList<User> friends)
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
        User friend = friends.get(position);

        holder.tvName.setText(friend.getName());

        holder.itemView.setBackgroundColor(selectedItem == holder.getBindingAdapterPosition() ? Color.LTGRAY : Color.TRANSPARENT);

        holder.itemView.setOnClickListener(v ->
        {
            int previousSelectedPosition = selectedItem;
            selectedItem = holder.getBindingAdapterPosition();
            notifyItemChanged(previousSelectedPosition);
            notifyItemChanged(selectedItem);
        });


        //hay que implementar que un usuario tenga una imagen de perfil
        //tiene que ser un submenú dentro de su modificiación de usuario
    }

    @Override
    public int getItemCount()
    {
        return friends.size();
    }
}
