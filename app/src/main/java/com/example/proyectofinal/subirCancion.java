package com.example.proyectofinal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class subirCancion extends AppCompatActivity {

    FirebaseFirestore bdSubir;

    Button btnSubir, btnElegir;
    EditText txtTitulo, txtAutor, txtCategoria;
    TextView txtSeleccionada;
    SharedPreferences pref;
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    Uri ruta;
    static final int requestMusic = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subir_cancion);
        setTitle("Subir cancion");


        btnSubir = findViewById(R.id.btnSubir);
        btnElegir = findViewById(R.id.btnCargar);
        txtTitulo = findViewById(R.id.txtTitulo);
        txtAutor = findViewById(R.id.txtAutor);
        txtCategoria = findViewById(R.id.txtCategoria);
        txtSeleccionada = findViewById(R.id.txtCancionSeleccionada);
        pref = getSharedPreferences("users_file", Context.MODE_PRIVATE);

        elegirCancion();
        subirCancion();
    }

    //Funcion para seleccionar el archivo mp3
    private void elegirCancion(){
        btnElegir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("audio/");
                startActivityForResult(i, requestMusic);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == requestMusic && resultCode == RESULT_OK){
            ruta = data.getData();
            txtSeleccionada.setText(ruta.getLastPathSegment());
        }
    }

    //Funcion que recoge los datos de la cancion y las sube al storage y firestore
    private void subirCancion(){
        btnSubir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txtAutor.getText().toString().isEmpty() && !txtTitulo.getText().toString().isEmpty() && !txtCategoria.getText().toString().isEmpty()) {
                    bdSubir = FirebaseFirestore.getInstance();
                    StorageReference musica = storageRef.child("canciones/").child(ruta.getLastPathSegment());

                    Map<String, Object> cancionSubir = new HashMap<>();

                    musica.putFile(ruta)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    //Toast.makeText(subirCancion.this, "Cancion Subida con exito", Toast.LENGTH_SHORT).show();
                                    if (taskSnapshot.getMetadata() != null) {
                                        if (taskSnapshot.getMetadata().getReference() != null) {
                                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    String cancionUrl = uri.toString();

                                                    cancionSubir.put("titulo", txtTitulo.getText().toString());
                                                    cancionSubir.put("autor", txtAutor.getText().toString());
                                                    cancionSubir.put("categoria", txtCategoria.getText().toString());
                                                    cancionSubir.put("archivo", ruta.getLastPathSegment());
                                                    cancionSubir.put("ruta", cancionUrl);

                                                    bdSubir.collection("canciones").add(cancionSubir)
                                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                @Override
                                                                public void onSuccess(DocumentReference documentReference) {
                                                                    Toast.makeText(subirCancion.this, "Cancion añadida con exito", Toast.LENGTH_SHORT).show();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(subirCancion.this, "Error al subir la cancion", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });

                                                    bdSubir.collection("users").document(pref.getString("email", null)).collection("misCanciones").add(cancionSubir)
                                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                @Override
                                                                public void onSuccess(DocumentReference documentReference) {
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                }
                                                            });
                                                }
                                            });
                                        }
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(subirCancion.this, "Error al subir la cancion al storage", Toast.LENGTH_SHORT).show();
                                }
                            });
                    finish();
                }else{
                    Toast.makeText(subirCancion.this, "No puede existir campos vacios", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}