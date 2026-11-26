
package juego;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import entidades.Entidad;
import entidades.Enemigo;

import java.util.List;
import java.util.Map;

public class GestorNiveles {

    private final DatosJuego datos;

    public GestorNiveles(DatosJuego datos) {
        this.datos = datos;
    }

    // Cargar los enemigos del capitulo desde niveles.json
    public Enemigo cargarCapitulo(
            int numeroCapitulo,
            List<Entidad> entidades,
            List<Enemigo> enemigos,
            Map<Enemigo, Integer> posiciones) {

        // Eliminar enemigos del capitulo anterior
        entidades.removeIf(e -> e instanceof Enemigo);

        enemigos.clear();
        posiciones.clear();

        JsonObject nivel = datos.obtenerNivel(numeroCapitulo);

        JsonArray enemigosNivel =
                nivel.getAsJsonArray("enemigos");

        for (JsonElement elemento : enemigosNivel) {

            JsonObject configuracion =
                    elemento.getAsJsonObject();

            int tipo = configuracion
                    .get("tipo")
                    .getAsInt();

            int posicionX = configuracion
                    .get("x")
                    .getAsInt();

            Enemigo nuevo = crearEnemigo(tipo);

            enemigos.add(nuevo);
            entidades.add(nuevo);
            posiciones.put(nuevo, posicionX);
        }

        System.out.println(
            "Capitulo " + numeroCapitulo
            + " cargado desde JSON. Enemigos: "
            + enemigos.size()
        );

        return enemigos.isEmpty()
                ? null
                : enemigos.get(0);
    }

    // Delegar la construccion al patron Factory Method
    private Enemigo crearEnemigo(int tipo) {

        JsonObject configuracion =
                datos.obtenerEnemigo(tipo);

        String clase = configuracion
                .get("clase")
                .getAsString();

        CreadorEnemigo creador;

        switch (clase) {

            case "Enemigo":
                creador =
                    new CreadorEnemigo.CreadorNormal();
                break;

            case "Boss":
                creador =
                    new CreadorEnemigo.CreadorJefe();
                break;

            default:
                throw new IllegalArgumentException(
                    "Clase de enemigo desconocida: " + clase
                );
        }

        return creador.crear(configuracion);
    }

    public String obtenerTitulo(int capitulo) {
        return datos.obtenerNivel(capitulo)
                .get("titulo")
                .getAsString();
    }
}
