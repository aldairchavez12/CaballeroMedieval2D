package juego;

import entidades.Caballero;
import entidades.Enemigo;
import escenario.Estructura;
import interfaces.Atacable;
import items.Inventario;

import java.util.*;

public class GestorCombate {

    private static final long COOLDOWN_CABALLERO =
        350_000_000L;

    private static final int ENEMIGO_Y = 550;

    private long ultimoAtaqueCaballero;

    private final Caballero caballero;
    private final GestorProyectiles proyectiles;
    private final List<Enemigo> enemigos;
    private final Map<Enemigo, Integer> posiciones;

    public GestorCombate(
            Caballero caballero,
            Inventario inventario,
            GestorProyectiles proyectiles,
            List<Enemigo> enemigos,
            Map<Enemigo, Integer> posiciones) {

        this.caballero = caballero;
        this.proyectiles = proyectiles;
        this.enemigos = enemigos;
        this.posiciones = posiciones;
    }

    public Enemigo atacar() {
        return atacar(Collections.emptyList());
    }

    public Enemigo atacar(List<Estructura> estructuras) {

        if (!caballero.estaVivo()) return null;

        long ahora = System.nanoTime();

        if (ahora - ultimoAtaqueCaballero
                < COOLDOWN_CABALLERO) {
            return null;
        }

        caballero.verificarArma();

        Atacable objetivo = null;

        if (!caballero.usaAtaqueDistancia()) {

            objetivo = buscarObjetivo(estructuras);

            if (objetivo == null) return null;
        }

        boolean ejecutado = caballero.ejecutarAtaque(
            objetivo,
            proyectiles
        );

        if (!ejecutado) return null;

        ultimoAtaqueCaballero = ahora;

        if (objetivo instanceof Enemigo) {
            return (Enemigo) objetivo;
        }

        return null;
    }

    private Atacable buscarObjetivo(
            List<Estructura> estructuras) {

        Atacable objetivo = null;
        int distanciaMinima = Integer.MAX_VALUE;

        for (Enemigo enemigo : enemigos) {

            if (!enemigo.estaVivo()) continue;

            Integer x = posiciones.get(enemigo);

            if (x == null) continue;

            int distancia = evaluarDistancia(
                x, ENEMIGO_Y
            );

            if (distancia < distanciaMinima) {

                objetivo = enemigo;
                distanciaMinima = distancia;
            }
        }

        for (Estructura estructura : estructuras) {

            if (estructura.estaDestruida()) continue;

            int distancia = evaluarDistancia(
                estructura.getX(),
                estructura.getY()
            );

            if (distancia < distanciaMinima) {

                objetivo = estructura;
                distanciaMinima = distancia;
            }
        }

        return objetivo;
    }

    private int evaluarDistancia(int x, int y) {

        int centroJugador =
            caballero.getX() + Caballero.ANCHO / 2;

        int diferencia = x + 22 - centroJugador;

        boolean delante = caballero.miraDerecha()
            ? diferencia >= 0
            : diferencia <= 0;

        int distancia = Math.abs(diferencia);

        if (!delante
                || Math.abs(caballero.getY() - y) >= 60
                || distancia > caballero.getAlcanceAtaque()) {

            return Integer.MAX_VALUE;
        }

        return distancia;
    }

    public void reiniciar() {
        ultimoAtaqueCaballero = 0;
    }
}