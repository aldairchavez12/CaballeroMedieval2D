package escenario;

import interfaces.Atacable;

public class Estructura implements Atacable {

    private String nombre;
    private int resistencia;

    public Estructura(String nombre, int resistencia) {
        this.nombre = nombre;
        this.resistencia = resistencia;
    }

    @Override
    public void recibirDanio(int danio) {
        if (danio > 0) {
            resistencia = Math.max(0, resistencia - danio);
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
}