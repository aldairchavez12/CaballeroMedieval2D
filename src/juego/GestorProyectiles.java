package juego;

import entidades.Enemigo;
import escenario.Estructura;
import interfaces.Atacable;
import interfaces.LanzadorProyectiles;
import items.herramientas.Flecha;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.util.*;

public class GestorProyectiles
        implements LanzadorProyectiles {

    private static class Proyectil {

        double x;
        double y;
        double recorrido;

        final int direccion;
        final int alcance;
        final int bonoDanio;
        final Flecha flecha;

        Proyectil(
                double x, double y,
                int direccion, int alcance,
                Flecha flecha, int bonoDanio) {

            this.x = x;
            this.y = y;
            this.direccion = direccion;
            this.alcance = alcance;
            this.flecha = flecha;
            this.bonoDanio = bonoDanio;
        }
    }

    private final List<Proyectil> proyectiles =
        new ArrayList<>();

    public void disparar(
            double x, double y,
            int direccion, int alcance,
            Flecha flecha) {

        disparar(x, y, direccion, alcance, flecha, 0);
    }

    @Override
    public void disparar(
            double x, double y,
            int direccion, int alcance,
            Flecha flecha, int bonoDanio) {

        proyectiles.add(new Proyectil(
            x, y, direccion,
            alcance, flecha, bonoDanio
        ));
    }

    public void actualizar(
            List<Enemigo> enemigos,
            int enemigoY) {

        actualizar(
            enemigos,
            enemigoY,
            Collections.emptyList()
        );
    }

    public void actualizar(
            List<Enemigo> enemigos,
            int enemigoY,
            List<Estructura> estructuras) {

        Iterator<Proyectil> it = proyectiles.iterator();

        while (it.hasNext()) {

            Proyectil p = it.next();

            double avance = Math.min(
                p.flecha.getVelocidad(),
                p.alcance - p.recorrido
            );

            if (avance <= 0) {
                it.remove();
                continue;
            }

            double anteriorX = p.x;

            p.x += avance * p.direccion;
            p.recorrido += avance;

            Atacable objetivo = null;
            double distanciaMinima =
                Double.POSITIVE_INFINITY;

            for (Enemigo enemigo : enemigos) {

                if (!enemigo.estaVivo()) continue;

                int x = enemigo.getX();

                if (p.y >= enemigoY
                        && p.y <= enemigoY + 70) {

                    double distancia = distanciaImpacto(
                        anteriorX, p.x, x, x + 45
                    );

                    if (distancia < distanciaMinima) {

                        objetivo = enemigo;
                        distanciaMinima = distancia;
                    }
                }
            }

            for (Estructura estructura : estructuras) {

                if (estructura.estaDestruida()) continue;

                if (p.y >= estructura.getY()
                        && p.y <= 620) {

                    double distancia = distanciaImpacto(
                        anteriorX, p.x,
                        estructura.getX(),
                        estructura.getX() + 45
                    );

                    if (distancia < distanciaMinima) {

                        objetivo = estructura;
                        distanciaMinima = distancia;
                    }
                }
            }

            if (objetivo != null) {

                p.flecha.impactar(
                    objetivo, p.bonoDanio
                );

                it.remove();

            } else if (p.recorrido >= p.alcance) {

                it.remove();
            }
        }
    }

    private double distanciaImpacto(
            double anteriorX,
            double actualX,
            double izquierda,
            double derecha) {

        double minimo = Math.min(anteriorX, actualX);
        double maximo = Math.max(anteriorX, actualX);

        if (maximo < izquierda || minimo > derecha) {
            return Double.POSITIVE_INFINITY;
        }

        if (anteriorX >= izquierda
                && anteriorX <= derecha) {
            return 0;
        }

        return actualX >= anteriorX
            ? Math.abs(izquierda - anteriorX)
            : Math.abs(derecha - anteriorX);
    }

    public void dibujar(Graphics2D original) {

        Graphics2D g = (Graphics2D) original.create();

        try {

            g.setColor(new Color(225, 190, 110));
            g.setStroke(new BasicStroke(3));

            for (Proyectil p : proyectiles) {

                int x = (int) p.x;
                int y = (int) p.y;

                int atras = x - 15 * p.direccion;

                g.drawLine(atras, y, x, y);

                g.drawLine(
                    x, y,
                    x - 6 * p.direccion, y - 4
                );

                g.drawLine(
                    x, y,
                    x - 6 * p.direccion, y + 4
                );
            }

        } finally {
            g.dispose();
        }
    }

    public void limpiar() {
        proyectiles.clear();
    }
}