
package juego;

import entidades.Enemigo;
import items.herramientas.Flecha;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GestorProyectiles {

    private static class Proyectil {

        double x;
        double y;
        double recorrido;

        int direccion;
        int alcance;

        Flecha flecha;

        Proyectil(double x, double y, int direccion,
                  int alcance, Flecha flecha) {

            this.x = x;
            this.y = y;
            this.direccion = direccion;
            this.alcance = alcance;
            this.flecha = flecha;
        }
    }

    private final List<Proyectil> proyectiles =
            new ArrayList<>();

    public void disparar(
            double x,
            double y,
            int direccion,
            int alcance,
            Flecha flecha) {

        proyectiles.add(
            new Proyectil(x, y, direccion, alcance, flecha)
        );
    }

    public void actualizar(
            List<Enemigo> enemigos,
            Map<Enemigo, Integer> posiciones,
            int enemigoY) {

        Iterator<Proyectil> it = proyectiles.iterator();

        while (it.hasNext()) {

            Proyectil p = it.next();

            double avance = p.flecha.getVelocidad();

            p.x += avance * p.direccion;
            p.recorrido += avance;

            boolean impacto = false;

            for (Enemigo enemigo : enemigos) {

                if (!enemigo.estaVivo()) continue;

                Integer x = posiciones.get(enemigo);
                if (x == null) continue;

                boolean colisionX =
                    p.x >= x && p.x <= x + 45;

                boolean colisionY =
                    p.y >= enemigoY
                    && p.y <= enemigoY + 70;

                if (colisionX && colisionY) {

                    p.flecha.impactar(enemigo);
                    impacto = true;
                    break;
                }
            }

            if (impacto || p.recorrido >= p.alcance) {
                it.remove();
            }
        }
    }

    public void dibujar(Graphics2D g) {

        g.setColor(new Color(225, 190, 110));
        g.setStroke(new BasicStroke(3));

        for (Proyectil p : proyectiles) {

            int x = (int) p.x;
            int y = (int) p.y;

            int atras = x - 15 * p.direccion;

            g.drawLine(atras, y, x, y);

            // Punta de la flecha
            g.drawLine(x, y, x - 6 * p.direccion, y - 4);
            g.drawLine(x, y, x - 6 * p.direccion, y + 4);
        }

        g.setStroke(new BasicStroke(1));
    }

    public void limpiar() {
        proyectiles.clear();
    }
}
