package com.example.proyectofinal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirestoreRegistrar;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class login_App extends AppCompatActivity {

    Button btnLogin;
    Button btnRegistrar;
    Button btnGoogle;
    EditText txtEmail;
    EditText txtPassword;
    SharedPreferences pref;
    FirebaseFirestore bd;


    private final int GOOGLE_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_ProyectoFinal_Carlos_Calvente);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnGoogle = findViewById(R.id.btnGoogle);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        pref = getSharedPreferences("users_file", Context.MODE_PRIVATE);
        bd = FirebaseFirestore.getInstance();


        setUp();
        session();
    }

    //Funcion que crea las funciones para los botones de registrar y hacer login
    private void setUp(){

        setTitle("Autenticacion");

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txtEmail.getText().toString().isEmpty() && !txtPassword.getText().toString().isEmpty()){
                    FirebaseAuth.getInstance()
                            .createUserWithEmailAndPassword(txtEmail.getText().toString(), txtPassword.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        home(txtEmail.getText().toString(), txtPassword.getText().toString());
                                    }else{
                                        mostrarAlerta();
                                    }
                                }
                            });

                }else{
                    mostrarAlertaVacio();
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txtEmail.getText().toString().isEmpty() && !txtPassword.getText().toString().isEmpty()) {
                    FirebaseAuth.getInstance()
                            .signInWithEmailAndPassword(txtEmail.getText().toString(), txtPassword.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        home(txtEmail.getText().toString(), txtPassword.getText().toString());
                                    } else {
                                        mostrarAlerta();
                                    }
                                }
                            });
                }else{
                    mostrarAlertaVacio();
                }
            }
        });

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInOptions googleConf = new GoogleSignInOptions
                        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                GoogleSignInClient client = GoogleSignIn.getClient(v.getContext(), googleConf);
                client.signOut();

                startActivityForResult(client.getSignInIntent(), GOOGLE_SIGN_IN);
            }
        });

    }

    //Funcion que recoge los datos y valida la cuenta de Google
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GOOGLE_SIGN_IN){

                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount cuenta = task.getResult(ApiException.class);

                    if (cuenta != null) {
                        AuthCredential credential = GoogleAuthProvider.getCredential(cuenta.getIdToken(), null);
                        FirebaseAuth.getInstance().signInWithCredential(credential)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {

                                            bd.collection("users").document(cuenta.getEmail()).get()
                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.getResult().getData().get("nombre") == null){
                                                                Map<String, Object> datos = new HashMap<>();
                                                                datos.put("nombre", cuenta.getDisplayName());
                                                                bd.collection("users").document(cuenta.getEmail()).set(datos, SetOptions.merge());
                                                            }
                                                        }
                                                    });

                                            home(cuenta.getEmail(), cuenta.getIdToken());
                                        } else {
                                            mostrarAlerta();
                                        }
                                    }
                                });
                    }
                } catch (ApiException apiException) {
                    mostrarAlerta();
                }

        }
    }

    //Funcion que comprueba que haya una sesion abierta
    private void session(){

        String email = pref.getString("email", null);
        String password = pref.getString("password", null);

        if(email != null && password != null){
            home(pref.getString("email", null), pref.getString("password", null));
        }
    }

    //Funcion que muestra mensaje de error al fallar el login
    private void mostrarAlerta(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Se ha producido un error al autenticar el usuario")
                .setPositiveButton("Aceptar", null);

        AlertDialog alerta = builder.create();
        alerta.show();
    }

    //Funcion que muestra error de que los campos estan vacios
    private void mostrarAlertaVacio(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Los campos email y password son obligatorios")
                .setPositiveButton("Aceptar", null);

        AlertDialog alerta = builder.create();
        alerta.show();
    }

    //Funcion que una vez validada la sesion inicia la pantalla principal
    private void home(String email, String password){
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("email", email);
        i.putExtra("password", password);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

}