package entidades;

import interfaces.AtaqueStrategy;
import interfaces.Atacable;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
public abstract class Entidad implements Atacable {

    private String nombre;
    private int vida;
    private final int vidaMaxima;
    private int ataqueBase;
    private final PropertyChangeSupport observadores =
        new PropertyChangeSupport(this);
    public Entidad(String nombre, int vida, int ataqueBase) {
        this.nombre = nombre;
        this.vida = vida;
        this.vidaMaxima = vida;
        this.ataqueBase = ataqueBase;
    }
    public void agregarObservador(PropertyChangeListener observador) {
        observadores.addPropertyChangeListener("vida", observador);
    }

    public void quitarObservador(PropertyChangeListener observador) {
        observadores.removePropertyChangeListener("vida", observador);
    }

    public int getVidaMaxima() {
        return vidaMaxima;
    }

    public void restaurarVida() {
        int anterior = vida;
        vida = vidaMaxima;

        observadores.firePropertyChange(
            "vida",
            anterior,
            vida
        );
    }

    public abstract void update();

    public abstract void moverse();

    public abstract void atacar(Atacable objetivo);

    public void curar(int cantidad) {

        if (cantidad <= 0) {
            return;
        }

        int vidaAnterior = vida;

        vida = Math.min(vidaMaxima, vida + cantidad);

        observadores.firePropertyChange(
            "vida",
            vidaAnterior,
            vida
        );
    }

    @Override
    public void recibirDanio(int danio) {

        if (danio <= 0) {
            return;
        }

        int vidaAnterior = vida;

        vida = Math.max(0, vida - danio);

        observadores.firePropertyChange(
            "vida",
            vidaAnterior,
            vida
        );
    }


    public boolean estaVivo() {
        return vida > 0;
    }

    public String getNombre() {
        return nombre;
    }

    public int getVida() {
        return vida;
    }

    public int getAtaqueBase() {
        return ataqueBase;
    }
}