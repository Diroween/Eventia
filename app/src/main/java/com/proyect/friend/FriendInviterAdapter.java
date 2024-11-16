package com.proyect.friend;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.proyect.R;
import com.proyect.user.User;

import java.util.ArrayList;

public class FriendInviterAdapter extends RecyclerView.Adapter<FriendsViewHolder>
{
    /**
     * Creamos dos variables de clase, el arraylist y la variable que guarda el valor del item
     * seleccionado y que inicializamos con el valor sin posición
     * */

    private ArrayList<User> friends;
    private OnUserClickListener clickListener;
    private int selectedItem = RecyclerView.NO_POSITION;

    /**
     * Constructor con argumento
     * */

    public FriendInviterAdapter(ArrayList<User> friends, OnUserClickListener clickListener)
    {
        this.friends = friends;
        this.clickListener = clickListener;
    }

    /**
     * Sobreescritura del método para poder cargar los items en el recyclerview
     * */

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);

        return new FriendsViewHolder(view);
    }

    /**
     * Sobreescritura del método para poder dar funcionalidad a los elementos que aparecen de cada
     * item, así como para asignar un escuchador al pulsar en cada elemento
     * */

    @Override
    public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position)
    {
        User friend = friends.get(position);

        holder.tvName.setText(friend.getName());

        //Si el usuario tiene una imagen la muestra, sino sale un placeholder sustitutivo
        if(friend.getImageUrl() != null)
        {
            Glide.with(holder.itemView.getContext())
                    .load(friend.getImageUrl())
                    .placeholder(R.drawable.baseline_tag_faces_128)
                    .transform(new CircleCrop())
                    .into(holder.ivUser);
        }
        else
        {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.baseline_tag_faces_128)
                    .transform(new CircleCrop())
                    .into(holder.ivUser);
        }

        //Hcemos que al pulsar en un amigo este se destaque en otro color
        //y cuando ese amigo deja de ser el foco se deje de destacar
        holder.itemView.setBackgroundColor(selectedItem == holder.getBindingAdapterPosition()
                ? Color.rgb(255,248,181) : Color.TRANSPARENT);

        holder.itemView.setOnClickListener(v ->
        {
            int previousSelectedPosition = selectedItem;
            selectedItem = holder.getBindingAdapterPosition();
            notifyItemChanged(previousSelectedPosition);
            notifyItemChanged(selectedItem);

            //también asignamos el escuchador
            clickListener.onUserClick(friend);
        });
    }

    /**
     * método que devuelve la cantidad de amigos
     * */
    @Override
    public int getItemCount()
    {
        return friends.size();
    }
    public interface OnUserClickListener
    {
        void onUserClick(User user);
    }

}
