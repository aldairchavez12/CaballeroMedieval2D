package interfaces;

import entidades.Caballero;
import items.Item;
import items.herramientas.Espada;
import items.herramientas.Arco;
import items.herramientas.Flecha;

public interface AtaqueStrategy {

    boolean ejecutar(
        Caballero caballero,
        Atacable objetivo,
        LanzadorProyectiles lanzador
    );

    int getAlcance();

    default boolean esAtaqueDistancia() {
        return false;
    }

    // ========================================
    // ATAQUE BASICO
    // ========================================

    final class Basico implements AtaqueStrategy {

        private static final int DANIO = 5;
        private static final int ALCANCE = 45;

        @Override
        public boolean ejecutar(
                Caballero c,
                Atacable objetivo,
                LanzadorProyectiles lanzador) {

            if (!c.estaVivo() || objetivo == null) {
                return false;
            }

            objetivo.recibirDanio(
                DANIO + c.getBonoAtaque()
            );

            return true;
        }

        @Override
        public int getAlcance() {
            return ALCANCE;
        }
    }

    // ========================================
    // ATAQUE CON ESPADA
    // ========================================

    final class ConEspada implements AtaqueStrategy {

        private final Espada espada;

        public ConEspada(Espada espada) {
            this.espada = espada;
        }

        @Override
        public boolean ejecutar(
                Caballero c,
                Atacable objetivo,
                LanzadorProyectiles lanzador) {

            if (!c.estaVivo()
                    || objetivo == null
                    || espada.getDurabilidad() <= 0) {

                return false;
            }

            objetivo.recibirDanio(
                espada.getDanio()
                + c.getBonoAtaque()
            );

            espada.usar(c);

            return true;
        }

        @Override
        public int getAlcance() {
            return 90;
        }
    }

    // ========================================
    // ATAQUE CON ARCO
    // ========================================

    final class ConArco implements AtaqueStrategy {

        private final Arco arco;

        public ConArco(Arco arco) {
            this.arco = arco;
        }

        @Override
        public boolean esAtaqueDistancia() {
            return true;
        }

        @Override
        public boolean ejecutar(
                Caballero c,
                Atacable objetivo,
                LanzadorProyectiles lanzador) {

            if (!c.estaVivo()
                    || lanzador == null
                    || arco.getDurabilidad() <= 0) {

                return false;
            }

            Flecha municion = buscarMunicion(c);

            if (municion == null
                    || !municion.consumirUna()) {

                System.out.println("No quedan flechas");
                return false;
            }

            int direccion =
                c.miraDerecha() ? 1 : -1;

            double origenX = c.getX()
                + (direccion > 0
                    ? Caballero.ANCHO + 10
                    : -10);

            double origenY = c.getY() + 35;

            int bono = arco.getDanioBase()
                + c.getBonoAtaque();

            lanzador.disparar(
                origenX,
                origenY,
                direccion,
                getAlcance(),
                municion,
                bono
            );

            arco.disparar();

            if (municion.getCantidad() == 0) {
                c.eliminarObjeto(municion);
            }

            return true;
        }

        private Flecha buscarMunicion(Caballero c) {

            for (Item item :
                    c.getInventario().obtenerItems()) {

                if (item instanceof Flecha) {

                    Flecha flecha = (Flecha) item;

                    if (flecha.getCantidad() > 0) {
                        return flecha;
                    }
                }
            }

            return null;
        }

        @Override
        public int getAlcance() {
            return arco.getAlcance();
        }
    }
}