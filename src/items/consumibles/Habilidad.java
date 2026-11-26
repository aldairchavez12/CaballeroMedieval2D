package items.consumibles;

import entidades.Entidad;
import entidades.Caballero;

public class Habilidad extends Consumible {

    private final int poder;
    private final String duracion;
    private final String tipoEfecto;

    public Habilidad(String nombre, String descripcion,
                     int cantidad, int poder,
                     String duracion, String tipoEfecto) {

        super(nombre, descripcion, cantidad);

        this.poder = poder;
        this.duracion = duracion;
        this.tipoEfecto = tipoEfecto;
    }

    public void aplicarEfecto(Entidad usuario) {

        if (usuario instanceof Caballero
                && "Furia".equalsIgnoreCase(tipoEfecto)) {

            Caballero caballero = (Caballero) usuario;

            caballero.activarFuria(poder, obtenerSegundos());
        }
    }

    @Override
    public void usar(Entidad usuario) {

        if (!disponible() || usuario == null
                || !usuario.estaVivo()) {
            return;
        }

        if (usuario instanceof Caballero
                && "Furia".equalsIgnoreCase(tipoEfecto)) {

            aplicarEfecto(usuario);
            consumir();
        }
    }

    private int obtenerSegundos() {

        try {
            return Math.max(1, Integer.parseInt(
                duracion.replaceAll("[^0-9]", "")
            ));
        } catch (NumberFormatException e) {
            return 10;
        }
    }
}