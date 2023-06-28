package com.example.proyectofinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class reproductor extends AppCompatActivity {

    String titulo, autor, categoria, archivo, ruta;
    TextView txtTitulo, txtAutor, txtTimeProgres, txtTimeMax;
    ImageButton btnStart, btnSiguiente, btnAnterior, btnRepetir, btnAleatorio;
    MaterialToolbar actionBarReproductor;
    SeekBar sk;
    Handler handler = new Handler();
    MediaPlayer reproductor;
    boolean isPlaying = false;
    boolean aleatorio =false;
    SharedPreferences settings;
    ArrayList<String> listaTitulos, listaAutores, listaCanciones;

    int tema, pantalla, posicion;
    int r = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reproductor);

        //Recoger datos
        if(getIntent().getExtras() != null){
            savedInstanceState = getIntent().getExtras();
            pantalla = savedInstanceState.getInt("pantalla");
            if (pantalla == 3){
                listaTitulos = savedInstanceState.getStringArrayList("listaTitulos");
                listaAutores = savedInstanceState.getStringArrayList("listaAutores");
                listaCanciones = savedInstanceState.getStringArrayList("listaCanciones");
                posicion = savedInstanceState.getInt("posicion");
            }else{
                titulo = savedInstanceState.getString("titulo");
                autor = savedInstanceState.getString("autor");
                categoria = savedInstanceState.getString("categoria");
                archivo = savedInstanceState.getString("archivo");
                ruta = savedInstanceState.getString("ruta");

            }
        }

        txtTitulo = findViewById(R.id.txtTituloReproductor);
        txtAutor = findViewById(R.id.txtAutorReproductor);
        txtTimeProgres = findViewById(R.id.txtTimeProgres);
        txtTimeMax = findViewById(R.id.txtTimeMax);
        btnStart = findViewById(R.id.btnStart);
        btnSiguiente = findViewById(R.id.btnSiguiente);
        btnAnterior = findViewById(R.id.btnAnterior);
        btnRepetir = findViewById(R.id.btnrepetir);
        btnAleatorio = findViewById(R.id.btnAleatorio);
        sk = findViewById(R.id.seekBar);
        actionBarReproductor = findViewById(R.id.actionBarReproductor);

        settings = getSharedPreferences("settings", Context.MODE_PRIVATE);
        tema = settings.getInt("tema", 0);

        actionBarReproductor.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        init();
        setDayNight(tema);
        bCancion();
        progreso();
        siguiente();
        anterior();
        repetir();
        aleatorio();
    }

    public void setDayNight(int mode){
        if(mode == 1){
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    //Funcion que prepara el objeto para reproducir la cancion
    public void init(){
        if (pantalla == 3){
            txtTitulo.setText(listaTitulos.get(posicion));
            txtAutor.setText(listaAutores.get(posicion));

            try {
                reproductor = new MediaPlayer();
                reproductor.setDataSource(listaCanciones.get(posicion));
                reproductor.prepare();
                txtTimeMax.setText(milisegundoAMinutosSegundos(reproductor.getDuration()));
                txtTimeProgres.setText("00:00");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else{
            txtTitulo.setText(titulo);
            txtAutor.setText(autor);

            try {
                reproductor = new MediaPlayer();
                reproductor.setDataSource(ruta);
                reproductor.prepare();
                txtTimeMax.setText(milisegundoAMinutosSegundos(reproductor.getDuration()));
                txtTimeProgres.setText("00:00");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    //Funcion que actualiza la barra de duracion
    public void progreso(){
        sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    reproductor.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    //Funcion que ejecuta hilo escuchando el tiempo de duracion
    public void playCycle() {
        txtTimeProgres.setText(milisegundoAMinutosSegundos(reproductor.getCurrentPosition()));
        sk.setProgress(reproductor.getCurrentPosition());


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                playCycle();
            }
        }, 100);
    }

    //Boton play/pause
    private void bCancion(){

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPlaying){
                    sk.setMax(reproductor.getDuration());
                    reproductor.start();
                    isPlaying=true;
                    btnStart.setImageResource(R.drawable.ic_baseline_pause_circle_24);
                    playCycle();
                }else{
                    reproductor.pause();
                    btnStart.setImageResource(R.drawable.ic_baseline_play);
                    isPlaying=false;
                }
            }
        });
    }

    //Funcion que parse de milisegundos a segundos
    public String milisegundoAMinutosSegundos(int milisegundos){
        int segundos;
        int minutos;
        String tiempo;

        minutos = (milisegundos/1000)/60;

        segundos = (milisegundos/1000)%60;

        if(minutos<10){
            tiempo = "0"+minutos+":";
        }else{
            tiempo=minutos+":";
        }

        if(segundos<10){
            tiempo+="0"+segundos;
        }else{
            tiempo+=segundos;
        }

        return tiempo;
    }


    //Boton de siguiente cancion
    public void siguiente(){
        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pantalla == 3){
                    if (!reproductor.isLooping()){
                        if (aleatorio){
                            Random r = new Random();
                            posicion = r.nextInt(listaCanciones.size());
                        }else{
                            posicion++;
                            if (posicion == listaCanciones.size()){
                                posicion = 0;
                            }
                        }
                    }

                    reproductor.stop();

                    init();
                    sk.setMax(reproductor.getDuration());
                    reproductor.start();
                    isPlaying=true;
                    btnStart.setImageResource(R.drawable.ic_baseline_pause_circle_24);
                    playCycle();
                }else{
                    try {
                        reproductor.stop();
                        reproductor.prepare();
                        reproductor.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }


    //Boton de cancion anterior
    public void anterior(){
        btnAnterior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pantalla == 3){
                    if (!reproductor.isLooping()){
                        if (aleatorio){
                            Random r = new Random();
                            posicion = r.nextInt(listaCanciones.size());
                        }else{
                            posicion--;
                            if (posicion < 0){
                                posicion = (listaCanciones.size()-1);
                            }
                        }
                    }

                    reproductor.stop();

                    init();
                    sk.setMax(reproductor.getDuration());
                    reproductor.start();
                    isPlaying=true;
                    btnStart.setImageResource(R.drawable.ic_baseline_pause_circle_24);
                    playCycle();
                }else{
                    try {
                        reproductor.stop();
                        reproductor.prepare();
                        reproductor.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }


    //Boton de repetir cancion o repetir lista
    public void repetir(){
        btnRepetir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reproductor.isLooping()){
                    reproductor.setLooping(false);
                    btnRepetir.setImageResource(R.drawable.ic_baseline_repeat_24);
                }else{
                    reproductor.setLooping(true);
                    btnRepetir.setImageResource(R.drawable.ic_baseline_repeat_one);
                }
            }
        });
    }

    public void aleatorio(){
        btnAleatorio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (aleatorio == false){
                    aleatorio = true;
                    btnAleatorio.setImageResource(R.drawable.ic_baseline_sync_alt_24);
                }else{
                    aleatorio = false;
                    btnAleatorio.setImageResource(R.drawable.ic_baseline_shuffle_24);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        reproductor.stop();
    }
}