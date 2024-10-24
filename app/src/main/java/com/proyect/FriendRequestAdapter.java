package com.proyect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendsRequestViewHolder>
{
    private ArrayList<FriendRequest> friendRequests;
    private OnFriendRequestActionListener requestActionListener;

    public FriendRequestAdapter(ArrayList<FriendRequest> friendRequests,
                                OnFriendRequestActionListener requestActionListener)
    {
        this.friendRequests = friendRequests;
        this.requestActionListener = requestActionListener;
    }

    @NonNull
    @Override
    public FriendsRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_request, parent, false);

        return new FriendsRequestViewHolder(view);
    }

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

    @Override
    public int getItemCount()
    {
        return friendRequests.size();
    }

    public interface OnFriendRequestActionListener
    {
        void onAccept(String userId);
        void onReject(String userId);
    }
}
