# 🌟 Conversor de Moneda WRH3 🌟

¡Bienvenido a Literalura WRH3! Este programa te permite consultar diferentes libros utilizando la API "Gutendex" y despues los lamacena en una base de datos para consultar tanto los libros como los autores, permite realizar las siguientes busquedas:
* Buscar libro por titulo
* Listar libros registrados
* Listar autores registrados
* Listar autores vivos en un determinado año
* Listar libros por idioma
* Generar estadisticas
* Top 10 libros
* Buscar autor por nombre
* Listar autores con otras consultas

## 🚀 Instalación

1. Clona este repositorio en tu máquina local:
   git clone https://github.com/walbertoRH3/LiterAlura.git

2. Abre el proyecto en tu IDE Preferido
3. Asegurate de tener Java instalado en tu sistema

## 💼 Uso
Este proyecto unicamnete tiene como finalidad aprovar el CChallenge Literalura
por lo que las funcionalidad son muy limitadas, sin embargo espero te diviertas un rato usandolo.

## 📁 Estructura del Proyecto
El proyecto contiene los siguientes package:
1. [x] model
2. [x] principal
3. [x] repository
4. [x] service

El  package model contine:
<ul>
  <li>La clase Autor</li>
  <li>La clase Libro</li>
  <li>El record Datos</li>
  <li>El record DatosAutor</li>
  <li>El record DatosLibro</li>
  <li>El enum Lenguaje</li>
</ul>

Las clases Autor y Libro son nuestras entidades qeu permiten mapear nuestras clases a la base de datos que tenmos creda con el nombre de alura_libros

````java
@Entity
@Table(name = "autores")
public class Autor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String nombre;
    private Integer nacimiento;
    private Integer fallecimiento;
    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Libro> libros;

    public Autor() {

    }

    public Autor(DatosAutor datosAutor) {
        this.nombre = datosAutor.nombre();
        this.nacimiento = datosAutor.nacimiento();
        this.fallecimiento = datosAutor.fallecimiento();
    }
}   
````

````java
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

    public Libro(DatosLibro libro) {
        this.id = libro.id();
        this.titulo = libro.titulo();
        this.lenguaje = Lenguaje.fromString(libro.lenguajes().stream()
                .limit(1).collect(Collectors.joining()));
        this.copyright = libro.copyright();
        this.descarga = libro.descarga();
    }
}   
````
con sus respectivos Seter y Geter

Datos es un record que nos facilita trabajar con las variables que vamos a utilizar para manipular los datos que recibimos de la API al traer todos los libros que se encuentran en ella:

````java
@JsonIgnoreProperties(ignoreUnknown = true)
public record Datos(
        @JsonAlias("count") Integer total,
        @JsonAlias("results") List<DatosLibro> libros) {
}
````
Ademas los record de DatosAutor y DatosLibro nos permiten trabajar con las variables que necesitamos en cada caso par apoder manipular los datos de los libros y los autores.

````java
@JsonIgnoreProperties(ignoreUnknown = true)
public record DatosAutor(
        @JsonAlias("name") String nombre,
        @JsonAlias("birth_year") Integer nacimiento,
        @JsonAlias("death_year") Integer fallecimiento
) {
}
````

````java
@JsonIgnoreProperties(ignoreUnknown = true)
public record DatosLibro(
        @JsonAlias("id") Long id,
        @JsonAlias("title") String titulo,
        @JsonAlias("authors") List<DatosAutor> autores,
        @JsonAlias("languages") List<String> lenguajes,
        @JsonAlias("copyright") String copyright,
        @JsonAlias("download_count") Integer descarga) {
}
````
Finalmente el Enum de Lengauje nos pertite trabajar con los idiomas de los libros de una manera mas controlada y evitando que el usuario introduzca datos inecesarios o no deseados.

````java
public enum Lenguaje {
    ES("es"),
    EN("en"),
    FR("fr"),
    PT("pt");

    private String idioma;

    Lenguaje(String idioma) {
        this.idioma = idioma;
    }

    public static Lenguaje fromString(String text){
        for (Lenguaje lenguaje : Lenguaje.values()){
            if(lenguaje.idioma.equalsIgnoreCase(text)){
                return lenguaje;
            }
        }
        throw new IllegalArgumentException("Ningun lenguaje encontrado: " + text);
    }

