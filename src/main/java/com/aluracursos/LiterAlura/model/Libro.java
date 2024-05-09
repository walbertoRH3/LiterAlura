package com.aluracursos.LiterAlura.model;

import jakarta.persistence.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "libros")
public class Libro {
    @Id
    private Long id;
    private String titulo;
    @Enumerated(EnumType.STRING)
    private Lenguaje lenguaje;
    private String copyright;
    private Integer descarga;
    @ManyToOne
    private Autor autor;

    public Libro() {
    }

    public Libro(DatosLibro libro){
        this.id = libro.id();
        this.titulo = libro.titulo();
        this.lenguaje = Lenguaje.fromString(libro.lenguajes().stream()
                .limit(1).collect(Collectors.joining()));
        this.copyright = libro.copyright();
        this.descarga = libro.descarga();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Lenguaje getLenguaje() {
        return lenguaje;
    }

    public void setLenguaje(Lenguaje lenguaje) {
        this.lenguaje = lenguaje;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public Integer getDescarga() {
        return descarga;
    }

    public void setDescarga(Integer descarga) {
        this.descarga = descarga;
    }

    public Autor getAutor() {
        return autor;
    }

    public void setAutor(Autor autor) {
        this.autor = autor;
    }

    @Override
    public String toString() {
        return
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", lenguaje=" + lenguaje +
                ", copyright='" + copyright + '\'' +
                ", descarga=" + descarga +
                ", autor=" + autor;
    }
}
