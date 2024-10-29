package com.proyect;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Clase que extiende ViewHolder para poder cargar los elementos del Recyclerview en pantalla
 * */

public class FriendSearcherViewHolder extends RecyclerView.ViewHolder
{
    /**
     * Creamos tantas variables de texto como elementos vamos a tratar en la lista
     * El nombre de usuario y su id para diferenciarlo de otros con el mismo username
     * //
     * *-Yosef-* Cuando los usuarios ya tengan imágenes se cargarán y modificaré esta clase
     * de momento les dejo una imagen genérica
     * */
    TextView tvName;
    TextView tvFriendId;

    /**
     * Constructor con argumentos donde asignamos cada variable a su elemento visual
     * */

    public FriendSearcherViewHolder(@NonNull View itemView)
    {
        super(itemView);

        tvName = itemView.findViewById(R.id.tv_name);
        tvFriendId = itemView.findViewById(R.id.tv_friendId);
    }
}
