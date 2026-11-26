package entidades;

import interfaces.Atacable;

public class Enemigo extends Entidad {

    protected int recompensa;
    protected double velocidad;

    private int x;
    private int desplazamiento;
    private int limiteX;

    public Enemigo(String nombre, int vida, int ataqueBase,
                   int recompensa, double velocidad) {

        super(nombre, vida, ataqueBase);

        this.recompensa = recompensa;
        this.velocidad = velocidad;
    }

    public void prepararMovimiento(int posicion,
                                    int desplazamiento,
                                    int limiteX) {

        this.x = posicion;
        this.desplazamiento = desplazamiento;
        this.limiteX = limiteX;
    }

    @Override
    public void update() {
        if (estaVivo()) {
            moverse();
        }
    }

    @Override
    public void moverse() {

        x = Math.max(
            0,
            Math.min(limiteX, x + desplazamiento)
        );

        desplazamiento = 0;
    }

    @Override
    public void atacar(Atacable objetivo) {

        if (estaVivo() && objetivo != null) {
            objetivo.recibirDanio(getAtaqueBase());
        }
    }

    public int getX() {
        return x;
    }

    public double getVelocidad() {
        return velocidad;
    }

    public int getRecompensa() {
        return recompensa;
    }
}