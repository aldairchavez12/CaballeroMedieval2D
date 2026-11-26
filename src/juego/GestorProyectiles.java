package juego;

import entidades.Enemigo;
import escenario.Estructura;
import interfaces.Atacable;
import interfaces.LanzadorProyectiles;
import items.herramientas.Flecha;

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

    

    public static final class VistaProyectil {

        private final double x;
        private final double y;
        private final int direccion;

        private VistaProyectil(
                double x,
                double y,
                int direccion) {

            this.x = x;
            this.y = y;
            this.direccion = direccion;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public int getDireccion() {
            return direccion;
        }
    }

    public List<VistaProyectil> obtenerVistaProyectiles() {

        List<VistaProyectil> vista = new ArrayList<>();

        for (Proyectil p : proyectiles) {

            vista.add(
                new VistaProyectil(
                    p.x,
                    p.y,
                    p.direccion
                )
            );
        }

        return Collections.unmodifiableList(vista);
    }

    public void limpiar() {
        proyectiles.clear();
    }
}