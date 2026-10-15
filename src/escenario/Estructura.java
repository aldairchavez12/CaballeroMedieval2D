package escenario;

public class Estructura {

    private int resistencia;

    public Estructura(int resistencia) {
        this.resistencia = resistencia;
    }

    public void recibirDanio(int danio) {

        resistencia -= danio;

        System.out.println(
                "La estructura recibe "
                + danio + " de daño."
        );

        if (resistencia <= 0) {
            System.out.println("La estructura fue destruida.");
        }
    }
}