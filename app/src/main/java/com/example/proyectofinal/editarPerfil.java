package com.example.proyectofinal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.HashMap;
import java.util.Map;

public class editarPerfil extends AppCompatActivity {

    MaterialToolbar actionBarPerfil;
    ImageView foto_perfilEditada;
    Button seleccionFoto, aplicarCambios;
    TextView nombre_perfilEditado;
    EditText nombreEditar;
    final int galeria = 150;
    Uri fotoSeleccionada;
    FirebaseFirestore bd = FirebaseFirestore.getInstance();
    StorageReference storf = FirebaseStorage.getInstance().getReference();
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        actionBarPerfil = findViewById(R.id.actionBarPerfil);
        seleccionFoto = findViewById(R.id.seleccionFoto);
        aplicarCambios = findViewById(R.id.aplicarCambios);
        foto_perfilEditada = findViewById(R.id.foto_perfilEditada);
        nombreEditar = findViewById(R.id.nombreEdtiar);

        pref = getSharedPreferences("users_file", Context.MODE_PRIVATE);

        actionBarPerfil.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        cargaFoto();
        init();
    }

    private void cargaFoto(){
        if (fotoSeleccionada != null){
            Picasso.get().load(fotoSeleccionada).transform(new Transformation() {
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
            }).into(foto_perfilEditada);
        }else{
            bd.collection("users").document(pref.getString("email", null))
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()){
                                DocumentSnapshot documento = task.getResult();
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
                                    }).into(foto_perfilEditada);
                                }
                            }
                        }
                    });
        }

    }

    private void init(){
        seleccionFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/");
                startActivityForResult(i,galeria);
            }
        });

        aplicarCambios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> datos = new HashMap<>();

                if (!nombreEditar.getText().toString().isEmpty()){
                    if (fotoSeleccionada != null){

                        StorageReference img = storf.child("imagenes").child(fotoSeleccionada.getLastPathSegment());

                        img.putFile(fotoSeleccionada).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                if (taskSnapshot.getMetadata() != null) {
                                    if (taskSnapshot.getMetadata().getReference() != null) {
                                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                String fotoUrl = uri.toString();

                                                datos.put("nombre", nombreEditar.getText().toString());
                                                datos.put("foto", fotoUrl);
                                                bd.collection("users").document(pref.getString("email", null))
                                                        .update(datos);
                                                finish();
                                            }
                                        });
                                    }
                                }
                            }
                        });

                    }else{
                        Toast.makeText(editarPerfil.this, "Tiene que seleccionar una foto", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(editarPerfil.this, "El campo nombre es obligatorio", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == galeria && resultCode == RESULT_OK){
            fotoSeleccionada = data.getData();
            cargaFoto();
        }
    }
}