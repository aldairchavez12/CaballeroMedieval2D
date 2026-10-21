package items.consumibles;

import items.Item;

public abstract class Consumible extends Item {

    protected int cantidad;

    public Consumible(String nombre, String descripcion, int cantidad) {
        super(nombre, descripcion);
        this.cantidad = Math.max(0, cantidad);
    }

    public void consumir() {
        if (cantidad > 0) {
            cantidad--;
        }
    }

    public boolean disponible() {
        return cantidad > 0;
    }

    public int getCantidad() {
        return cantidad;
    }
}