
package juego;

import escenario.Moneda;
import entidades.Caballero;

import escenario.Cofre;
import java.util.Random;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import entidades.Entidad;
import entidades.Enemigo;

import java.util.List;


public class GestorNiveles {

    private final List<Moneda> monedas =
        new ArrayList<>();

    public void generarMonedas() {

        monedas.clear();

        monedas.add(new Moneda(320, 565, 1));
        monedas.add(new Moneda(950, 565, 1));
        monedas.add(new Moneda(1700, 565, 1));
        monedas.add(new Moneda(2540, 565, 1));
        monedas.add(new Moneda(3450, 565, 1));
        monedas.add(new Moneda(4450, 565, 1));
    }

    public List<Moneda> getMonedas() {

        return Collections.unmodifiableList(monedas);
    }

    public int recogerMonedas(Caballero caballero) {

        if (!caballero.estaVivo()) return 0;

        int total = 0;

        for (Moneda moneda : monedas) {

            total += moneda.recoger(
                caballero.getX(),
                caballero.getY(),
                Caballero.ANCHO,
                Caballero.ALTO
            );
        }

        return total;
    }

private final List<Cofre> cofres = new ArrayList<>();

    private final Random azarCofres = new Random();

    public void generarCofres(CreadorItems creador) {

        cofres.clear();

        List<String> botin = new ArrayList<>(
            Arrays.asList(
                "espada_basica",
                "arco_basico",
                "flecha_basica",
                "escudo_basico",
                "armadura_basica",
                "pocima_vida",
                "comida_manzana",
                "habilidad_furia"
            )
        );

        Collections.shuffle(botin, azarCofres);

        // Garantizar un arma antes del primer combate.
        Collections.swap(
            botin,
            0,
            botin.indexOf("espada_basica")
        );

        for (int i = 0; i < botin.size(); i++) {

            int x = 200 + i * 550
                + azarCofres.nextInt(100);

            cofres.add(
                new Cofre(
                    x,
                    580,
                    creador.crear(botin.get(i))
                )
            );
        }
    }

    public List<Cofre> getCofres() {
        return Collections.unmodifiableList(cofres);
    }

    public Cofre buscarCofreCercano(
            int jugadorX,
            int jugadorY) {

        Cofre cercano = null;
        int distanciaMinima = Integer.MAX_VALUE;

        for (Cofre cofre : cofres) {

            if (cofre.estaAbierto()
                    || !cofre.estaCerca(jugadorX, jugadorY)) {

                continue;
            }

            int distancia =
                Math.abs(cofre.getX() - jugadorX);

            if (distancia < distanciaMinima) {

                cercano = cofre;
                distanciaMinima = distancia;
            }
        }

        return cercano;
    }

private final DatosJuego datos;

    public GestorNiveles(DatosJuego datos) {
        this.datos = datos;
    }

    // Cargar los enemigos del capitulo desde niveles.json
    public Enemigo cargarCapitulo(
            int numeroCapitulo,
            List<Entidad> entidades,
            List<Enemigo> enemigos) {

        // Eliminar enemigos del capitulo anterior
        entidades.removeIf(e -> e instanceof Enemigo);

        enemigos.clear();
        

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
            nuevo.establecerPosicion(posicionX);
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
