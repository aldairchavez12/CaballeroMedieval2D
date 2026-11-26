
package juego;

import entidades.Aliado;
import entidades.Caballero;
import entidades.Enemigo;

import java.util.List;


public class GestorAliados {

    private final Aliado aliado;

    private int tiempoAtaque = 0;

    private static final int DISTANCIA_ATAQUE = 75;
    private static final int ESPERA_ATAQUE = 65;

    public GestorAliados() {

        aliado = new Aliado(
            "Guardian de Valdoria",
            60,
            5,
            "Combate",
            5
        );
    }

    public void actualizar(
            Caballero caballero,
            List<Enemigo> enemigos) {

        if (!aliado.estaVivo()) {
            return;
        }

        aliado.seguir(caballero);

        if (tiempoAtaque > 0) {
            tiempoAtaque--;
            return;
        }

        for (Enemigo enemigo : enemigos) {

            if (!enemigo.estaVivo()) {
                continue;
            }

            int xEnemigo = enemigo.getX();

            int distancia = Math.abs(
                aliado.getX() - xEnemigo
            );

            if (distancia <= DISTANCIA_ATAQUE) {

                aliado.atacar(enemigo);

                tiempoAtaque = ESPERA_ATAQUE;
                break;
            }
        }
    }

    public void reiniciar() {
        aliado.reiniciar();
        tiempoAtaque = 0;
    }

    public Aliado getAliado() {
        return aliado;
    }
}
