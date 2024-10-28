package com.proyect;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


/**
 * Clase que extiende RecyclerView ViewHolder para cargar elementos en pantalla
 * */

public class FriendsRequestViewHolder extends RecyclerView.ViewHolder
{
    /**
     * Creamos tantas variables de clase como elementos queremos dar funcionalidad
     * */
    TextView tvName;
    Button btnAccept;
    Button btnReject;

    /**
     * Constructor que llamamos a la superclase y asignamos cada variable a cada botón
     * */
    public FriendsRequestViewHolder(@NonNull View itemView)
    {
        super(itemView);

        tvName = itemView.findViewById(R.id.tv_name);
        btnAccept = itemView.findViewById(R.id.btn_accept);
        btnReject = itemView.findViewById(R.id.btn_reject);
    }
}
