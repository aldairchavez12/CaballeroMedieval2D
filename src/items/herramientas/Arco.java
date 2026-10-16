package items.herramientas;

import entidades.Entidad;
import interfaces.Atacable;

public class Arco extends Herramienta {
    private int danioBase;
    private double alcance;

    public Arco(String nombre, String descripcion, int durabilidad, int danioBase, double alcance) {
        super(nombre, descripcion, durabilidad);
        this.danioBase = danioBase;
        this.alcance = alcance;
    }

    @Override
    public void usar(Entidad usuario) {
        System.out.println(usuario.getNombre() + " apunta con el arco " + getNombre() + ".");
    }

    public void disparar(Atacable objetivo) {
        System.out.println("Disparando con el arco " + getNombre() + ".");
        reducirDurabilidad(1);
    }
}
