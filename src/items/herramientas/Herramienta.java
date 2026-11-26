package items.herramientas;

import items.Item;

public abstract class Herramienta extends Item {

    protected int durabilidad;
    private final int durabilidadMaxima;

    public Herramienta(String nombre, String descripcion, int durabilidad) {
        super(nombre, descripcion);
        this.durabilidad = Math.max(0, durabilidad);
        this.durabilidadMaxima = this.durabilidad;
    }

    public int getDurabilidadMaxima() {
        return durabilidadMaxima;
    }

    public int getDurabilidad() {
        return durabilidad;
    }

    public void reducirDurabilidad(int cantidad) {
        if (cantidad > 0) {
            durabilidad = Math.max(0, durabilidad - cantidad);
        }
    }
}