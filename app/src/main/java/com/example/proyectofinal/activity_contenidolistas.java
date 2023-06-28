package com.example.proyectofinal;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class activity_contenidolistas extends AppCompatActivity {

    String tituloLista;
    MaterialToolbar appBar;
    RecyclerView reciclerListaC;
    ListAdapter listaCanciones;
    int tema;
    FirebaseFirestore bd = FirebaseFirestore.getInstance();
    SharedPreferences pref, settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contenidolistas);

        appBar = findViewById(R.id.actionBarContenidoListas);
        reciclerListaC = findViewById(R.id.reciclerListaC);

        pref = getSharedPreferences("users_file", Context.MODE_PRIVATE);
        settings = getSharedPreferences("settings", Context.MODE_PRIVATE);
        tema = settings.getInt("tema", 0);
        savedInstanceState = getIntent().getExtras();
        tituloLista = savedInstanceState.getString("tituloLista");
        appBar.setTitle(tituloLista);

        appBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setDayNight(tema);
        init();
    }

    public void setDayNight(int mode){
        if(mode == 1){
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    //Funcion que carga y muestra las canciones de la lista
    private void init(){

        bd = FirebaseFirestore.getInstance();
        //new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        reciclerListaC.setLayoutManager(new LinearLayoutManager(activity_contenidolistas.this));

        Query q = bd.collection("users").document(pref.getString("email", null)).collection(tituloLista);

        q.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.isEmpty()){
                }else{
                    if(error != null){
                        Toast.makeText(activity_contenidolistas.this, "Error al recuperar canciones", Toast.LENGTH_SHORT).show();
                    }else{
                        List<cancion> canciones = value.toObjects(cancion.class);
                        listaCanciones = new ListAdapter(canciones, 3, tituloLista);
                        reciclerListaC.setAdapter(listaCanciones);
                    }
                }
            }
        });
    }

}