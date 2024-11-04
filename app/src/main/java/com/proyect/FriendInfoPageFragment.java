package com.proyect;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendInfoPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendInfoPageFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Creamos tantas variables como datos tenemos que mostrar
     * además de los botones de texto que vamos a usar
     * */

    ImageView ivDelete;
    ImageView ivFriendImage;
    TextView tvFriendId;
    TextView tvFriendName;

    public FriendInfoPageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendInfoPageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendInfoPageFragment newInstance(String param1, String param2) {
        FriendInfoPageFragment fragment = new FriendInfoPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friend_info_page, container, false);

        ivDelete = view.findViewById(R.id.iv_delete);
        ivFriendImage = view.findViewById(R.id.iv_user_image);
        tvFriendId = view.findViewById(R.id.tv_user_id);
        tvFriendName = view.findViewById(R.id.tv_user_name);

        String friendImage = getActivity().getIntent().getStringExtra("friendImageUrl");
        String friendId = getActivity().getIntent().getStringExtra("friendId");
        String friendName = getActivity().getIntent().getStringExtra("friendName");

        tvFriendId.setText(friendId);
        tvFriendName.setText(friendName);


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

        ivDelete.setOnClickListener(v ->
        {
            new AlertDialog.Builder(getContext()).setTitle("Eliminar amigo")
                    .setMessage("¿Estás seguro de que quieres eliminar este amigo?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) ->
                    {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                        databaseReference.child("users")
                                .child(FirebaseAuth.getInstance().getUid())
                                .child("friends").child(friendId).removeValue()
                                .addOnCompleteListener(task ->
                                {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(getContext(), "Amigo eliminado",
                                                Toast.LENGTH_SHORT).show();
                                        getActivity().finish();
                                    }
                                    else
                                    {
                                        Toast.makeText(getContext(), "Error al borrar amigo",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(R.drawable.ic_alert_caution)
                    .show();

        });

        return view;
    }
}