package juego;

import entidades.Caballero;
import entidades.Enemigo;
import escenario.Estructura;
import interfaces.Atacable;


import java.util.*;

public class GestorCombate {

    private static final long COOLDOWN_CABALLERO =
        350_000_000L;

    private static final int ENEMIGO_Y = 550;

    private long ultimoAtaqueCaballero;

    private final Caballero caballero;
    
    private final List<Enemigo> enemigos;
    

    public GestorCombate(
            Caballero caballero,
            GestorProyectiles proyectiles,
            List<Enemigo> enemigos) {

        this.caballero = caballero;
        caballero.configurarLanzador(proyectiles);
        this.enemigos = enemigos;
        
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

        boolean ejecutado = caballero.ejecutarAtaque(objetivo);

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

            int x = enemigo.getX();

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