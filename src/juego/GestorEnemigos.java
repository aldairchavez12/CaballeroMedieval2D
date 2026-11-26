
package juego;

import entidades.Boss;
import entidades.Caballero;
import entidades.Enemigo;

import java.util.List;
import java.util.Map;
import java.util.IdentityHashMap;

public class GestorEnemigos {

    private static final int ANCHO_ENEMIGO = 45;
    private static final int ENEMIGO_Y = 550;

    private static final int VELOCIDAD_ENEMIGO = 2;    
    private static final int DISTANCIA_DETECCION = 340;
    private static final int DISTANCIA_ATAQUE = 65;
    private static final int DISTANCIA_MINIMA = 90;
    private static final int DISTANCIA_ATAQUE_REAL = 110;
    private static final long COOLDOWN_ATAQUE =
            1_200_000_000L;

    private final Caballero caballero;
    private final List<Enemigo> enemigos;
    private final Map<Enemigo, Integer> posiciones;
    private static final int DISTANCIA_PERDIDA = 450;
    private static final int DISTANCIA_MAX_PERSECUCION = 400;

    private final Map<Enemigo, Integer> puntosDeteccion =
            new IdentityHashMap<>();

    private final Map<Enemigo, Boolean> esperandoSalida =
            new IdentityHashMap<>();
    private final Map<Enemigo, Boolean> persiguiendo =
            new IdentityHashMap<>();
    private final Map<Enemigo, Long> ultimosAtaques =
            new IdentityHashMap<>();

    private int golpesJefe = 0;

    public GestorEnemigos(
            Caballero caballero,
            List<Enemigo> enemigos,
            Map<Enemigo, Integer> posiciones) {

        this.caballero = caballero;
        this.enemigos = enemigos;
        this.posiciones = posiciones;
    }

    public void actualizar() {

        if (!caballero.estaVivo()) {
            return;
        }

        for (Enemigo enemigo : enemigos) {

            if (!enemigo.estaVivo()) {
                continue;
            }

            actualizarEnemigo(enemigo);
        }
    }

    
   
    
    private void actualizarEnemigo(Enemigo enemigo) {

        Integer posicion = posiciones.get(enemigo);
        if (posicion == null) return;

        int x = posicion;

        int centroCaballero =
                caballero.getX() + Caballero.ANCHO / 2;

        int centroEnemigo =
                x + ANCHO_ENEMIGO / 2;

        int diferencia = centroCaballero - centroEnemigo;
        int distancia = Math.abs(diferencia);

        boolean detectado =
                persiguiendo.getOrDefault(enemigo, false);

        boolean bloqueado =
                esperandoSalida.getOrDefault(enemigo, false);

        // Si abandono la persecucion, esperar a que el
        // jugador salga del rango antes de detectarlo otra vez.
        if (bloqueado) {
            if (distancia > DISTANCIA_DETECCION) {
                esperandoSalida.put(enemigo, false);
            }
            return;
        }

        if (!detectado) {

            if (distancia > DISTANCIA_DETECCION) {
                return;
            }

            persiguiendo.put(enemigo, true);
            puntosDeteccion.put(enemigo, centroEnemigo);
        }

        int puntoInicial = puntosDeteccion.get(enemigo);

        int recorrido = Math.abs(
            centroEnemigo - puntoInicial
        );

        // Perder el objetivo sin regresar al origen
        if (recorrido >= DISTANCIA_MAX_PERSECUCION
                || distancia > DISTANCIA_PERDIDA) {

            persiguiendo.put(enemigo, false);
            esperandoSalida.put(enemigo, true);
            return;
        }

        int movimiento = 0;

        // Acercarse, pero sin invadir la distancia minima.
        if (distancia > DISTANCIA_MINIMA) {

            movimiento = Math.min(
                VELOCIDAD_ENEMIGO,
                distancia - DISTANCIA_MINIMA
            ) * Integer.compare(diferencia, 0);

        } else if (distancia < DISTANCIA_MINIMA) {

            // Retroceder cuando el caballero se acerca demasiado.
            movimiento = Math.min(
                VELOCIDAD_ENEMIGO,
                DISTANCIA_MINIMA - distancia
            ) * -Integer.compare(diferencia, 0);
        }

        enemigo.prepararMovimiento(
            x,
            movimiento,
            Juego.ANCHO_NIVEL - ANCHO_ENEMIGO
        );

        enemigo.update();

        x = enemigo.getX();

        posiciones.put(enemigo, x);

        boolean alturaCompatible =
                Math.abs(caballero.getY() - ENEMIGO_Y) < 55;

        // Atacar manteniendo cierta separacion.
        if (distancia <= DISTANCIA_ATAQUE_REAL
                && alturaCompatible) {

            ejecutarAtaque(enemigo);
        }
    }


    private void ejecutarAtaque(Enemigo enemigo) {

        long ahora = System.nanoTime();

        long anterior = ultimosAtaques.getOrDefault(
            enemigo, 0L
        );

        if (ahora - anterior < COOLDOWN_ATAQUE) {
            return;
        }

        if (enemigo instanceof Boss) {

            golpesJefe++;

            if (golpesJefe % 3 == 0) {
                ((Boss) enemigo).ataqueEspecial(caballero);
            } else {
                enemigo.atacar(caballero);
            }

        } else {

            enemigo.atacar(caballero);
        }

        ultimosAtaques.put(enemigo, ahora);

        System.out.println(
            "Vida del Caballero: " + caballero.getVida()
        );
    }

    public void reiniciar() {

        ultimosAtaques.clear();
        persiguiendo.clear();
        puntosDeteccion.clear();
        esperandoSalida.clear();

        golpesJefe = 0;
    }
}
