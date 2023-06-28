package com.example.proyectofinal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    SharedPreferences pref, settings;
    BottomNavigationView navigation;
    TextView bienvenida;
    int tema;
    String email;
    String password;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        settings = getSharedPreferences("settings", Context.MODE_PRIVATE);
        tema = settings.getInt("tema", 0);
        setDayNight(tema);

        navigation = findViewById(R.id.bottomNavigationView);
        bienvenida = findViewById(R.id.bienvenida);

        savedInstanceState = getIntent().getExtras();

        email = savedInstanceState.getString("email");
        password = savedInstanceState.getString("password");

        bienvenida.setText("Bienvenido " + email);

        //Instancia de preferences para sesion usuarios
        pref =  getSharedPreferences(getResources().getString(R.string.usersFile), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        //Guardar datos de sesion
        editor.putString("email", email);
        editor.putString("password", password);
        editor.apply();

        //Montar pantalla
        setUp();

        //Funcion del menu inferior para navegar por pantallas
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if(item.getItemId() == R.id.home){
                    setCurrentFragment(new home_framgent());
                    bienvenida.setText("");
                }

                if(item.getItemId() == R.id.biblioteca){
                    setCurrentFragment(new library_fragment());
                    bienvenida.setText("");
                }

                if(item.getItemId() == R.id.cuenta){
                    setCurrentFragment(new account_fragment());
                    bienvenida.setText("");
                }

                return true;
            }
        });

    }

    //Funcioin que cambia al modo oscuro si esta seleccionado
    public void setDayNight(int mode){
        if(mode == 1){
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

    }

    private void setCurrentFragment(Fragment fragment){
         getSupportFragmentManager().beginTransaction().replace(R.id.containerView, fragment)
                 .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                 .commit();
    }

    private void setUp(){
        setTitle("App");
        /*if(MainActivity.inicio == 0){
            setCurrentFragment( new home_framgent());
            MainActivity.inicio++;
        }*/
    }

    //Funcion que comprueba si el modo oscuro esta activo cuando regresa a la pantalla
    @Override
    protected void onRestart() {
        super.onRestart();
        settings = getSharedPreferences("settings", Context.MODE_PRIVATE);
        tema = settings.getInt("tema", 0);
        SharedPreferences.Editor edit = settings.edit();
        edit.putInt("creacion", 1);
        edit.apply();
        setDayNight(tema);
    }

}