
package juego;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class APIClient {

    public enum Clima {
        SOLEADO,
        NUBLADO,
        LLUVIOSO
    }

    private final HttpClient cliente;

    public APIClient() {
        cliente = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public CompletableFuture<Clima> obtenerClima() {

        // Coordenadas de Lima, Perú
        String url =
            "https://api.open-meteo.com/v1/forecast"
            + "?latitude=-12.0464"
            + "&longitude=-77.0428"
            + "&current=weather_code"
            + "&timezone=America%2FLima";

        HttpRequest solicitud = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();

        return cliente.sendAsync(
                solicitud,
                HttpResponse.BodyHandlers.ofString()
        ).thenApply(respuesta -> {

            if (respuesta.statusCode() != 200) {
                throw new IllegalStateException(
                    "Error HTTP: " + respuesta.statusCode()
                );
            }

            JsonObject json = JsonParser
                    .parseString(respuesta.body())
                    .getAsJsonObject();

            int codigo = json
                    .getAsJsonObject("current")
                    .get("weather_code")
                    .getAsInt();

            // Clasificación simplificada de códigos WMO
            if (codigo >= 51 && codigo <= 82) {
                return Clima.LLUVIOSO;
            }

            if (codigo >= 1 && codigo <= 3
                    || codigo >= 45 && codigo <= 48
                    || codigo >= 85) {
                return Clima.NUBLADO;
            }

            return Clima.SOLEADO;

        }).exceptionally(error -> {

            System.err.println(
                "No se pudo consultar la API: "
                + error.getMessage()
            );

            // El videojuego sigue funcionando sin internet.
            return Clima.NUBLADO;
        });
    }
}
