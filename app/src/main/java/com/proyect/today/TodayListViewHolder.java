package com.proyect.today;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.proyect.R;

public class TodayListViewHolder extends RecyclerView.ViewHolder {

    TextView tvEventName;
    TextView tvEventDate;
    TextView tvEventHour;
    TextView tvEventPlace;
    TextView tvEventUsers;
    ImageView ivEventImage;
    ImageView ivEventHourIcon;
    ImageView ivEventPlaceIcon;
    ImageView ivEventUsersIcon;
    RecyclerView recyclerView;
    public TodayListViewHolder(@NonNull View itemView) {
        super(itemView);

        tvEventDate = itemView.findViewById(R.id.tv_event_date);
        tvEventHour = itemView.findViewById(R.id.tv_event_hour);
        tvEventName = itemView.findViewById(R.id.tv_event_name);
        tvEventPlace = itemView.findViewById(R.id.tv_event_place);
        tvEventUsers = itemView.findViewById(R.id.tv_event_users);

        ivEventImage = itemView.findViewById(R.id.iv_user_image);
        ivEventHourIcon = itemView.findViewById(R.id.iv_event_hour);
        ivEventPlaceIcon = itemView.findViewById(R.id.iv_event_place);
        ivEventUsersIcon = itemView.findViewById(R.id.iv_event_users);

        ivEventHourIcon.setImageResource(R.drawable.ic_clock);
        ivEventPlaceIcon.setImageResource(R.drawable.ic_marker);
        ivEventUsersIcon.setImageResource(R.drawable.ic_users);

        recyclerView = itemView.findViewById(R.id.rv_registered_users);

    }
}
