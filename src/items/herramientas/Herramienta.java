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
        this.durabilidad -= cantidad;
        if (this.durabilidad < 0) {
            this.durabilidad = 0;
        }
    }
}