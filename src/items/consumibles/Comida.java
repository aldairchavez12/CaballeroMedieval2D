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

        if (!disponible() || usuario == null) {
            return;
        }

        usuario.curar(curacion);
        consumir();

        System.out.println(
            usuario.getNombre()
            + " consumió " + getNombre()
            + ". Vida actual: " + usuario.getVida()
        );
    }
}