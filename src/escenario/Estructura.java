package escenario;

import interfaces.Atacable;

public class Estructura implements Atacable {

    private int resistencia;

    public Estructura(int resistencia) {
        this.resistencia = resistencia;
    }

    @Override
    public void recibirDanio(int danio) {

        resistencia -= danio;

        System.out.println(
                "La estructura recibe "
                        + danio
                        + " de daño."
        );

        if (resistencia <= 0) {
            System.out.println(
                    "La estructura fue destruida."
            );
        }
    }
}