    public String getIdioma(){
        return this.idioma;
    }
}
````
El  package repository contine:
<ul>
  <li>La Interface IAutorRepository</li>
</ul>

Esta interfaz extiende de JpaRepository nos sirve para poder obtener los datos de nuestra base de datos utilizando PQL

````java
public interface IAutorRepository extends JpaRepository<Autor, Long> {
    @Query("SELECT a FROM Libro l JOIN l.autor a WHERE a.nombre LIKE %:nombre%")
    Optional<Autor> buscarAutorPorNombre(String nombre);

    @Query("SELECT l FROM Libro l JOIN l.autor a WHERE l.titulo LIKE %:nombre%")
    Optional<Libro> buscarLibroPorNombre(String nombre);
    
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
````
El  package service contine:
<ul>
  <li>La clase ConsumoAPI</li>
  <li>La clase ConvierteDatos</li>
  <li>La interface IConvierteDatos</li>
</ul>

La clase ConsumnoAPI se encarga de realizar la peticion a la api.

````java
public class ConsumoAPI {
   public String obtenerDatos(String url){
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
              .build();
      HttpResponse<String> response = null;
      try {
         response = client.send(request, HttpResponse.BodyHandlers.ofString());
      } catch (IOException e) {
         throw new RuntimeException(e);
      } catch (InterruptedException e) {
         throw new RuntimeException(e);
      }
      String json = response.body();
      return json;
   }
}
````
El  package principal contine:
<ul>
  <li>La clase Principal</li>
</ul>

Esta clase es la que contiene toda la logica de nuestro programa y los metodos para que todas las opciones del menu funcionen de manera correcta

````java
public class Principal {
    private Scanner entrada = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private String URL_BASE = "https://gutendex.com/books/";
    private IAutorRepository repository;

    public Principal(IAutorRepository repository) {
        this.repository = repository;
    }

