package entidades;

import interfaces.Atacable;
import items.herramientas.Herramienta;

public class Caballero extends Entidad {

    private int experiencia;
    private Herramienta arma;
    private Herramienta defensa;

    // Estado del personaje en el escenario
    private int x;
    private int y;

    private int direccion;
    private double velocidadVertical;
    private boolean enSuelo;

    private static final int VELOCIDAD = 5;
    private static final double GRAVEDAD = 0.7;
    private static final double FUERZA_SALTO = -14;

    public static final int ANCHO = 45;
    public static final int ALTO = 70;

    public Caballero(
            String nombre,
            int vida,
            int ataqueBase,
            int experiencia,
            Herramienta arma,
            Herramienta defensa
    ) {
        super(nombre, vida, ataqueBase);

        this.experiencia = experiencia;
        this.arma = arma;
        this.defensa = defensa;

        this.x = 100;
        this.y = 550;

        this.direccion = 0;
        this.velocidadVertical = 0;
        this.enSuelo = true;
    }

    @Override
    public void update() {

        moverse();

        // Aplicar gravedad
        velocidadVertical += GRAVEDAD;

        y += (int) velocidadVertical;

        // Colisión con el suelo
        int posicionSuelo = 620 - ALTO;

        if (y >= posicionSuelo) {
            y = posicionSuelo;
            velocidadVertical = 0;
            enSuelo = true;
        } else {
            enSuelo = false;
        }

        // Limitar desplazamiento horizontal
        x = Math.max(0, Math.min(x, 1280 - ANCHO));
    }

    @Override
    public void moverse() {
        x += direccion * VELOCIDAD;
    }

    public void establecerDireccion(int direccion) {
        this.direccion = Integer.compare(direccion, 0);
    }

    @Override
    public void atacar(Atacable objetivo) {

        if (objetivo != null) {
            objetivo.recibirDanio(getAtaqueBase());
        }
    }

    public void saltar() {

        if (enSuelo) {
            velocidadVertical = FUERZA_SALTO;
            enSuelo = false;
        }
    }

    public void equiparArma(Herramienta arma) {
        this.arma = arma;
    }

    public void equiparDefensa(Herramienta defensa) {
        this.defensa = defensa;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getExperiencia() {
        return experiencia;
    }
}