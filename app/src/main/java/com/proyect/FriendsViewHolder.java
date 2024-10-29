package com.proyect;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Clase que extiende RecyclerView ViewHolder para cargar elementos en pantalla
 * */

public class FriendsViewHolder extends RecyclerView.ViewHolder
{
    /**
     * Creamos tantas variables de clase como elementos queremos dar funcionalidad
     * //
     * *-Yosef-* Cuando los usuarios tengan una foto se cargará también, me momento solo pongo
     * una genérica
     * */
    TextView tvName;
    ImageView ivUser;

    /**
     * Constructor que llamamos a la superclase y asignamos cada variable a cada botón
     * */
    public FriendsViewHolder(@NonNull View itemView)
    {
        super(itemView);

        tvName = itemView.findViewById(R.id.tv_name);
        ivUser = itemView.findViewById(R.id.iv_user);
    }
}
