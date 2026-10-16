package escenario;

import interfaces.Atacable;

public class Estructura implements Atacable {
    private String nombre; // Añadido según UML
    private int resistencia;

    public Estructura(String nombre, int resistencia) {
        this.nombre = nombre;
        this.resistencia = resistencia;
    }

    @Override
    public void recibirDanio(int danio) {
        resistencia -= danio;
        System.out.println("La estructura " + nombre + " recibe " + danio + " de daño.");
        if (estaDestruida()) {
            System.out.println("La estructura fue destruida.");
        }
    }

    // Método requerido por el UML
    public boolean estaDestruida() {
        return resistencia <= 0;
    }
}
