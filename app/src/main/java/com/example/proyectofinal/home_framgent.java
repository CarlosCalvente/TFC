package com.example.proyectofinal;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class home_framgent extends Fragment implements SearchView.OnQueryTextListener{

    View vista;
    RecyclerView rHome;
    FirebaseFirestore bdHome;
    ListAdapter lista;
    SearchView buscador;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        vista = inflater.inflate(R.layout.fragment_home_framgent, container, false);
        rHome = vista.findViewById(R.id.reciclerHome);
        buscador = vista.findViewById(R.id.buscador);
        buscador.setOnQueryTextListener(this);

        init();


        return vista;
    }

    //Funcion que recoge todas las canciones y las muestra en la pantalla
    private void init(){

        bdHome = FirebaseFirestore.getInstance();
        //new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        rHome.setLayoutManager(new LinearLayoutManager(getContext()));

        Query q = bdHome.collection("canciones");

            q.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (value.isEmpty()) {
                    } else {
                        if (error != null) {
                            Toast.makeText(getContext(), "Error al recuperar canciones", Toast.LENGTH_SHORT).show();
                        } else {
                            List<cancion> canciones = value.toObjects(cancion.class);
                            lista = new ListAdapter(canciones, 1, null);
                            rHome.setAdapter(lista);
                        }
                    }
                }
            });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    //Funcion que recoge cualquier cadena escrita en el buscador
    @Override
    public boolean onQueryTextChange(String newText) {
        lista.filtrado(newText);
        return false;
    }

}