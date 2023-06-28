package com.example.proyectofinal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;

public class account_fragment extends Fragment {

    View vista;
    Button btnCerrar;
    Button btnEditar;
    TextView nombre_perfil;
    MaterialToolbar menu;
    RecyclerView r;
    ListAdapter lista;
    FirebaseFirestore bd = FirebaseFirestore.getInstance();
    SharedPreferences pref;
    ImageView foto_perfil;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        vista = inflater.inflate(R.layout.fragment_account_fragment, container, false);
        btnCerrar = vista.findViewById(R.id.btnCerrar);
        btnEditar = vista.findViewById(R.id.editar_perfil);
        nombre_perfil = vista.findViewById(R.id.nombre_perfil);
        foto_perfil = vista.findViewById(R.id.foto_perfil);
        menu = vista.findViewById(R.id.actionBarAccount);
        r = vista.findViewById(R.id.recicler);
        pref = getActivity().getSharedPreferences("users_file", Context.MODE_PRIVATE);

        menuInit();

        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), editarPerfil.class);
                startActivity(i);
            }
        });

        init();
        datosUsuario();

        // Inflate the layout for this fragment
        return vista;


    }


    //Funcion de los botones de configuracion y subir cancion
    private void menuInit(){
        menu.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.configuracion){
                    Intent i = new Intent(getContext(), configuracion.class);
                    startActivity(i);
                }

                if(item.getItemId() == R.id.subir){
                    Intent i = new Intent(getContext(), subirCancion.class);
                    startActivity(i);
                }

                return true;
            }
        });
    }


    //Funcion que recoge las canciones subidas por el usario y las muestra en la pantalla
    private void init(){


        r.setLayoutManager(new LinearLayoutManager(getContext()));

        CollectionReference c = bd.collection("users").document(pref.getString("email", null)).collection("misCanciones");

        Query q = c;


        q.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Toast.makeText(getContext(), "Error al recuperar cancines", Toast.LENGTH_SHORT).show();
                }else{
                    List<cancion> canciones = value.toObjects(cancion.class);
                    lista = new ListAdapter(canciones, 2, null);
                    r.setAdapter(lista);
                }
            }
        });
    }

    private void datosUsuario(){
        bd.collection("users").document(pref.getString("email", null))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot documento = task.getResult();
                            if (documento.exists()){
                                nombre_perfil.setText(documento.getData().get("nombre").toString());
                                if (documento.getData().get("foto") != null){
                                    Picasso.get().load(documento.getData().get("foto").toString()).transform(new Transformation() {
                                        @Override
                                        public Bitmap transform(Bitmap source) {
                                            int size = Math.min(source.getWidth(), source.getHeight());

                                            int x = (source.getWidth() - size) / 2;
                                            int y = (source.getHeight() - size) / 2;

                                            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
                                            if (squaredBitmap != source) {
                                                source.recycle();
                                            }

                                            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

                                            Canvas canvas = new Canvas(bitmap);
                                            Paint paint = new Paint();
                                            BitmapShader shader = new BitmapShader(squaredBitmap,
                                                    Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                                            paint.setShader(shader);
                                            paint.setAntiAlias(true);

                                            float r = size / 2f;
                                            canvas.drawCircle(r, r, r, paint);

                                            squaredBitmap.recycle();
                                            return bitmap;
                                        }

                                        @Override
                                        public String key() {
                                            return "circle";
                                        }
                                    }).into(foto_perfil);
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        datosUsuario();
    }
}