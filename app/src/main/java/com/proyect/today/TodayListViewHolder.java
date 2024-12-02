package com.proyect.today;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.proyect.R;

public class TodayListViewHolder extends RecyclerView.ViewHolder {

    /**
     * Declaramos las variables de clase necesarias
     * */

    TextView tvEventName;
    TextView tvEventDate;
    TextView tvEventHour;
    TextView tvEventPlace;
    TextView tvEventUsers;
    ImageView ivEventImage;
    ImageView ivEventHourIcon;
    ImageView ivEventPlaceIcon;
    ImageView ivEventUsersIcon;
    RecyclerView rvRegisteredUsers;

    /**
     * Método para poder asignar a los elementos en pantalla valores
     * */

    public TodayListViewHolder(@NonNull View itemView)
    {
        super(itemView);

        //Declaramos todas las variables necesarias
        //las cuales visualizaremos por pantalla
        tvEventDate = itemView.findViewById(R.id.tv_event_date);
        tvEventHour = itemView.findViewById(R.id.tv_event_hour);
        tvEventName = itemView.findViewById(R.id.tv_event_name);
        tvEventPlace = itemView.findViewById(R.id.tv_event_place);
        tvEventUsers = itemView.findViewById(R.id.tv_event_users);

        ivEventImage = itemView.findViewById(R.id.iv_event_image);
        ivEventHourIcon = itemView.findViewById(R.id.iv_event_hour);
        ivEventPlaceIcon = itemView.findViewById(R.id.iv_event_place);
        ivEventUsersIcon = itemView.findViewById(R.id.iv_event_users);

        ivEventHourIcon.setImageResource(R.drawable.ic_clock);
        ivEventPlaceIcon.setImageResource(R.drawable.ic_marker);
        ivEventUsersIcon.setImageResource(R.drawable.ic_users);

        rvRegisteredUsers = itemView.findViewById(R.id.rv_registered_users);
        rvRegisteredUsers.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
    }
}
