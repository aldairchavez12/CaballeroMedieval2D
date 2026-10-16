package items.consumibles;

import entidades.Entidad;

public class Habilidad extends Consumible {
    private int poder;
    private String duracion;
    private String tipoEfecto;

    public Habilidad(String nombre, String descripcion, int cantidad, int poder, String duracion, String tipoEfecto) {
        super(nombre, descripcion, cantidad);
        this.poder = poder;
        this.duracion = duracion;
        this.tipoEfecto = tipoEfecto;
    }

    public void aplicarEfecto(Entidad usuario) {
        System.out.println("Efecto " + tipoEfecto + " activado con poder " + poder + " durante " + duracion + ".");
    }

    @Override
    public void usar(Entidad usuario) {
        if (disponible()) {
            System.out.println(usuario.getNombre() + " activa la habilidad " + getNombre() + ".");
            aplicarEfecto(usuario);
            consumir();
        } else {
            System.out.println("No se puede usar la habilidad " + getNombre() + ".");
        }
    }
}
