package com.proyect;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Actividad FriendViewer para poder visualizar los datos de un amigo
 * */

public class FriendViewerActivity extends AppCompatActivity
{
    /**
     * Creamos las variables de clase:
     * un tab layout para indicar cada página
     * un viewpager para poder usar la paginación
     * un adaptador para darle funcionalidad al viewpager
     * */

    TabLayout tabLayout;
    ViewPager2 viewPager;
    FriendsViewerPageAdapter viewerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_friend_viewer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Asignamos los elementos de pantalla a las variables
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        //inicializamos el adaptador
        viewerAdapter = new FriendsViewerPageAdapter(this);

        //Setteamos el adapter al viewpager
        viewPager.setAdapter(viewerAdapter);

        //Creamos un mediador de tabs para asignar un nombre a cada pestaña
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
        {
            if(position == 0)
            {
                tab.setText(R.string.username);
            }
            else
            {
                tab.setText(R.string.events);
            }

        }).attach();

    }
}