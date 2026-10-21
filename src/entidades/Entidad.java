package entidades;

import interfaces.Atacable;

public abstract class Entidad implements Atacable {

    private String nombre;
    private int vida;
    private int ataqueBase;

    public Entidad(String nombre, int vida, int ataqueBase) {
        this.nombre = nombre;
        this.vida = vida;
        this.ataqueBase = ataqueBase;
    }

    public abstract void update();

    public abstract void moverse();

    public abstract void atacar(Atacable objetivo);

    @Override
    public void recibirDanio(int danio) {
        if (danio > 0) {
            vida = Math.max(0, vida - danio);
        }
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