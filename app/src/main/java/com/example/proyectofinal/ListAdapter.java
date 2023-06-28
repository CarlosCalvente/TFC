package com.example.proyectofinal;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private List<cancion> mData;
    private List<cancion> listaF;
    private int pantalla;
    private String tituloLista;
    //private LayoutInflater mInflater;

    public ListAdapter(List<cancion> itemList, int pantalla, String tituloLista){
        //this.mInflater = LayoutInflater.from(contexto);
        this.mData = itemList;
        listaF = new ArrayList<>();
        listaF.addAll(itemList);
        this.pantalla = pantalla;
        this.tituloLista = tituloLista;
    }

    //Funcion que permite buscar por titulo y autor
    public void filtrado(String txtBuscar){
        int longitud = txtBuscar.length();

        if(longitud == 0){
            mData.clear();
            mData.addAll(listaF);
        }else{
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                List<cancion> collection = mData.stream()
                        .filter(i -> i.getTitulo().toLowerCase().contains(txtBuscar.toLowerCase()) ||
                                i.getAutor().toLowerCase().contains(txtBuscar.toLowerCase()) ||
                                i.getCategoria().toLowerCase().contains(txtBuscar.toLowerCase()))
                        .collect(Collectors.toList());
                mData.clear();
                mData.addAll(collection);
            }else{
                for(cancion c : listaF){
                    if(c.getTitulo().toLowerCase().contains(txtBuscar.toLowerCase()) ||
                            c.getAutor().toLowerCase().contains(txtBuscar.toLowerCase()) ||
                            c.getCategoria().toLowerCase().contains(txtBuscar.toLowerCase())){
                        mData.clear();
                        mData.add(c);
                    }
                }

            }
        }
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount(){
        return mData.size();
    }

    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.plantilla_canciones, null, false);
        //mInflater.inflate(R.layout.plantilla_canciones, null);

        return new ViewHolder(view);
    }

    //Funcion que te lleva al reproductor al clicar en una cancion
    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") final int position){
        holder.bindData(mData.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (pantalla == 3){
                    ArrayList<String> listaTitulos = new ArrayList<>();
                    ArrayList<String> listaAutores = new ArrayList<>();
                    ArrayList<String> listaCanciones = new ArrayList<>();
                    for (cancion c: mData) {
                        listaTitulos.add(c.getTitulo());
                        listaAutores.add(c.getAutor());
                        listaCanciones.add(c.getRuta());
                    }
                    Intent i = new Intent(holder.itemView.getContext(), reproductor.class);
                    i.putStringArrayListExtra("listaTitulos", listaTitulos);
                    i.putStringArrayListExtra("listaAutores", listaAutores);
                    i.putStringArrayListExtra("listaCanciones", listaCanciones);
                    i.putExtra("posicion", position);
                    i.putExtra("pantalla", pantalla);
                    holder.itemView.getContext().startActivity(i);
                }else{
                    Intent i = new Intent(holder.itemView.getContext(), reproductor.class);
                    i.putExtra("titulo", mData.get(position).getTitulo());
                    i.putExtra("autor", mData.get(position).getAutor());
                    i.putExtra("categoria", mData.get(position).getCategoria());
                    i.putExtra("archivo", mData.get(position).getArchivo());
                    i.putExtra("ruta", mData.get(position).getRuta());
                    i.putExtra("pantalla", pantalla);
                    holder.itemView.getContext().startActivity(i);
                }
            }
        });
    }

    public void setItems(List<cancion> items){
        mData = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder  {

        TextView titulo, autor;
        ImageButton btnOpciones;
        Button btnPop;
        cancion c;
        RecyclerView rListasExisten;
        StorageReference musica = FirebaseStorage.getInstance().getReference();

        FirebaseFirestore bd = FirebaseFirestore.getInstance();
        SharedPreferences pref;

        ViewHolder(View itemView){
            super(itemView);

            titulo = itemView.findViewById(R.id.titulo);
            autor = itemView.findViewById(R.id.autor);
            btnOpciones = itemView.findViewById(R.id.btnOpciones);

            opciones();
        }

        void bindData(final cancion item) {
            titulo.setText(item.getTitulo());
            autor.setText(item.getAutor());
            c = item;
        }

        //Funcion del boton de opcines de las canciones
        void opciones(){
            btnOpciones.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popMenu = new PopupMenu(itemView.getContext(), v);
                    popMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.añadirLista:
                                    LayoutInflater inf = (LayoutInflater) itemView.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                                    View popupView = inf.inflate(R.layout.popup_listacancion, null);

                                    //int height = 800;
                                    //int width = 600;
                                    boolean focusable = true;
                                    PopupWindow popUp = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, focusable);

                                    popUp.showAtLocation(v, Gravity.CENTER, 0, 0);

                                    botonesPopUp(popupView, popUp);
                                    return true;
                                case R.id.eliminarCancion:
                                    pref = v.getContext().getSharedPreferences("users_file", Context.MODE_PRIVATE);
                                    if (pantalla == 2){
                                        bd.collection("users")
                                                .document(pref.getString("email", null))
                                                .collection("misCanciones")
                                                .whereEqualTo("titulo", c.getTitulo())
                                                .whereEqualTo("autor", c.getAutor())
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            for (QueryDocumentSnapshot d : task.getResult()) {
                                                                bd.collection("users")
                                                                        .document(pref.getString("email", null))
                                                                        .collection("misCanciones")
                                                                        .document(d.getId())
                                                                        .delete();
                                                            }

                                                            bd.collection("canciones")
                                                                    .whereEqualTo("titulo", c.getTitulo())
                                                                    .whereEqualTo("autor", c.getAutor())
                                                                    .get()
                                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                            if (task.isSuccessful()) {
                                                                                for (QueryDocumentSnapshot d : task.getResult()) {
                                                                                    StorageReference cEliminar = musica.child("canciones/")
                                                                                            .child(c.getArchivo());

                                                                                    cEliminar.delete();

                                                                                    bd.collection("canciones")
                                                                                            .document(d.getId())
                                                                                            .delete();
                                                                                }
                                                                            }
                                                                        }
                                                                    });
                                                        } else {
                                                            Toast.makeText(v.getContext(), "Error", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                    }else if (pantalla == 3){
                                        bd.collection("users")
                                                .document(pref.getString("email", null))
                                                .collection(tituloLista)
                                                .whereEqualTo("titulo", c.getTitulo())
                                                .whereEqualTo("autor", c.getAutor())
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            for (QueryDocumentSnapshot d : task.getResult()) {
                                                                bd.collection("users")
                                                                        .document(pref.getString("email", null))
                                                                        .collection(tituloLista)
                                                                        .document(d.getId())
                                                                        .delete();
                                                            }
                                                        } else {
                                                            Toast.makeText(v.getContext(), "Error", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                    return true;

                                default:
                                    return false;
                            }
                        }
                    });

                    if (pantalla == 2){
                        popMenu.inflate(R.menu.menu_lista);
                        popMenu.inflate(R.menu.eliminar_cancion);
                    }else if (pantalla == 1){
                        popMenu.inflate(R.menu.menu_lista);
                    }else {
                        popMenu.inflate(R.menu.eliminar_cancion);
                    }

                    popMenu.show();

                }
            });
        }



        //Funcion que crea nueva lista y añade la cancion a esta
        void botonesPopUp(View view, PopupWindow p){

            init(view, p);

            view.findViewById(R.id.btnNuevaLista).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText txtNuevaLista = view.findViewById(R.id.txtNuevaLista);
                    String nombreLista = txtNuevaLista.getText().toString();
                    if(!nombreLista.isEmpty()){
                        pref = view.getContext().getSharedPreferences("users_file", Context.MODE_PRIVATE);
                        Map<String, Object> datos = new HashMap<>();
                        datos.put("listas", Arrays.asList(nombreLista));

                        bd.collection("users")
                                .document(pref.getString("email", null))
                                .collection(nombreLista)
                                .add(c).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Toast.makeText(view.getContext(), "Cancion agregada a " + nombreLista, Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(view.getContext(), "Error al crear la lista nueva", Toast.LENGTH_SHORT).show();
                                    }
                                });


                        bd.collection("users")
                                .document(pref.getString("email", null))
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()){
                                            if(task.getResult().getData().containsKey("listas") == true){
                                                bd.collection("users")
                                                        .document(pref.getString("email", null))
                                                        .update("listas", FieldValue.arrayUnion(nombreLista));
                                            }else{
                                                bd.collection("users")
                                                        .document(pref.getString("email", null))
                                                        .set(datos, SetOptions.merge());
                                            }
                                        }
                                    }
                                });


                        p.dismiss();
                    }else{
                        Toast.makeText(view.getContext(), "El nombre de la lista es obligatorio", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }


        //Funcion que recoge las posibles listas creadas y las muestra
        public void init(View view, PopupWindow p){

            rListasExisten = view.findViewById(R.id.rListasExisten);
            pref = view.getContext().getSharedPreferences("users_file", Context.MODE_PRIVATE);
            bd = FirebaseFirestore.getInstance();

            //new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            rListasExisten.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));

            bd.collection("users").document(pref.getString("email", null)).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            ListaAdapterListas listaListas;
                            if (task.isSuccessful()){
                                DocumentSnapshot d = task.getResult();
                                if(d == null){
                                    Toast.makeText(view.getContext(), "Error al encontrar el documento", Toast.LENGTH_SHORT).show();
                                }else{
                                    Map<String, Object> mapa = d.getData();
                                    if(mapa.isEmpty()){
                                        Toast.makeText(view.getContext(), "Error al recoger los datos", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Object objeto = mapa.get("listas");
                                        if (objeto == null){
                                            Toast.makeText(view.getContext(), "Sin listas", Toast.LENGTH_SHORT).show();
                                        }else{
                                            List<String> listaC = (List<String>) task.getResult().getData().get("listas");

                                            listaListas = new ListaAdapterListas(listaC, c, p);
                                            rListasExisten.setAdapter(listaListas);

                                        }
                                    }
                                }

                            }
                        }
                    });

        }


    }
}
