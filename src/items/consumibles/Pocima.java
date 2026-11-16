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

        if (!disponible() || usuario == null) {
            return;
        }

        usuario.curar(curacion);
        consumir();

        System.out.println(
            usuario.getNombre()
            + " utilizó " + getNombre()
            + ". Vida actual: " + usuario.getVida()
        );
    }
}