package com.proyect.friend;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.proyect.R;
import com.proyect.user.User;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Clase que extiende RecyclerView Adapter con un viewholder personalizado
 * */

public class FriendsAdapter extends RecyclerView.Adapter<FriendsViewHolder>
{
    /**
     * Creamos cinco variables de clase:
     * - El arraylist
     * - La variable que guarda el valor del item
     * - Un string con el Id del usuario actual
     * - Un objeto de la interfaz OnItemClickListener para poder mostrar un popupmenu
     * - Un valor booleano para ctivar o desactivare el longclick
     * seleccionado y que inicializamos con el valor sin posición
     * */

    private ArrayList<User> friends;
    private OnItemClickListener onItemClickListener;
    private String currentUserId;
    private int selectedItem = RecyclerView.NO_POSITION;
    private HashMap<String, String> roles;
    private boolean enableLongClick;

    /**
     * Constructores con argumentos
     * */

    public FriendsAdapter(ArrayList<User> friends, boolean enableLongClick)
    {
        this.friends = friends;
        this.enableLongClick = enableLongClick;
    }

    public FriendsAdapter(ArrayList<User> friends, String currentUserId, boolean enableLongClick)
    {
        this.friends = friends;
        this.currentUserId = currentUserId;
        this.enableLongClick = enableLongClick;
    }

    public FriendsAdapter(ArrayList<User> friends, String currentUserId,
                          OnItemClickListener onItemClickListener, HashMap<String, String> roles,
                          boolean enableLongClick)
    {
        this.friends = friends;
        this.onItemClickListener = onItemClickListener;
        this.currentUserId = currentUserId;
        this.roles = roles;
        this.enableLongClick = enableLongClick;
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
        if(!friend.getImageUrl().isEmpty())
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
        //también ponemos código para poder abrir el detalle de cada amigo
        holder.itemView.setBackgroundColor(selectedItem == holder.getBindingAdapterPosition()
                ? Color.rgb(255,248,181) : Color.TRANSPARENT);

        holder.itemView.setOnClickListener(v ->
        {
            int previousSelectedPosition = selectedItem;
            selectedItem = holder.getBindingAdapterPosition();
            notifyItemChanged(previousSelectedPosition);
            notifyItemChanged(selectedItem);

            //Si el usuario que vamos a ver es el propio usuario
            //se queda seleccionado pero no se muestra a sí mismo
            if (!friend.getId().equals(currentUserId))
            {
                //Hacemos un intent para que se pueda abrir la actividad
                //para ver a un amigo
                Intent intent = new Intent(holder.itemView.getContext(), FriendViewerActivity.class);

                //Ponemos como extras todos los datos que vamos a mostrar de cada amigo
                //que coinciden con los registrados de un amigo en la bdd
                intent.putExtra("friendId", friend.getId());
                intent.putExtra("friendName", friend.getName());
                intent.putExtra("friendImageUrl", friend.getImageUrl());

                //Se inicia la actividad que muestra cada amigo
                holder.itemView.getContext().startActivity(intent);
            }

        });

        //Si se confirma que se pueda desplegar el menú contextual y que no es el propio usuario
        //el que está siendo seleccionado, se activa la pulsación prolongada
        if(enableLongClick)
        {
            String role = roles.get(friend.getId());

            if ("admin".equals(role))
            {
                holder.itemView.setBackground(ContextCompat.getDrawable(holder.itemView.getContext()
                        , R.drawable.adminbckgrnd));
            }

            holder.itemView.setOnLongClickListener(v ->
            {
                if (!friend.getId().equals(currentUserId))
                {
                    onItemClickListener.onItemClick(v, friend);
                }

                return true;
            });
        }

    }

    /**
     * método que devuelve la cantidad de amigos
     * */
    @Override
    public int getItemCount()
    {
        return friends.size();
    }

    /**
     * Interfaz para poder cargar el menú contextual en un usuario
     * */

    public interface OnItemClickListener
    {
        void onItemClick(View view, User user);
    }
}
