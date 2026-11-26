
package juego;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;

public class DatosJuego {

    private final JsonObject enemigos;
    private final JsonObject niveles;
    private final JsonObject historia;
    private final JsonObject items;

    public DatosJuego() {
        enemigos = leerJson("enemigos.json");
        niveles = leerJson("niveles.json");
        historia = leerJson("historia.json");
        items = leerJson("items.json");
    }

    private JsonObject leerJson(String archivo) {

        Path ruta = Paths.get("data", archivo);

        try (Reader lector = Files.newBufferedReader(ruta)) {

            JsonObject resultado = JsonParser
                    .parseReader(lector)
                    .getAsJsonObject();

            System.out.println("JSON cargado: " + archivo);

            return resultado;

        } catch (IOException | RuntimeException e) {

            throw new IllegalStateException(
                "Error leyendo " + ruta.toAbsolutePath(), e
            );
        }
    }

    private JsonObject buscarPorEntero(
            JsonArray lista, String campo, int valor) {

        if (lista == null) {
            throw new IllegalStateException(
                "Arreglo JSON no encontrado: " + campo
            );
        }

        for (JsonElement elemento : lista) {

            JsonObject objeto = elemento.getAsJsonObject();

            if (objeto.get(campo).getAsInt() == valor) {
                return objeto;
            }
        }

        throw new IllegalArgumentException(
            "No existe " + campo + " = " + valor
        );
    }

    public JsonObject obtenerEnemigo(int tipo) {
        return buscarPorEntero(
            enemigos.getAsJsonArray("enemigos"),
            "tipo",
            tipo
        );
    }

    public JsonObject obtenerNivel(int capitulo) {
        return buscarPorEntero(
            niveles.getAsJsonArray("capitulos"),
            "id",
            capitulo
        );
    }

    public JsonObject obtenerHistoria(int capitulo) {
        return buscarPorEntero(
            historia.getAsJsonArray("capitulos"),
            "id",
            capitulo
        );
    }

    public String obtenerMensajeDerrota() {
        return historia.get("derrota").getAsString();
    }

    public JsonObject obtenerItem(String id) {

        String[] categorias = {
            "herramientas",
            "consumibles"
        };

        for (String categoria : categorias) {

            JsonArray lista = items.getAsJsonArray(categoria);

            if (lista == null) {
                continue;
            }

            for (JsonElement elemento : lista) {

                JsonObject objeto = elemento.getAsJsonObject();

                if (objeto.get("id").getAsString().equals(id)) {
                    return objeto;
                }
            }
        }

        throw new IllegalArgumentException(
            "Item no encontrado: " + id
        );
    }
}
