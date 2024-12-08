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

/**
 * Clase que extiende RecyclerView Adapter con un ViewHolder personalizado
 * */

public class FriendSearcherAdapter extends RecyclerView.Adapter<FriendSearcherViewHolder>
{
    /**
     * Creamos las variables de clase
     * Un arraylist para contener los usuarios que tengan cierto username
     * un escuchador de eventos para dar funcinalidad a los botones
     * una varible para saber que item se está seleccionando
     */

    private ArrayList<User> users;
    private OnUserClickListener clickListener;
    private int selectedItem = RecyclerView.NO_POSITION;

    /**
     * Contructor con argumentos
     * */

    public FriendSearcherAdapter(ArrayList<User> users, OnUserClickListener clickListener)
    {
        this.users = users;
        this.clickListener = clickListener;
    }

    /**
     * Método sobreescrito para cargar la vista de los items en el contendor de vistas
     * */

    @NonNull
    @Override
    public FriendSearcherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_searcher,
                parent, false);

        return new FriendSearcherViewHolder(view);
    }

    /**
     * Método sobreescrito para asignar valores a los elementos gráficos
     * también asignamos el escuchador para dar funcionalidad a los botones
     * */

    @Override
    public void onBindViewHolder(@NonNull FriendSearcherViewHolder holder, int position)
    {
        User user = users.get(position);

        holder.tvName.setText(user.getName());
        holder.tvFriendId.setText(user.getId());

        //Si el usuario tiene una imagen la muestra, sino sale un placeholder sustitutivo

        if(user.getImageUrl() != null)

        {
            Glide.with(holder.itemView.getContext())
                    .load(user.getImageUrl())
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

        //Hacemos que al pulsar en un amigo este se destaque en otro color
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
            clickListener.onUserClick(user);
        });
    }

    /**
     * Método que devuelve todos los usuarios cargados en el arraylist
     * */

    @Override
    public int getItemCount()
    {
        return users.size();
    }

    /**
     * Interfaz que contiene el método onUserClick para dar funcionalidad a los botones
     * *-Yosef-* esto lo explico también en FriendSearcherActivity, es similar a hacer
     * la de View.OnClickListener
     * */

    public interface OnUserClickListener
    {
        void onUserClick(User user);
    }
}
