package items.herramientas;

import entidades.Entidad;

public class Espada extends Herramienta {
    private int danio;
    private double alcance;

    public Espada(String nombre, String descripcion, int durabilidad, int danio, double alcance) {
        super(nombre, descripcion, durabilidad);
        this.danio = danio;
        this.alcance = alcance;
    }

    public int getDanio() {
        return danio;
    }

    @Override
    public void usar(Entidad usuario) {
        System.out.println(usuario.getNombre() + " blande la espada " + nombre + " haciendo " + danio + " de daño.");
        reducirDurabilidad(1);
    }
}