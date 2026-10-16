package items.consumibles;

import entidades.Entidad;

public class Comida extends Consumible {
    private int curacion;

    public Comida(String nombre, String descripcion, int cantidad, int curacion) {
        super(nombre, descripcion, cantidad);
        this.curacion = curacion;
    }

    @Override
    public void usar(Entidad usuario) {
        if (disponible()) {
            System.out.println(usuario.getNombre() + " consume " + getNombre() + " y recupera " + curacion + " puntos de vida.");
            consumir();
        } else {
            System.out.println("No queda suficiente " + getNombre() + ".");
        }
    }
}
