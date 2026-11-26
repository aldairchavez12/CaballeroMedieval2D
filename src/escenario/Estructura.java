package escenario;

import interfaces.Atacable;

public class Estructura implements Atacable {

    private final String nombre;
    private int resistencia;
    private final int resistenciaMaxima;
    private final int x;
    private final int y;

    public Estructura(String nombre, int resistencia) {
        this(nombre, resistencia, 0, 0);
    }

    public Estructura(String nombre, int resistencia,
                      int x, int y) {

        this.nombre = nombre;
        this.resistencia = Math.max(0, resistencia);
        this.resistenciaMaxima = this.resistencia;
        this.x = x;
        this.y = y;
    }

    @Override
    public void recibirDanio(int danio) {

        if (danio > 0 && !estaDestruida()) {

            resistencia = Math.max(
                0, resistencia - danio
            );
        }
    }

    public boolean estaDestruida() {
        return resistencia == 0;
    }

    public String getNombre() {
        return nombre;
    }

    public int getResistencia() {
        return resistencia;
    }

    public int getResistenciaMaxima() {
        return resistenciaMaxima;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}