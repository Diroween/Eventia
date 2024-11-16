package com.proyect.calendar;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.proyect.R;

/**
 * ViewHolder personalizado para CalendarFragmentAdapter
 * */

public class CalendarFragmentViewHolder extends RecyclerView.ViewHolder
{
    /**
     * Creamos tantas variables de clase como elementos queramos dar funcionalidad
     * */
    ImageView ivEventImage;
    TextView tvEventName;
    TextView tvEventData;

    /**
     * Asignamos las variables a los elementos en pantalla
     * */

    public CalendarFragmentViewHolder(@NonNull View itemView)
    {
        super(itemView);

        ivEventImage = itemView.findViewById(R.id.iv_event_image);
        tvEventName = itemView.findViewById(R.id.tv_event_name);
        tvEventData = itemView.findViewById(R.id.tv_event_data);
    }
}
