package escenario;

import items.Item;
import items.Inventario;

public final class Cofre {

    private final int x;
    private final int y;
    private final Item contenido;
    private boolean abierto;

    public Cofre(int x, int y, Item contenido) {

        if (contenido == null) {
            throw new IllegalArgumentException(
                "El cofre necesita un objeto."
            );
        }

        this.x = x;
        this.y = y;
        this.contenido = contenido;
        this.abierto = false;
    }

    public boolean abrir(Inventario inventario) {

        if (abierto || inventario == null) {
            return false;
        }

        if (!inventario.agregarItem(contenido)) {
            return false;
        }

        abierto = true;

        return true;
    }

    public boolean estaCerca(int jugadorX, int jugadorY) {

        return Math.abs(jugadorX - x) <= 100
            && Math.abs(jugadorY - (y - 30)) <= 80;
    }

    public boolean estaAbierto() {
        return abierto;
    }

    public String getNombreContenido() {
        return contenido.getNombre();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}