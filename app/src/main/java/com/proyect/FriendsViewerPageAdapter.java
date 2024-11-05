package com.proyect;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Clase FriendsViewerPageAdapter que extiende FragmentStateAdapter
 * para poder hacer la paginación con viewpager2
 * */

public class FriendsViewerPageAdapter extends FragmentStateAdapter
{
    /**
     * Constructor que llama a la superclase
     * */

    public FriendsViewerPageAdapter(@NonNull FragmentActivity fragmentActivity)
    {
        super(fragmentActivity);
    }

    /**
     * Método sobreescrito para cuando se crea un fragment
     * */

    @NonNull
    @Override
    public Fragment createFragment(int position)
    {
        //si la posición donde se encuentra el fragment es 1
        //se cargan el fragment de la lista de eventos
        //sino se carga el fragment de información del amigo
        if(position == 1)
        {
            return new FriendEventListFragment();
        }
        else
        {
            return new FriendInfoPageFragment();
        }
    }

    /**
     * Método que devuelve cuantas páginas tiene el adaptador
     * necesario para el funcionamiento de la paginación
     * */

    @Override
    public int getItemCount()
    {
        return 2;
    }
}
