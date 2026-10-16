package items.herramientas;

import items.Item;

public abstract class Herramienta extends Item {

    protected int durabilidad;

    public Herramienta(String nombre, String descripcion, int durabilidad) {
        super(nombre, descripcion);
        this.durabilidad = durabilidad;
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
