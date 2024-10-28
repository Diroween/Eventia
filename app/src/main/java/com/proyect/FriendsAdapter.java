package com.proyect;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Clase que extiende RecyclerView Adapter con un viewholder personalizado
 * */

public class FriendsAdapter extends RecyclerView.Adapter<FriendsViewHolder>
{
    /**
     * Creamos dos variables de clase, el arraylist y la variable que guarda el valor del item
     * seleccionado y que inicializamos con el valor sin posición
     * */

    private ArrayList<User> friends;
    private int selectedItem = RecyclerView.NO_POSITION;

    /**
     * Constructor con argumento
     * */

    public FriendsAdapter(ArrayList<User> friends)
    {
        this.friends = friends;
    }

    /**
     * Sobreescritura del método para poder cargar los items en el recyclerview
     * */

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);

        return new FriendsViewHolder(view);
    }

    /**
     * Sobreescritura del método para poder dar funcionalidad a los elementos que aparecen de cada
     * item, así como para asignar un escuchador al pulsar en cada elemento
     * */

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

    /**
     * método que devuelve la cantidad de amigos
     * */
    @Override
    public int getItemCount()
    {
        return friends.size();
    }
}
