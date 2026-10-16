package items.consumibles;

import entidades.Entidad;

public class Pocima extends Consumible {
    private int curacion;
    private String efecto;

    public Pocima(String nombre, String descripcion, int cantidad, int curacion, String efecto) {
        super(nombre, descripcion, cantidad);
        this.curacion = curacion;
        this.efecto = efecto;
    }

    @Override
    public void usar(Entidad usuario) {
        if (disponible()) {
            System.out.println(usuario.getNombre() + " toma la pócima " +  getNombre() + " (" + efecto + ") regenerando " + curacion + " HP.");
            consumir();
        } else {
            System.out.println("La pócima " + getNombre() + " está agotada.");
        }
    }
}
