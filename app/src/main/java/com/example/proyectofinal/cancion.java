package com.example.proyectofinal;

import android.net.Uri;

public class cancion {

    private String titulo;
    private String autor;
    private String categoria;
    private String archivo;
    private String ruta;

    public cancion(){};

    public cancion(String titulo, String autor, String categoria, String archivo, String ruta) {
        this.titulo = titulo;
        this.autor = autor;
        this.categoria = categoria;
        this.archivo = archivo;
        this.ruta = ruta;

    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getArchivo() {
        return archivo;
    }

    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }

    public  String getRuta(){return ruta;}

    public void setRuta(String ruta){this.ruta = ruta;}


}
