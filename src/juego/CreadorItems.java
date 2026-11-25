
package juego;

import com.google.gson.JsonObject;
import items.Item;
import items.herramientas.*;
import items.consumibles.*;

public class CreadorItems {

    private final DatosJuego datos;

    public CreadorItems(DatosJuego datos) {
        this.datos = datos;
    }

    public Item crear(String id) {

        JsonObject d = datos.obtenerItem(id);

        String tipo = d.get("tipo").getAsString();
        String nombre = d.get("nombre").getAsString();
        String descripcion = d.get("descripcion").getAsString();

        switch (tipo) {

            case "Espada":
                return new Espada(
                    nombre, descripcion,
                    d.get("durabilidad").getAsInt(),
                    d.get("danio").getAsInt(),
                    d.get("alcance").getAsDouble()
                );

            case "Arco":
                return new Arco(
                    nombre, descripcion,
                    d.get("durabilidad").getAsInt(),
                    d.get("danio").getAsInt(),
                    d.get("alcance").getAsDouble()
                );

            case "Escudo":
                return new Escudo(
                    nombre, descripcion,
                    d.get("durabilidad").getAsInt(),
                    d.get("defensa").getAsInt(),
                    d.get("durabilidad").getAsInt()
                );

            case "Armadura":
                return new Armadura(
                    nombre, descripcion,
                    d.get("durabilidad").getAsInt(),
                    d.get("defensa").getAsInt(),
                    5.0
                );

            case "Flecha":
                return new Flecha(
                    nombre,
                    descripcion,
                    d.get("danio").getAsInt(),
                    12.0,
                    d.get("cantidad").getAsInt()
                );

            case "Pocima":
                return new Pocima(
                    nombre, descripcion,
                    d.get("cantidad").getAsInt(),
                    d.get("curacion").getAsInt(),
                    d.get("efecto").getAsString()
                );

            case "Comida":
                return new Comida(
                    nombre, descripcion,
                    d.get("cantidad").getAsInt(),
                    d.get("curacion").getAsInt()
                );

            case "Habilidad":
                return new Habilidad(
                    nombre, descripcion,
                    d.get("cantidad").getAsInt(),
                    d.get("valor").getAsInt(),
                    "10 segundos",
                    d.get("efecto").getAsString()
                );

            default:
                throw new IllegalArgumentException(
                    "Tipo de item desconocido: " + tipo
                );
        }
    }

    public Herramienta crearArma(String id) {
        Item item = crear(id);

        if (!(item instanceof Espada) && !(item instanceof Arco)) {
            throw new IllegalArgumentException(
                "El item no es un arma: " + id
            );
        }

        return (Herramienta) item;
    }
}
