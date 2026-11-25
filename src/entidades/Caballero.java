package entidades;

import interfaces.*;
import items.herramientas.*;
import juego.Juego;

public class Caballero extends Entidad {
    private int experiencia;
    private Herramienta arma;
    private Herramienta defensa;
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
    private Armadura armaduraEquipada;
    private Escudo escudoEquipado;
    private boolean mirandoDerecha = true;
    private AtaqueStrategy estrategiaAtaque = new AtaqueStrategy.Basico();
    public Caballero(String nombre, int vida, int ataqueBase, int experiencia, Herramienta arma, Herramienta defensa) {
        super(nombre, vida, ataqueBase);
        this.experiencia = experiencia;
        this.arma = null;
        this.defensa = defensa;
        this.x = 100;
        this.y = 550;
        this.direccion = 0;
        this.velocidadVertical = 0;
        this.enSuelo = true;
        this.arma = null;
        this.estrategiaAtaque = new AtaqueStrategy.Basico();
    }

    @Override
    public void update() {
        moverse();
        velocidadVertical += GRAVEDAD;
        y += (int) velocidadVertical;
        int posicionSuelo = 620 - ALTO;
        if (y >= posicionSuelo) {
            y = posicionSuelo;
            velocidadVertical = 0;
            enSuelo = true;
        } else {
            enSuelo = false;
        }
        // Unico ajuste de una regla existente: nivel largo, no pantalla de 1280 px.
        x = Math.max(0, Math.min(x, Juego.ANCHO_NIVEL - ANCHO));
    }

    @Override
    public void moverse() { x += direccion * VELOCIDAD; }
    public void establecerDireccion(int direccion) {

        this.direccion = Integer.compare(direccion, 0);

        if (this.direccion > 0) {
            mirandoDerecha = true;
        } else if (this.direccion < 0) {
            mirandoDerecha = false;
        }
    }

    public boolean miraDerecha() {
        return mirandoDerecha;
    }   
     @Override
    public void atacar(Atacable objetivo) {
        if (objetivo != null) {
            estrategiaAtaque.ejecutar(this, objetivo);
        }
    }
    public int getAlcanceAtaque() {
        return estrategiaAtaque.getAlcance();
    }
    public void saltar() {
        if (enSuelo) {
            velocidadVertical = FUERZA_SALTO;
            enSuelo = false;
        }
    }

    public void equiparArma(Herramienta arma) {

        this.arma = arma;

        if (arma instanceof Espada) {

            estrategiaAtaque = new AtaqueStrategy.ConEspada(
                (Espada) arma
            );

        } else if (arma instanceof Arco) {

            estrategiaAtaque = new AtaqueStrategy.ConArco(
                (Arco) arma
            );

        } else {

            estrategiaAtaque = new AtaqueStrategy.Basico();
        }
    }

    public void equiparDefensa(Herramienta defensa) { this.defensa = defensa; }

    public void equiparArmadura(Armadura armadura) {
        this.armaduraEquipada = armadura;
    }

    public void equiparEscudo(Escudo escudo) {
        this.escudoEquipado = escudo;
    }

    public Herramienta getArmaEquipada() {
        return arma;
    }

    public Armadura getArmaduraEquipada() {
        return armaduraEquipada;
    }

    public Escudo getEscudoEquipado() {
        return escudoEquipado;
    }

    
    @Override
    public void recibirDanio(int danio) {

        if (danio <= 0) return;

        int restante = danio;

        if (escudoEquipado != null
                && escudoEquipado.getDurabilidad() > 0) {

            restante = Math.max(
                0,
                restante - escudoEquipado.getBloqueo()
            );

            escudoEquipado.reducirDurabilidad(1);
        }

        if (armaduraEquipada != null
                && armaduraEquipada.getDurabilidad() > 0) {

            restante = Math.max(
                0,
                restante - armaduraEquipada.getProteccion()
            );

            armaduraEquipada.reducirDurabilidad(1);
        }

        super.recibirDanio(restante);
        
    }


    public int getX() { return x; }
    public int getY() { return y; }
    public int getExperiencia() { return experiencia; }

    // Extension minima: reinicio entre capitulos. No elimina ningun metodo UML.
    public void reiniciarPosicion() {
        x = 100;
        y = 550;
        direccion = 0;
        velocidadVertical = 0;
        enSuelo = true;
    }
}
