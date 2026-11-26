package entidades;

import interfaces.*;
import items.Inventario;
import items.Item;
import items.consumibles.Consumible;
import java.util.List;
import items.herramientas.*;
import juego.Juego;

public class Caballero extends Entidad {
    private int bonoAtaque = 0;
    private int duracionHabilidad = 0;
    private int experiencia;
    private int monedas = 0;
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
    private final Inventario inventario = new Inventario(12);
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

    public void activarFuria(int poder, int duracion) {

        if (poder <= 0 || duracion <= 0) {
            return;
        }

        bonoAtaque = poder;
        duracionHabilidad = (int) Math.ceil(duracion * 1000.0 / 16.0);

        System.out.println(
            "Furia activada: +" + poder + " de ataque"
        );
    }

    public void actualizarHabilidad() {

        if (duracionHabilidad <= 0) {
            return;
        }

        duracionHabilidad--;

        if (duracionHabilidad == 0) {
            bonoAtaque = 0;
            System.out.println("Furia finalizada");
        }
    }

    public void cancelarHabilidad() {
        bonoAtaque = 0;
        duracionHabilidad = 0;
    }

    public int getBonoAtaque() {
        return bonoAtaque;
    }

    public boolean tieneFuriaActiva() {
        return duracionHabilidad > 0;
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
    public void verificarArma() {

        if (arma != null && arma.getDurabilidad() <= 0) {
            equiparArma(null);
        }
    }

    public boolean usaAtaqueDistancia() {

        verificarArma();
        return estrategiaAtaque.esAtaqueDistancia();
    }

    public boolean ejecutarAtaque(
            Atacable objetivo,
            LanzadorProyectiles lanzador) {

        if (!estaVivo()) return false;

        verificarArma();

        return estrategiaAtaque.ejecutarAtaque(
            this,
            objetivo,
            lanzador
        );
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
    public boolean eliminarObjeto(Item item) {

        if (!inventario.eliminarItem(item)) {
            return false;
        }

        if (item == arma) {
            equiparArma(null);
        }

        if (item == armaduraEquipada) {
            equiparArmadura(null);
        }

        if (item == escudoEquipado) {
            equiparEscudo(null);
        }

        if (item == defensa) {
            equiparDefensa(null);
        }

        return true;
    }

    public boolean usarObjeto(int indice) {

        List<Item> objetos = inventario.obtenerItems();

        if (indice < 0 || indice >= objetos.size()
                || !estaVivo()) {
            return false;
        }

        Item item = objetos.get(indice);

        if (item instanceof Herramienta) {

            Herramienta herramienta = (Herramienta) item;

            if (herramienta.getDurabilidad() <= 0) {
                return false;
            }

            if (item instanceof Espada
                    || item instanceof Arco) {

                equiparArma(herramienta);

            } else if (item instanceof Armadura) {

                equiparArmadura((Armadura) item);

            } else if (item instanceof Escudo) {

                equiparEscudo((Escudo) item);

            } else {

                return false;
            }

            return true;
        }

        if (item instanceof Flecha) {
            return false;
        }

        if (item instanceof Consumible) {

            Consumible consumible = (Consumible) item;

            if (!consumible.disponible()) {
                return false;
            }

            int cantidadAnterior = consumible.getCantidad();

            item.usar(this);

            if (consumible.getCantidad() == cantidadAnterior) {
                return false;
            }

            if (!consumible.disponible()) {
                eliminarObjeto(item);
            }

            return true;
        }

        item.usar(this);
        return true;
    }

    public int getMonedas() {
        return monedas;
    }

    public void agregarMonedas(int cantidad) {

        if (cantidad > 0) {

            monedas = (int) Math.min(
                Integer.MAX_VALUE,
                (long) monedas + cantidad
            );
        }
    }

    public boolean gastarMonedas(int cantidad) {

        if (cantidad <= 0 || cantidad > monedas) {
            return false;
        }

        monedas -= cantidad;
        return true;
    }

    public void reiniciarMonedas() {
        monedas = 0;
    }

    public Inventario getInventario() {
        return inventario;
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
