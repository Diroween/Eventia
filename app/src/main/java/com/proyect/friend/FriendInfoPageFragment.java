package com.proyect.friend;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyect.R;

/**
 * Fragment FriendInfoPageFragment que muestra los datos de un amigo
 */
public class FriendInfoPageFragment extends Fragment
{
    /**
     * Variables creadas por la clase fragment para su correcto funcionamiento
     * */

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    /**
     * Creamos tantas variables como datos tenemos que mostrar
     * además de la imagen de usuario
     * y el botón de imagen que vamos a usar para eliminar usuarios
     * */

    ImageView ivDelete;
    ImageView ivFriendImage;
    TextView tvFriendId;
    TextView tvFriendName;
    TextView tvDelete;

    private DatabaseReference databaseReference;

    /**
     * Constructor vacío necesario para el funcionamiento del fragment
     * */

    public FriendInfoPageFragment()
    {
        //Constructor vacío necesario
    }

    /**
     * Método para poder crear instancias del fragmento
     * */

    public static FriendInfoPageFragment newInstance(String param1, String param2)
    {
        FriendInfoPageFragment fragment = new FriendInfoPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Método onCreate necesario
     * */

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /**
     * Método para realizar la lógica del fragment cuando se crea la vista
     * */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        //Creamos una vista que infle el layout
        View view = inflater.inflate(R.layout.fragment_friend_info_page, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        //Asignamos todas las variables a los elementos en pantalla
        ivDelete = view.findViewById(R.id.iv_delete);
        ivFriendImage = view.findViewById(R.id.iv_user_image);
        tvFriendId = view.findViewById(R.id.tv_user_id);
        tvFriendName = view.findViewById(R.id.tv_user_name);
        tvDelete = view.findViewById(R.id.tv_delete);

        //Contenemos en tres strings los datos del amigo que se nos pasan desde friends fragment
        String friendImage = getActivity().getIntent().getStringExtra("friendImageUrl");
        String friendId = getActivity().getIntent().getStringExtra("friendId");
        String friendName = getActivity().getIntent().getStringExtra("friendName");

        //Setteamos el texto de cada textview
        tvFriendId.setText(friendId);
        tvFriendName.setText(friendName);

        //Si el usuario tiene imagen se carga, sino se carga una imagen genérica
        if(friendImage != null)
        {
            Glide.with(this)
                    .load(friendImage)
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.baseline_tag_faces_128)
                    .into(ivFriendImage);
        }
        else
        {
            Glide.with(this)
                    .load(R.drawable.baseline_tag_faces_128)
                    .transform(new CircleCrop())
                    .into(ivFriendImage);
        }

        //Si vamos a consultarnos a nosotros mismo
        //quitamos el botón y el texto de elminar amigo
        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(friendId))
        {
            ivDelete.setVisibility(View.INVISIBLE);
            tvDelete.setVisibility(View.INVISIBLE);
        }

        //Si no se tiene como amigo a un usuario,
        //también quitamos el botón y el tecto de eliminar amigo
        //aunque puede ver al usuario y sus eventos en lso que coinciden
        databaseReference.child("users")
                .child(FirebaseAuth.getInstance().getUid())
                .child("friends").addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(!snapshot.hasChild(friendId))
                        {
                            ivDelete.setVisibility(View.INVISIBLE);
                            tvDelete.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        Log.e("INFO", "No se han podido ocultar los botones");
                    }
                });

        //Le setteamos un escuchador de click al icono de borrar amigo
        ivDelete.setOnClickListener(v ->
        {
            //Creamos un snackbar para que salga un mensaje pequeño
            //para confirmar el borrado de amigo
            //Le ponemos una frase, el texto que funciona como botón y su función de borrado
            Snackbar.make(requireActivity().findViewById(android.R.id.content),
                    R.string.deletefriendquestion,Snackbar.LENGTH_LONG)
                    .setAction(R.string.delete, var ->
                    {
                        //si el usuario tiene un amigo y ese amigo
                        //también le tiene al primero como amigo
                        //se elimina el amigo en los dos usuarios
                        databaseReference.child("users")
                                .child(FirebaseAuth.getInstance().getUid())
                                .child("friends").child(friendId).removeValue()
                                .addOnCompleteListener(task ->
                                {
                                    if(task.isSuccessful())
                                    {
                                        databaseReference.child("users")
                                                .child(friendId)
                                                .child("friends")
                                                .child(FirebaseAuth.getInstance().getUid())
                                                .removeValue().addOnCompleteListener(job ->
                                                {
                                                    if(job.isSuccessful())
                                                    {
                                                        Toast.makeText(getContext(),
                                                                R.string.frienddeleted,
                                                                Toast.LENGTH_SHORT).show();
                                                        getActivity().finish();
                                                    }
                                                    //Si no se ha podido borrar se manda un toast
                                                    //informando del error
                                                    else
                                                    {
                                                        Toast.makeText(getContext(),
                                                                R.string.frienddeletederror,
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                    //Si no se ha podido borrar se manda un toast
                                    //informando del error
                                    else
                                    {
                                        Toast.makeText(getContext(), R.string.frienddeletederror,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }).show();

        });
        //retornamos la vista carga con las funcionalidades
        return view;
    }
}