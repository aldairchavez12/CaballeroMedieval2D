package items;

import entidades.Entidad;

public abstract class Item {

    private String nombre;
    private String descripcion;

    public Item(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public abstract void usar(Entidad usuario);

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}