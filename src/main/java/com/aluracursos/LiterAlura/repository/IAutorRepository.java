package com.aluracursos.LiterAlura.repository;

import com.aluracursos.LiterAlura.model.Autor;
import com.aluracursos.LiterAlura.model.Lenguaje;
import com.aluracursos.LiterAlura.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;


public interface IAutorRepository extends JpaRepository<Autor, Long> {
    @Query("SELECT a FROM Libro l JOIN l.autor a WHERE a.nombre LIKE %:nombre%")
    Optional<Autor> buscarAutorPorNombre(String nombre);

    @Query("SELECT l FROM Libro l JOIN l.autor a WHERE l.titulo LIKE %:nombre%")
    Optional<Libro> buscarLibroPorNombre(String nombre);

//    @Query("SELECT l FROM Libro l JOIN l.autor")
//    List<Libro> buscarTodosLosLibros();

    @Query("SELECT l FROM Autor a JOIN a.libros l")
    List<Libro> buscarTodosLosLibros();

    @Query("SELECT a FROM Autor a WHERE a.fallecimiento > :fecha")
    List<Autor> buscarAutoresVivos(Integer fecha);

    @Query("SELECT l FROM Autor a JOIN a.libros l WHERE l.lenguaje = :idioma ")
    List<Libro> buscarLibrosPorIdioma(Lenguaje idioma);

    @Query("SELECT l FROM Autor a JOIN a.libros l ORDER BY l.descarga DESC LIMIT 10")
    List<Libro> top10Libros();

    @Query("SELECT a FROM Autor a WHERE a.nacimiento = :fecha")
    List<Autor> ListarAutoresPorNacimiento(Integer fecha);

    @Query("SELECT a FROM Autor a WHERE a.fallecimiento = :fecha")
    List<Autor> ListarAutoresPorFallecimiento(Integer fecha);

}

