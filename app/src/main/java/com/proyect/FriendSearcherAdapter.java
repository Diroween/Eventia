package com.proyect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Clase que extiende RecyclerView Adapter con un ViewHolder personalizado
 * */

public class FriendSearcherAdapter extends RecyclerView.Adapter<FriendSearcherViewHolder>
{
    /**
     * Creamos las variables de clase
     * Un arraylist para contener los usuarios que tengan cierto username
     * un escuchador de eventos para dar funcinalidad a los botones
     */

    private ArrayList<User> users;
    private OnUserClickListener clickListener;

    /**
     * Contructor con argumentos
     * */

    public FriendSearcherAdapter(ArrayList<User> users, OnUserClickListener clickListener)
    {
        this.users = users;
        this.clickListener = clickListener;
    }

    /**
     * Método sobreescrito para cargar la vista de los items en el contendor de vistas
     * */

    @NonNull
    @Override
    public FriendSearcherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_searcher,
                parent, false);

        return new FriendSearcherViewHolder(view);
    }

    /**
     * Método sobreescrito para asignar valores a los elementos gráficos
     * también asignamos el escuchador para dar funcionalidad a los botones
     * */

    @Override
    public void onBindViewHolder(@NonNull FriendSearcherViewHolder holder, int position)
    {
        User user = users.get(position);

        holder.tvName.setText(user.getName());
        holder.tvFriendId.setText(user.getId());

        holder.itemView.setOnClickListener(v -> clickListener.onUserClick(user));
    }

    /**
     * Método que devuelve todos los usuarios cargados en el arraylist
     * */

    @Override
    public int getItemCount()
    {
        return users.size();
    }

    /**
     * Interfaz que contiene el método onUserClick para dar funcionalidad a los botones
     * *-Yosef-* esto lo explico también en FriendSearcherActivity, es similar a hacer
     * la de View.OnClickListener
     * */

    public interface OnUserClickListener
    {
        void onUserClick(User user);
    }
}
