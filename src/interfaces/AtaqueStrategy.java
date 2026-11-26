package interfaces;

import entidades.Caballero;
import items.Item;
import items.herramientas.Espada;
import items.herramientas.Arco;
import items.herramientas.Flecha;

public interface AtaqueStrategy {

    void ejecutar(
        Caballero caballero,
        Atacable objetivo
    );

    int getAlcance();

    default boolean esAtaqueDistancia() {
        return false;
    }

    default boolean ejecutarAtaque(
            Caballero caballero,
            Atacable objetivo,
            LanzadorProyectiles lanzador) {

        if (objetivo == null) {
            return false;
        }

        ejecutar(caballero, objetivo);
        return true;
    }

    class Basico implements AtaqueStrategy {

        private static final int DANIO_PUNOS = 5;
        private static final int ALCANCE_PUNOS = 45;

        @Override
        public void ejecutar(
                Caballero caballero,
                Atacable objetivo) {

            if (objetivo == null) return;

            objetivo.recibirDanio(
                DANIO_PUNOS
                + caballero.getBonoAtaque()
            );
        }

        @Override
        public int getAlcance() {
            return ALCANCE_PUNOS;
        }
    }

    class ConEspada implements AtaqueStrategy {

        private final Espada espada;

        public ConEspada(Espada espada) {
            this.espada = espada;
        }

        @Override
        public void ejecutar(
                Caballero c,
                Atacable objetivo) {

            if (objetivo == null) return;

            if (espada.getDurabilidad() <= 0) {

                new Basico().ejecutar(c, objetivo);
                return;
            }

            objetivo.recibirDanio(
                espada.getDanio()
                + c.getBonoAtaque()
            );

            espada.usar(c);
        }

        @Override
        public int getAlcance() {
            return 90;
        }
    }

    class ConArco implements AtaqueStrategy {

        private final Arco arco;

        public ConArco(Arco arco) {
            this.arco = arco;
        }

        @Override
        public boolean esAtaqueDistancia() {
            return true;
        }

        @Override
        public void ejecutar(
                Caballero c,
                Atacable objetivo) {

            // Un arco necesita un lanzador de proyectiles.
            // No aplica danio directo al objetivo.
        }

        @Override
        public boolean ejecutarAtaque(
                Caballero c,
                Atacable objetivo,
                LanzadorProyectiles lanzador) {

            if (lanzador == null
                    || arco.getDurabilidad() <= 0) {
                return false;
            }

            Flecha municion = buscarMunicion(c);

            if (municion == null
                    || !municion.consumirUna()) {

                System.out.println("No quedan flechas");
                return false;
            }

            int direccion = c.miraDerecha() ? 1 : -1;

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