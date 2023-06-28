package com.example.proyectofinal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

public class configuracion extends AppCompatActivity {

    MaterialToolbar actionBar;
    Switch btnOscuro;
    Button btnCerrar;
    SharedPreferences pref, settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        actionBar = findViewById(R.id.actionBar);
        btnOscuro = findViewById(R.id.btnOscuro);
        btnCerrar = findViewById(R.id.btnCerrar);

        pref =  getSharedPreferences(getResources().getString(R.string.usersFile), Context.MODE_PRIVATE);
        settings = getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = settings.edit();
        int tema= settings.getInt("tema", 0);

        //Funcion del icono de la flecha para volver atras
        actionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (tema==1){
            btnOscuro.setChecked(true);
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        //Switch que controla el modo oscuro
        btnOscuro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnOscuro.isChecked()){
                    edit.putInt("tema", 1);
                    getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }else{
                    edit.putInt("tema", 0);
                    getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                edit.apply();
            }
        });


        //Boton que permite cerrar sesion
        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences.Editor editor = pref.edit();
                editor.clear();
                editor.apply();

                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(v.getContext(), login_App.class);
                startActivity(i);
                finishAffinity();
            }
        });


    }

}