
package interfaces;

import entidades.Caballero;
import items.herramientas.Espada;
import items.herramientas.Arco;

public interface AtaqueStrategy {

    void ejecutar(Caballero caballero, Atacable objetivo);

    int getAlcance();

    class Basico implements AtaqueStrategy {

        @Override
        public void ejecutar(Caballero c, Atacable objetivo) {
            objetivo.recibirDanio(c.getAtaqueBase());
        }

        @Override
        public int getAlcance() {
            return 90;
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
