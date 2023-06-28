package com.example.proyectofinal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class library_fragment extends Fragment {

    View vista;
    RecyclerView rLibrary;
    ListaAdapterListas listaListas;
    Handler handler = new Handler();

    FirebaseFirestore bd = FirebaseFirestore.getInstance();
    SharedPreferences pref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        vista = inflater.inflate(R.layout.fragment_library_fragment, container, false);
        rLibrary = vista.findViewById(R.id.containerListas);

        pref = getActivity().getSharedPreferences("users_file", Context.MODE_PRIVATE);


        init();

        return vista;
    }


    //Funcion que recoge las listas de reproduccion existentes y las muestra
    public void init(){

        bd = FirebaseFirestore.getInstance();
        //new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        //new LinearLayoutManager(getContext())
        rLibrary.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));

        bd.collection("users").document(pref.getString("email", null)).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot d = task.getResult();
                            if(d == null){
                                Toast.makeText(getContext(), "Error al encontrar el documento", Toast.LENGTH_SHORT).show();
                            }else{
                                Map<String, Object> mapa = d.getData();
                                if(mapa.isEmpty()){
                                    Toast.makeText(getContext(), "Error al recoger los datos", Toast.LENGTH_SHORT).show();
                                }else{
                                    Object objeto = mapa.get("listas");
                                    if (objeto == null){
                                        Toast.makeText(getContext(), "Sin listas", Toast.LENGTH_SHORT).show();
                                    }else{
                                         List<String> listaC = (List<String>) task.getResult().getData().get("listas");

                                         listaListas = new ListaAdapterListas(listaC, null, null);
                                         rLibrary.setAdapter(listaListas);

                                    }
                                }
                            }

                        }
                    }
                });

    }
}