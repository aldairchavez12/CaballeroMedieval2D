
package interfaces;

import entidades.Caballero;
import items.herramientas.Espada;
import items.herramientas.Arco;

public interface AtaqueStrategy {

    void ejecutar(Caballero caballero, Atacable objetivo);

    int getAlcance();

    
    class Basico implements AtaqueStrategy {

        private static final int DANIO_PUNOS = 5;
        private static final int ALCANCE_PUNOS = 45;

        @Override
        public void ejecutar(Caballero caballero, Atacable objetivo) {

            if (objetivo == null) {
                return;
            }

            objetivo.recibirDanio(DANIO_PUNOS);
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
        public void ejecutar(Caballero c, Atacable objetivo) {

            if (espada.getDurabilidad() <= 0) {
                new Basico().ejecutar(c, objetivo);
                return;
            }

            objetivo.recibirDanio(espada.getDanio());
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
        public void ejecutar(Caballero c, Atacable objetivo) {

            if (arco.getDurabilidad() <= 0) {
                new Basico().ejecutar(c, objetivo);
                return;
            }

            objetivo.recibirDanio(arco.getDanioBase());
            arco.disparar(objetivo);
        }

        @Override
        public int getAlcance() {
            return 280;
        }
    }
}
