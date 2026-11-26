
package items.herramientas;

import entidades.Entidad;
import interfaces.Atacable;
import items.Item;

public class Flecha extends Item {

    private final int danio;
    private final double velocidad;
    private int cantidad;

    public Flecha(String nombre, String descripcion,
                  int danio, double velocidad, int cantidad) {

        super(nombre, descripcion);
        this.danio = danio;
        this.velocidad = velocidad;
        this.cantidad = Math.max(0, cantidad);
    }

    // Conserva compatibilidad con el constructor anterior.
    public Flecha(String nombre, String descripcion,
                  int danio, double velocidad) {

        this(nombre, descripcion, danio, velocidad, 1);
    }

    public boolean consumirUna() {
        if (cantidad <= 0) return false;
        cantidad--;
        return true;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getVelocidad() {
        return velocidad;
    }

    @Override
    public void usar(Entidad usuario) {
        System.out.println("Flechas disponibles: " + cantidad);
    }

    public void impactar(Atacable objetivo) {
        impactar(objetivo, 0);
    }

    public void impactar(
            Atacable objetivo, int bonoDanio) {
        if (objetivo != null) {
            objetivo.recibirDanio(
                danio + Math.max(0, bonoDanio)
            );
        }
    }
}
