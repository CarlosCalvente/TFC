package com.example.proyectofinal;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.HasDefaultViewModelProviderFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ListaAdapterListas extends RecyclerView.Adapter<ListaAdapterListas.ViewHolder> {

    private List<String> mData;
    private cancion c;
    private PopupWindow p;

    private FirebaseFirestore bd = FirebaseFirestore.getInstance();
    private SharedPreferences pref;

    /*private List<String> listaF;*/
    //private LayoutInflater mInflater
    public ListaAdapterListas(List<String> itemList, cancion c, PopupWindow p){
        //this.mInflater = LayoutInflater.from(contexto);
        this.mData = itemList;
        this.c = c;
        this.p = p;

    }


    @Override
    public int getItemCount(){
        return mData.size();
    }

    @Override
    public ListaAdapterListas.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.plantilla_listas, null, false);
        //mInflater.inflate(R.layout.plantilla_canciones, null);

        return new ListaAdapterListas.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ListaAdapterListas.ViewHolder holder, @SuppressLint("RecyclerView") final int position){
        holder.bindData(mData.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(c == null){
                    Intent i = new Intent(holder.itemView.getContext(), activity_contenidolistas.class);
                    i.putExtra("tituloLista", mData.get(position));
                    holder.itemView.getContext().startActivity(i);
                }else{
                    pref = v.getContext().getSharedPreferences("users_file", Context.MODE_PRIVATE);

                    bd.collection("users")
                            .document(pref.getString("email", null))
                            .collection(mData.get(position)).add(c)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    Toast.makeText(v.getContext(), "Cancion añadida a " + mData.get(position), Toast.LENGTH_SHORT).show();
                                    p.dismiss();
                                }
                            });

                    bd.collection("users")
                            .document(pref.getString("email", null))
                            .update("listas", FieldValue.arrayUnion(mData.get(position)));
                }

            }
        });
    }

    public void setItems(List<String> items){
        mData = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder  {

        //ImageView portada;
        TextView tituloLista;
        ImageButton btnOpcionesLista;
        Button btnPop;
        FragmentContainerView f;
        String idColeccion;


        FirebaseFirestore bd = FirebaseFirestore.getInstance();
        SharedPreferences pref;

        ViewHolder(View itemView){
            super(itemView);
            //portada = itemView.findViewById(R.id.portada);
            tituloLista = itemView.findViewById(R.id.tituloLista);
            btnOpcionesLista = itemView.findViewById(R.id.btnOpcionesLista);
            f = itemView.findViewById(R.id.containerView);


            opciones(idColeccion);
        }

        void opciones(String t){
            btnOpcionesLista.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popMenu = new PopupMenu(itemView.getContext(), v);
                    popMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.eliminarLista:
                                    pref = v.getContext().getSharedPreferences("users_file", Context.MODE_PRIVATE);
                                    bd.collection("users")
                                            .document(pref.getString("email", null))
                                            .collection(idColeccion)
                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                    for (DocumentSnapshot q: value) {
                                                        q.getReference().delete();

                                                        bd.collection("users")
                                                                .document(pref.getString("email", null))
                                                                .update("listas", FieldValue.arrayRemove(idColeccion));
                                                    }
                                                }
                                            });
                                    mData.remove(idColeccion);
                                    notifyDataSetChanged();

                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    popMenu.inflate(R.menu.eliminar_lista);
                    popMenu.show();

                }
            });
        }

        void bindData(final String item) {
            //portada.setColorFilter(Color.parseColor(item.getPortada()), PorterDuff.Mode.SRC_IN);
            tituloLista.setText(item);
            idColeccion = item;
        }


    }
}
