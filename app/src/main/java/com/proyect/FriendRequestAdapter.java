package com.proyect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Clase que extiende el adaptador de RecyclerView con un viewholder personalizado
 * */

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendsRequestViewHolder>
{
    /**
     * Creamos las variables de clase necesarias
     * un arraylist para contener las peticiones y un escuchador de eventos
     * */
    private ArrayList<FriendRequest> friendRequests;
    private OnFriendRequestActionListener requestActionListener;

    /**
     * Constructor con argumentos
     * */
    public FriendRequestAdapter(ArrayList<FriendRequest> friendRequests,
                                OnFriendRequestActionListener requestActionListener)
    {
        this.friendRequests = friendRequests;
        this.requestActionListener = requestActionListener;
    }

    /**
     * Sobreescritura del método para poder cargar los items en el recyclerview
     * */
    @NonNull
    @Override
    public FriendsRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_request, parent, false);

        return new FriendsRequestViewHolder(view);
    }

    /**
     * Sobreescritura del método para poder dar funcionalidad a los elementos que aparecen de cada
     * item, así como para asignar un escuchador a los botones
     * */
    @Override
    public void onBindViewHolder(@NonNull FriendsRequestViewHolder holder, int position)
    {
        FriendRequest request = friendRequests.get(position);

        holder.tvName.setText(request.getName());

        holder.btnAccept.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                requestActionListener.onAccept(request.getFrom());
            }
        });

        holder.btnReject.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                requestActionListener.onReject(request.getFrom());
            }
        });
    }

    /**
     * método que devuelve la cantidad de peticiones
     * */
    @Override
    public int getItemCount()
    {
        return friendRequests.size();
    }

    /**
     * Interfaz de la clase para poder dar funcionalidad a los botones
     * */
    public interface OnFriendRequestActionListener
    {
        void onAccept(String userId);
        void onReject(String userId);
    }
}
