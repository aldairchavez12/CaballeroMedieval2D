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
    public int getDanioBase() {
      return danioBase;
}

    @Override
    public void usar(Entidad usuario) {
        System.out.println(usuario.getNombre() + " apunta con el arco " + getNombre() + ".");
    }

    public int getAlcance() {
        return Math.max(1, (int) alcance);
    }

    public void disparar(Atacable objetivo) {
        disparar();
    }

    public void disparar() {
        System.out.println("Disparando con el arco " + getNombre() + ".");
        reducirDurabilidad(1);
    }
}