    public void mostrarMenu() {
        var opcion = -1;
        var menu = """
                -------------
                Elija la opcion a través de su numero:
                1 - Buscar libro por titulo
                2 - Listar libros registrados
                3 - Listar autores registrados
                4 - Listar autores vivos en un determinado año
                5 - Listar libros por idioma
                6 - Generar estadisticas
                7 - Top 10 libros
                8 - Buscar autor por nombre
                9 - Listar autores con otras consultas
                0 - Salir
                """;
        while (opcion != 0) {
            System.out.println(menu);
            try {
                opcion = Integer.valueOf(entrada.nextLine());
                switch (opcion) {
                    case 1:
                        buscarLibroPorTitulo();
                        break;
                    case 2:
                        listarLibrosRegistrados();
                        break;
                    case 3:
                        listarAutoresRegistrados();
                        break;
                    case 4:
                        listarAutoresVivos();
                        break;
                    case 5:
                        listarLibrosPorIdioma();
                        break;
                    case 6:
                        generarEstadisticas();
                        break;
                    case 7:
                        top10Libros();
                        break;
                    case 8:
                        buscarAutorPorNombre();
                        break;
                    case 9:
                        listarAutoresConOtrasConsultas();
                        break;
                    case 0:
                        System.out.println("Gracias por usar LiterAlura");
                        System.out.println("Cerrando la aplicacion...");
                        break;
                    default:
                        System.out.println("Opcion no valida!");
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Opcion no valida: " + e.getMessage());

            }
        }
    }
}
````
Finalmente cada uno de los metodos usados permiten el buen funcionamiento de la aplicacion.
Por ejemplo el metodo buscarLibroPorTitulo nos permite obtenr un libro de la API Gutendex mediante el titulo del libro, si el titulo se encuentra en la api nos devuelve los datos 
del libro manejados de forma personalizada, para esto usamos la libreria de jackson, si el libro se encuntra ademas de mostralo en pantalla tambien lo guarda en la base de datos y si
no se encuentra manda un mensaje inidcando Libro no encontrado!

````java
public void buscarLibroPorTitulo(){
   System.out.println("Introduce el nombre del libro que deseas buscar:");
   var nombre = entrada.nextLine();
   var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + nombre.replace(" ","+"));
   var datos = conversor.obtenerDatos(json, Datos.class);
   Optional<DatosLibro> libroBuscado = datos.libros().stream()
           .findFirst();
   if(libroBuscado.isPresent()){
      System.out.println(
              "\n----- LIBRO -----" +
                      "\nTitulo: " + libroBuscado.get().titulo() +
                      "\nAutor: " + libroBuscado.get().autores().stream()
                      .map(a -> a.nombre()).limit(1).collect(Collectors.joining())+
                      "\nIdioma: " + libroBuscado.get().lenguajes().stream().collect(Collectors.joining()) +
                      "\nNumero de descargas: " + libroBuscado.get().descarga() +
                      "\n-----------------\n"
      );

      try{
         List<Libro> libroEncontrado = libroBuscado.stream().map(a -> new Libro(a)).collect(Collectors.toList());
         Autor autorAPI = libroBuscado.stream().
                 flatMap(l -> l.autores().stream()
                         .map(a -> new Autor(a)))
                 .collect(Collectors.toList()).stream().findFirst().get();
         Optional<Autor> autorBD = repository.buscarAutorPorNombre(libroBuscado.get().autores().stream()
                 .map(a -> a.nombre())
                 .collect(Collectors.joining()));
         Optional<Libro> libroOptional = repository.buscarLibroPorNombre(nombre);
         if (libroOptional.isPresent()) {
            System.out.println("El libro ya está guardado en la base de datos.");
         } else {
            Autor autor;
            if (autorBD.isPresent()) {
               autor = autorBD.get();
               System.out.println("EL autor ya esta guardado en la BD!");
            } else {
               autor = autorAPI;
               repository.save(autor);
            }
            autor.setLibros(libroEncontrado);
            repository.save(autor);
         }
      } catch(Exception e) {
         System.out.println("Advertencia! " + e.getMessage());
      }
   } else {
      System.out.println("Libro no encontrado!");
   }
}
````
El metodo listarLibrosRegistrados nos permite obtener todos los libros registrados en nuestra BD y mostrarlos en pantalla con un formato personalizado.

````java
public void listarLibrosRegistrados(){
List<Libro> libros = repository.buscarTodosLosLibros();
libros.forEach(l -> System.out.println(
"----- LIBRO -----" +
"\nTitulo: " + l.getTitulo() +
"\nAutor: " + l.getAutor().getNombre() +
"\nIdioma: " + l.getLenguaje().getIdioma() +
"\nNumero de descargas: " + l.getDescarga() +
"\n-----------------\n"
));
}
````

El metodo listarAutoresRegistrados nos permite obtener todos los Autores registrados en nuestra BD y mostrarlos en pantalla con un formato personalizado.

````java
public void listarAutoresRegistrados(){
   List<Autor> autores = repository.findAll();
   System.out.println();
   autores.forEach(l-> System.out.println(
           "Autor: " + l.getNombre() +
                   "\nFecha de nacimiento: " + l.getNacimiento() +
                   "\nFecha de fallecimiento: " + l.getFallecimiento() +
                   "\nLibros: " + l.getLibros().stream()
                   .map(t -> t.getTitulo()).collect(Collectors.toList()) + "\n"
   ));
}
````

El metodo listarAutoresVivos nos permite obtener todos los Autores registrados en nuestra BD los cuales tienen un año de fallecimeinto >= que el año ingresado por el usuario

````java
public void listarAutoresVivos(){
System.out.println("Introduce el año vivo del autor(es) que deseas buscar:");
try{
var fecha = Integer.valueOf(entrada.nextLine());
List<Autor> autores = repository.buscarAutoresVivos(fecha);
if(!autores.isEmpty()){
System.out.println();
autores.forEach(a -> System.out.println(
"Autor: " + a.getNombre() +
"\nFecha de nacimiento: " + a.getNacimiento() +
"\nFecha de fallecimiento: " + a.getFallecimiento() +
"\nLibros: " + a.getLibros().stream()
.map(l -> l.getTitulo()).collect(Collectors.toList()) + "\n"
));
} else{
System.out.println("No hay autores vivos en ese año registradoe en la BD!");
}
} catch(NumberFormatException e){
System.out.println("introduce un año valido " + e.getMessage());
}
}
````
El metodo listarLibrosPorIdioma() nos permite obtener los libro registrados en nuestra BD los cuales tienen el idioma introducido por el usuario, si el idioma no esta en nuestra BD
se le informa al usuario que No hay libros registrados en ese idioma! ademas si no introduce un idioma en el formato valido se le envia un alerta.

````java
public void listarLibrosPorIdioma(){
   var menu = """
           Ingrese el idioma para buscar los libros:
           es - español
           en - inglés
           fr - francés
           pt - portugués
           """;
   System.out.println(menu);
   var idioma = entrada.nextLine();
   if(idioma.equalsIgnoreCase("es") || idioma.equalsIgnoreCase("en") ||
           idioma.equalsIgnoreCase("fr") || idioma.equalsIgnoreCase("pt")){
      Lenguaje lenguaje = Lenguaje.fromString(idioma);
      List<Libro> libros = repository.buscarLibrosPorIdioma(lenguaje);
      if(libros.isEmpty()){
         System.out.println("No hay libros registrados en ese idioma!");
      } else{
         System.out.println();
         libros.forEach(l -> System.out.println(
                 "----- LIBRO -----" +
                         "\nTitulo: " + l.getTitulo() +
                         "\nAutor: " + l.getAutor().getNombre() +
                         "\nIdioma: " + l.getLenguaje().getIdioma() +
                         "\nNumero de descargas: " + l.getDescarga() +
                         "\n-----------------\n"
         ));
      }
   } else{
      System.out.println("Introduce un idioma en el formato valido");
   }
}
````
El metodo generarEstadisticas nos permite obtener las estadisticas de todos los libros que se encuentran en laa API basandonos en las descargas, para esto excluimos aquellos libros 
que no tienen descargas registrada.

````java
public void generarEstadisticas(){
   var json = consumoAPI.obtenerDatos(URL_BASE);
   var datos = conversor.obtenerDatos(json, Datos.class);
   IntSummaryStatistics est = datos.libros().stream()
           .filter(l -> l.descarga() > 0)
           .collect(Collectors.summarizingInt(DatosLibro::descarga));
   System.out.println("Cantidad media de descargas: " + est.getAverage());
   System.out.println("Cantidad maxima de descargas: " + est.getMax());
   System.out.println("Cantidad minima de descargas: " + est.getMin());
   System.out.println("Cantidad de registros evaluados para calcular las estadisticas: " + est.getCount());
}
````

El metodo top10Libros nos permite obtener los 10 libros mas descargados qeu se encuentran registrados en nuestra Base de datos.

````java
public void top10Libros(){
   List<Libro> libros = repository.top10Libros();
   System.out.println();
   libros.forEach(l -> System.out.println(
           "----- LIBRO -----" +
                   "\nTitulo: " + l.getTitulo() +
                   "\nAutor: " + l.getAutor().getNombre() +
                   "\nIdioma: " + l.getLenguaje().getIdioma() +
                   "\nNumero de descargas: " + l.getDescarga() +
                   "\n-----------------\n"
   ));
}
````

El metodo buscarAutorPorNombre nos permite buscar los autores por un nombre que el usuario introduzca, esta busqueda la hacemos dentro de nuestra Base de datos

````java
public void buscarAutorPorNombre(){
   System.out.println("Ingrese el nombre del autor que deseas buscar:");
   var nombre = entrada.nextLine();
   Optional<Autor> autor = repository.buscarAutorPorNombre(nombre);
   if(autor.isPresent()){
      System.out.println(
              "\nAutor: " + autor.get().getNombre() +
                      "\nFecha de nacimiento: " + autor.get().getNacimiento() +
                      "\nFecha de fallecimiento: " + autor.get().getFallecimiento() +
                      "\nLibros: " + autor.get().getLibros().stream()
                      .map(l -> l.getTitulo()).collect(Collectors.toList()) + "\n"
      );
   } else {
      System.out.println("El autor no existe en la BD!");
   }
}
````

El metodo listarAutoresConOtrasConsultas nos permite reqalizar una busqueda de autores dentro de nuestra base de datos con las siguientes opciones:

1. [ ] Listar autor por Año de nacimiento
2. [ ] Listar autor por año de fallecimiento

por lo que el usuario elige una opcion y despues inserta un año para realizar la busqueda
````java
public void listarAutoresConOtrasConsultas(){
   var menu = """
           Ingrese la opcion por la cual desea listar los autores
           1 - Listar autor por Año de nacimiento
           2 - Listar autor por año de fallecimiento
           """;
   System.out.println(menu);
   try{
      var opcion = Integer.valueOf(entrada.nextLine());
      switch (opcion){
         case 1:
            ListarAutoresPorNacimiento();
            break;
         case 2:
            ListarAutoresPorFallecimiento();
            break;
         default:
            System.out.println("Opcion invalida!");
            break;
      }
   } catch (NumberFormatException e) {
      System.out.println("Opcion no valida: " + e.getMessage());
   }
}
````
Metodo para buscar un autor por año de nacimiento:

````java
public void ListarAutoresPorNacimiento(){
   System.out.println("Introduce el año de nacimiento que deseas buscar:");
   try{
      var nacimiento = Integer.valueOf(entrada.nextLine());
      List<Autor> autores = repository.ListarAutoresPorNacimiento(nacimiento);
      if(autores.isEmpty()){
         System.out.println("No existen autores con año de nacimeinto igual a " + nacimiento);
      } else {
         System.out.println();
         autores.forEach(a -> System.out.println(
                 "Autor: " + a.getNombre() +
                         "\nFecha de nacimiento: " + a.getNacimiento() +
                         "\nFecha de fallecimeinto: " + a.getFallecimiento() +
                         "\nLibros: " + a.getLibros().stream().map(l -> l.getTitulo()).collect(Collectors.toList()) + "\n"
         ));
      }
   } catch (NumberFormatException e){
      System.out.println("Año no valido: " + e.getMessage());
   }
}
````

Metodo para buscar un autor por año de fallecimiento:
````java
public void ListarAutoresPorFallecimiento(){
   System.out.println("Introduce el año de fallecimiento que deseas buscar:");
   try{
      var fallecimiento = Integer.valueOf(entrada.nextLine());
      List<Autor> autores = repository.ListarAutoresPorFallecimiento(fallecimiento);
      if(autores.isEmpty()){
         System.out.println("No existen autores con año de fallecimiento igual a " + fallecimiento);
      } else {
         System.out.println();
         autores.forEach(a -> System.out.println(
                 "Autor: " + a.getNombre() +
                         "\nFecha de fallecimiento: " + a.getNacimiento() +
                         "\nFecha de fallecimeinto: " + a.getFallecimiento() +
                         "\nLibros: " + a.getLibros().stream().map(l -> l.getTitulo()).collect(Collectors.toList()) + "\n"
         ));
      }
   } catch (NumberFormatException e) {
      System.out.println("Opcion no valida: " + e.getMessage());
   }
}
````


## ✅ Tecnologías Utilizadas
- Java 🔧
- Maven 🔧
- Spring Framework 🔧
- Jackson Databind 🔧
- Spring Data JPA 🔧
- MySQL Driver 🔧

## 🌟 Funcionalidades
- Conversión de monedas 🛠️
- Buscar libro por titulo 🛠️
- Listar libros registrados 🛠️
- Listar autores registrados 🛠️
- Listar autores vivos en un determinado año 🛠️
- Listar libros por idioma 🛠️
- Generar estadisticas 🛠️
- Top 10 libros 🛠️
- Buscar autor por nombre 🛠️ 
- Listar autores con otras consultas 🛠️ 
- Interfaz de usuario amigable 🛠️
- Integración con API ExchageRate 🛠️


## 🤝 Contribución
mis profesores de Alura Latam que me han ensañado mucho y siempre muy profesionales.

## 📝 Licencia

Este proyecto está bajo la licencia MIT.

¡Gracias por utilizar Literalura WRH3! Espero que te sea útil y que tengas una excelente experiencia Consultando diferentes libros y Autores. ¡Feliz Literatura! 🚀✨

## Autores

1. Walberto Roblero Hernandez
