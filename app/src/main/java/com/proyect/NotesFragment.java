package com.proyect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Map;

/**
 * Fragment friends creado para contener las notas del usuario
 */

public class NotesFragment extends Fragment
{

    /**
     * Variables param creadas por el propio fragment para su funcionamiento correcto
     * */
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    /**
     * Creamos las variables de clase necesarias:
     * el botón flotante para añadir nuevas notas
     * El Recyclerview donde se verán las notas
     * El contenedor para actualizar las notas
     * Un adaptador y un arraylist para dar forma
     */

    FloatingActionButton fabAddNote;
    RecyclerView rvNotes;
    private SwipeRefreshLayout srlNotes;
    NotesAdapter adapter;
    ArrayList<Note> arrayNotes;

    /**
     * Constructor sin argumentos necesario para el funcionamiento del fragment
     * */

    public NotesFragment()
    {
        //Constructor vacío necesario
    }

    /**
     * Constructor con argumentos para poder crear instancias del fragment
     */

    public static NotesFragment newInstance(String param1, String param2) {
        NotesFragment fragment = new NotesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Método onCreate usado para crear el fragment
     * */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /**
     * Método necesario para poder inflar el fragment
     * */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notes, container, false);
    }

    /**
     * Método necesario para dar funcionalidad a los widgets
     *
     * @param view la vista de nuestro fragment
     * @param savedInstanceState Bundle necesario para el funcionamiento
     *
     * */

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        //Cogemos la base de datos de FirebaseAthenticator inicializada en la LoginActivity
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        //Cogemos el usuario actual que ha iniciado la sesión
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        //Decimos que el contenedor de las notas es el email de la persona
        //único para cada usuario
        String noteStore = firebaseUser.getEmail();

        //Creamos un SharedPrefereces para que guarde permanentemente las notas
        SharedPreferences sharedNotes = view.getContext().getSharedPreferences(noteStore,
                Context.MODE_PRIVATE);

        //Llenamos el array con los nombres de las notas alamcenados
        arrayNotes = getNotes(sharedNotes);

        //inicializamos el recylerview y el swiperefresh indicando que son los del layout
        rvNotes = view.findViewById(R.id.rv_notes);
        srlNotes = view.findViewById(R.id.srl_notes);

        //inicializamos el adaptador y le pasamos como argumento el array de nombres y el contexto
        adapter = new NotesAdapter(getContext(), arrayNotes);

        rvNotes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotes.setAdapter(adapter);

        //le ponemos funcionalidad al pulsar en cada nota
        //*-Yosef-* ya solo queda que las notas se borren

        //Le damos funcionalidad al refrescarse la pagina
        srlNotes.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            /**
             * Sobreescribimos el método onRefresh para actualizar
             * el array al refrescar
             * */
            @Override
            public void onRefresh()
            {
                //Le indicamos que vacie arrayNames
                arrayNotes.clear();

                //Le indicamos que se vuelva a llenar con las shared preferences actualizadas
                arrayNotes.addAll(getNotes(sharedNotes));

                //Le indicamos al adaptardor que ha habido cambios
                //y le forzamosa actualizarse
                adapter.notifyDataSetChanged();

                //Hacemos que la flecha de refrescar desaparezca
                srlNotes.setRefreshing(false);
            }

        });

        //Creamos el botón flotante para añadir notas
        fabAddNote = (FloatingActionButton)view.findViewById(R.id.fab_add_note);

        //Le incluimos funcionalidad a dar click en el botón flotante
        //*--yosef--* Gracias a esto ya podemos crear notas nuevas
        //al pinchar en cada una de las notas se pueden editar
        fabAddNote.setOnClickListener(new View.OnClickListener()
        {
            /**
             * Sobreescribimos el método onCLick para indicar la funcionalidad del botón
             * */
            @Override
            public void onClick(View view)
            {
                //Creamos un intent para NotesActivity
                Intent intent = new Intent(view.getContext(), NotesActivity.class);

                //Le pasamos como extra el nombre del contenedor de notas
                intent.putExtra("note_store", noteStore);

                //iniciamos el intent
                startActivity(intent);
            }
        });

        //Creamos un ayudante para poder dar movimiento al elemento del recyclerview
        //y le pasamos una llamada simple en la que le indicamos que da igual que
        //se swipee a la derecha o a la izquierda
        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper
                .SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
        {
            /**
             * Método para mover por pantalla el elemento
             * No lo vamos a hacer, por lo que retornamos false
             * */
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target)
            {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction)
            {
                //Obtenemos la posición del elemento
                int position = viewHolder.getBindingAdapterPosition();

                //Obtenemos la nota que está asociada a esa posición el el arraylist
                Note note = arrayNotes.get(position);

                //Eliminamos la nota del usuario
                deleteNote(sharedNotes, note.getNoteName());

                //Eliminamos al nota del arraylist
                arrayNotes.remove(position);

                //Notificamos los cambios al adaptador en esa posición
                adapter.notifyItemRemoved(position);

                //motificamos que se ha borrado la nota
                Toast.makeText(view.getContext(), R.string.deletednote, Toast.LENGTH_SHORT).show();
            }
        });

        //Asociamos al Recyclerview nuesto ayudante
        touchHelper.attachToRecyclerView(rvNotes);

        //le indicamos al adaptador que cuando se cree el fragment se actualice
        adapter.notifyDataSetChanged();
    }

    /**
     * Método que devuelve un array list cargado con todos los nombres de las notas
     *
     * @param sharedNotes instancia de sharedpreferences pasada para coger los nombres
     * */

    public ArrayList<Note> getNotes(SharedPreferences sharedNotes)
    {
        //Creamos un mapa para recoger los pares clave-valor de las notas, dos String
        Map<String, ?> map = sharedNotes.getAll();

        //Creamos un arraylist de notas
        ArrayList<Note> notes = new ArrayList<Note>();

        //En el bucle se hace lo siguiente
        //Se recorre el mapa
        //se guardan el par y el valor según su correspondencia para una nota
        //se mete en el arraylist que creamos una nueva nota con los datos recogidos
        for(Map.Entry<String, ?> entry : map.entrySet())
        {
            String noteName = entry.getKey();
            String noteBody = entry.getValue().toString();
            notes.add(new Note(noteName, noteBody));
        }

        //retornamos el arraylist cargado con las notas
        return notes;
    }

    /**
     * Método que borra una nota de las sharedpreferences
     *
     * @param noteName El nombre de la nota a borrar.
     * @param sp instancia de sharedpreferences pasada para borrar de ella la nota.
     * //
     * *--Yosef--* hay que hacer un swipe para eliminar las notas en este fragment
     * lo he estado bien y lo creo realizable con pocos cambios
     * */

    public void deleteNote(SharedPreferences sp, String noteName)
    {
        //Creamos un editor para poder editar las sharedpreferences
        SharedPreferences.Editor editor = sp.edit();

        //Eliminamos las nota pasada como argumento
        editor.remove(noteName);

        //aplicamos los cambios
        editor.apply();

        //los hacemos permanentes
        editor.commit();
    }

}