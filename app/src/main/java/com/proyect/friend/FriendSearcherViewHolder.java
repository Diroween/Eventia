package com.proyect.friend;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.proyect.R;

/**
 * Clase que extiende ViewHolder para poder cargar los elementos del Recyclerview en pantalla
 * */

public class FriendSearcherViewHolder extends RecyclerView.ViewHolder
{
    /**
     * Creamos tantas variables de texto como elementos vamos a tratar en la lista
     * El nombre de usuario y su id para diferenciarlo de otros con el mismo username
     * La foto de un usuario para diferenciarlo más facil de los demás
     * */
    TextView tvName;
    TextView tvFriendId;
    ImageView ivUser;

    /**
     * Constructor con argumentos donde asignamos cada variable a su elemento visual
     * */

    public FriendSearcherViewHolder(@NonNull View itemView)
    {
        super(itemView);

        tvName = itemView.findViewById(R.id.tv_name);
        tvFriendId = itemView.findViewById(R.id.tv_friendId);
        ivUser = itemView.findViewById(R.id.iv_user);
    }
}
