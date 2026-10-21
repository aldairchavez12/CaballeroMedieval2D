package items.herramientas;

import entidades.Entidad;
import interfaces.Atacable;
import items.Item;

public class Flecha extends Item {
    private int danio;
    private double velocidad;

    public Flecha(String nombre, String descripcion, int danio, double velocidad) {
        super(nombre, descripcion);
        this.danio = danio;
        this.velocidad = velocidad;
    }

    @Override
    public void usar(Entidad usuario) {
        System.out.println(usuario.getNombre() + " coloca una flecha " + getNombre() + ".");
    }

    public void impactar(Atacable objetivo) {
        System.out.println("La flecha impacta causando " + danio + " de daño.");
        objetivo.recibirDanio(danio);
    }
}
