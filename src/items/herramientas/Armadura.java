package items.herramientas;

import entidades.Entidad;

public class Armadura extends Herramienta {
    private int proteccion;
    private double peso;

    public Armadura(String nombre, String descripcion, int durabilidad, int proteccion, double peso) {
        super(nombre, descripcion, durabilidad);
        this.proteccion = proteccion;
        this.peso = peso;
    }

    public int getProteccion() {
        return proteccion;
    }

    @Override
    public void usar(Entidad usuario) {
        System.out.println(usuario.getNombre() + " se equipa la armadura " + getNombre() + ".");
    }
}
