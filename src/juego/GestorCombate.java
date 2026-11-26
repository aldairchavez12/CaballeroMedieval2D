
package juego;

import entidades.*;
import escenario.Estructura;
import items.*;
import items.herramientas.*;
import java.util.*;

public class GestorCombate {

    private static final long COOLDOWN_CABALLERO =
            350_000_000L;

    private static final int ANCHO_ENEMIGO = 45;
    private static final int ENEMIGO_Y = 550;

    private long ultimoAtaqueCaballero;

    private final Caballero caballero;
    private final Inventario inventario;
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
        this.inventario = inventario;
        this.proyectiles = proyectiles;
        this.enemigos = enemigos;
        this.posiciones = posiciones;
    }

    public Enemigo atacar() {
        return atacar(Collections.emptyList());
    }

    public Enemigo atacar(List<Estructura> estructuras) {

        if (!caballero.estaVivo()) {
            return null;
        }

        long ahora = System.nanoTime();

        if (ahora - ultimoAtaqueCaballero
                < COOLDOWN_CABALLERO) {
            return null;
        }

        Herramienta arma = caballero.getArmaEquipada();

        // Ataque a distancia
        if (arma instanceof Arco) {

            if (dispararArco((Arco) arma)) {
                ultimoAtaqueCaballero = ahora;
            }

            return null;
        }

        // Arma desgastada: regresar a ataque basico
        if (arma != null && arma.getDurabilidad() <= 0) {
            caballero.equiparArma(null);
        }

        Enemigo objetivo = buscarObjetivo();

        if (objetivo == null) {

            for (Estructura estructura : estructuras) {

                if (estructura.estaDestruida()) {
                    continue;
                }

                int diferencia =
                    estructura.getX() + 22
                    - caballero.getX()
                    - Caballero.ANCHO / 2;

                boolean delante = caballero.miraDerecha()
                    ? diferencia >= 0
                    : diferencia <= 0;

                boolean alturaCompatible =
                    Math.abs(caballero.getY()
                    - estructura.getY()) < 60;

                if (delante && alturaCompatible
                        && Math.abs(diferencia)
                        <= caballero.getAlcanceAtaque()) {

                    caballero.atacar(estructura);

                    ultimoAtaqueCaballero = ahora;

                    return null;
                }
            }
        }

        if (objetivo == null) {
            return null;
        }

        // Se conserva el patron Strategy
        caballero.atacar(objetivo);

        ultimoAtaqueCaballero = ahora;

        System.out.println(
            objetivo.getNombre()
            + " - Vida: "
            + objetivo.getVida()
        );

        return objetivo;
    }

    private Enemigo buscarObjetivo() {

        int centroCaballero =
                caballero.getX() + Caballero.ANCHO / 2;

        Enemigo objetivo = null;
        int distanciaMinima = Integer.MAX_VALUE;

        for (Enemigo enemigo : enemigos) {

            if (!enemigo.estaVivo()) {
                continue;
            }

            Integer posicion = posiciones.get(enemigo);

            if (posicion == null) {
                continue;
            }

            int centroEnemigo =
                    posicion + ANCHO_ENEMIGO / 2;

            int diferencia = centroEnemigo - centroCaballero;
            int distancia = Math.abs(diferencia);

            boolean delante = caballero.miraDerecha()
                    ? diferencia >= 0
                    : diferencia <= 0;

            if (!delante) {
                continue;
            }

            boolean alturaCompatible =
                    Math.abs(caballero.getY() - ENEMIGO_Y) < 60;

            if (alturaCompatible
                    && distancia <= caballero.getAlcanceAtaque()
                    && distancia < distanciaMinima) {

                objetivo = enemigo;
                distanciaMinima = distancia;
            }
        }

        return objetivo;
    }

    private boolean dispararArco(Arco arco) {

        if (arco.getDurabilidad() <= 0) {
            return false;
        }

        Flecha municion = buscarMunicion();

        if (municion == null || !municion.consumirUna()) {
            System.out.println("No quedan flechas");
            return false;
        }

        int direccion = caballero.miraDerecha() ? 1 : -1;

        double origenX = caballero.getX()
                + (direccion > 0
                    ? Caballero.ANCHO + 10
                    : -10);

        double origenY = caballero.getY() + 35;

        proyectiles.disparar(
            origenX,
            origenY,
            direccion,
            caballero.getAlcanceAtaque(),
            municion
        );

        arco.disparar(null);

        if (municion.getCantidad() == 0) {
            inventario.eliminarItem(municion);
        }

        return true;
    }

    private Flecha buscarMunicion() {

        for (Item item : inventario.obtenerItems()) {

            if (item instanceof Flecha) {

                Flecha flecha = (Flecha) item;

                if (flecha.getCantidad() > 0) {
                    return flecha;
                }
            }
        }

        return null;
    }

    public void reiniciar() {
        ultimoAtaqueCaballero = 0;
    }
}
