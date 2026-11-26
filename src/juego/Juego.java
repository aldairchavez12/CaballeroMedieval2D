package juego;

import entidades.*;
import escenario.*;
import items.*;
import items.herramientas.*;
import items.consumibles.*;
import com.google.gson.*;
import java.nio.file.*;
import java.io.*;

import java.util.*;
/** Control de partida: mismo contrato publico original, con recorrido lateral. */
public class Juego {

    private final DatosJuego datos = new DatosJuego();
    private final GestorNiveles gestorNiveles =
        new GestorNiveles(datos);
    public static final int ANCHO_NIVEL = 4800;
    public static final int META_X = 4670;
    public enum Estado { INTRO, JUGANDO, ENTRE_CAPITULOS, VICTORIA, DERROTA }

    private static Juego instancia;
    public static synchronized Juego getInstancia() {
        if (instancia == null) instancia = new Juego();
        return instancia;
    }
    private final GestorProyectiles gestorProyectiles = new GestorProyectiles();
    private final GestorAliados gestorAliados =new GestorAliados();
    private final GestorEnemigos gestorEnemigos;
    private final GestorCombate gestorCombate;
    private final List<Entidad> entidades;
    private final List<Estructura> estructuras;
    private final Caballero caballero;
    private final List<Enemigo> enemigos = new ArrayList<>();
    private final Map<Enemigo, Integer> posicionesEnemigos = new IdentityHashMap<>();
    private Enemigo enemigo;
    private Estado estado;
    private int capitulo;
    private int enemigoX;
    private static final int ENEMIGO_Y = 550;
    private final CreadorItems creadorItems = new CreadorItems(datos);
   



    private Juego() {
        entidades = new ArrayList<>();
        estructuras = new ArrayList<>();
        caballero = new Caballero("Caballero de Valdoria", 100, 15, 0, null, null);
        entidades.add(caballero);
        gestorCombate = new GestorCombate(
            caballero,
            caballero.getInventario(),
            gestorProyectiles,
            enemigos,
            posicionesEnemigos
        );
        gestorEnemigos = new GestorEnemigos(
            caballero,
            enemigos,
            posicionesEnemigos
        );
            
        capitulo = 1;
        estado = Estado.INTRO;
        prepararCapitulo();
        caballero.cancelarHabilidad();
        
    }

    public void iniciar() {
        estado = Estado.INTRO;
        System.out.println("La Fortaleza Oscura");
    }


    public void reiniciarCapitulo() {

        if (estado != Estado.DERROTA) {
            return;
        }

        // Recuperar toda la vida del caballero
        caballero.restaurarVida();

        // Volver a cargar enemigos y posiciones
        prepararCapitulo();

        // Reiniciar el estado de la partida
        estado = Estado.JUGANDO;

        System.out.println(
            "Capitulo " + capitulo + " reiniciado."
        );
    }
    public GestorAliados getGestorAliados() {
        return gestorAliados;
    }




    private void prepararCapitulo() {

        // Limpiar proyectiles del capitulo anterior
        gestorProyectiles.limpiar();

        estructuras.clear();

        estructuras.add(
            new Estructura("Barricada", 60, 520, 550)
        );

        estructuras.add(
            new Estructura("Caja de madera", 40, 1250, 570)
        );

        // Reiniciar posicion del jugador
        caballero.reiniciarPosicion();

        // Cargar enemigos y posiciones desde JSON
        enemigo = gestorNiveles.cargarCapitulo(
            capitulo,
            entidades,
            enemigos,
            posicionesEnemigos
        );

        // Mantener compatibilidad con PanelJuego
        enemigoX = enemigo == null
                ? 0
                : posicionesEnemigos.get(enemigo);

        // Reiniciar los sistemas de combate
        gestorCombate.reiniciar();
        gestorEnemigos.reiniciar();
        gestorAliados.reiniciar();

        // Reiniciar habilidades temporales
        caballero.cancelarHabilidad();
    }

    

    public Item crearObjeto(String id) {
        return creadorItems.crear(id);
    }

    public Inventario getInventario() {
        return caballero.getInventario();
    }

    public boolean recogerItem(Item item) {
        return caballero.getInventario().agregarItem(item);
    }

   

    public boolean usarItem(int indice) {

        if (estado != Estado.JUGANDO) {
            return false;
        }

        List<Item> objetos = caballero.getInventario().obtenerItems();

        if (indice < 0 || indice >= objetos.size()) {
            return false;
        }

        Item item = objetos.get(indice);

        // Equipar espada o arco
        if (item instanceof Espada || item instanceof Arco) {

            Herramienta arma = (Herramienta) item;

            if (arma.getDurabilidad() <= 0) {
                return false;
            }

            caballero.equiparArma(arma);

            System.out.println("Arma equipada: " + item.getNombre());
            return true;
        }

        // Equipar armadura
        if (item instanceof Armadura) {

            Armadura armadura = (Armadura) item;

            if (armadura.getDurabilidad() <= 0) {
                return false;
            }

            caballero.equiparArmadura(armadura);
            System.out.println("Armadura equipada");
            return true;
        }

        // Equipar escudo
        if (item instanceof Escudo) {

            Escudo escudo = (Escudo) item;

            if (escudo.getDurabilidad() <= 0) {
                return false;
            }

            caballero.equiparEscudo(escudo);
            System.out.println("Escudo equipado");
            return true;
        }

        // Las flechas se utilizan al disparar el arco
        if (item instanceof Flecha) {
            return false;
        }

        // Utilizar consumibles
        if (item instanceof Consumible) {

            Consumible consumible = (Consumible) item;

            if (!consumible.disponible()) {
                return false;
            }

            item.usar(caballero);

            if (!consumible.disponible()) {
                caballero.getInventario().eliminarItem(item);
            }

            return true;
        }

        item.usar(caballero);
        return true;
    }


    public void continuarHistoria() {
        if (estado == Estado.INTRO) {
            estado = Estado.JUGANDO;
            System.out.println("Comienza el capitulo " + capitulo);
        } else if (estado == Estado.ENTRE_CAPITULOS
            && enemigosDerrotados()) {capitulo++;
            prepararCapitulo();
            estado = Estado.INTRO;
        }
            
        
    }

    public void actualizar() {
        if (estado != Estado.JUGANDO) return;
        if (caballero.estaVivo()) caballero.update();
        caballero.actualizarHabilidad();
        gestorEnemigos.actualizar();
        if (!caballero.estaVivo()) {
            estado = Estado.DERROTA;
            System.out.println("El Caballero ha sido derrotado.");
            return;
        }
        
        if (enemigosDerrotados()
                && caballero.getX() >= META_X - 60) {

            estado = capitulo == 3
                    ? Estado.VICTORIA
                    : Estado.ENTRE_CAPITULOS;

            System.out.println(
                capitulo == 3
                    ? "Victoria: Valdoria ha sido liberada."
                    : "Capitulo completado: " + capitulo
            );
        }

        gestorProyectiles.actualizar(
            enemigos,
            posicionesEnemigos,
            ENEMIGO_Y
        );
        gestorAliados.actualizar(
            caballero,
            enemigos,
            this::getPosicionEnemigo
        );
    }
    public GestorProyectiles getGestorProyectiles() {
     return gestorProyectiles;
    }

    public List<Estructura> getEstructuras() {
        return Collections.unmodifiableList(estructuras);
    }

    public void atacarEnemigo() {

        if (estado != Estado.JUGANDO) {
            return;
        }

        Enemigo objetivo = gestorCombate.atacar(estructuras);

        if (objetivo != null) {

            enemigo = objetivo;

            enemigoX = posicionesEnemigos.get(objetivo);
        }
    }

    public String getTituloCapitulo() {
        return gestorNiveles.obtenerTitulo(capitulo);
    }

    
    public String getMensajeHistoria() {

        if (estado == Estado.VICTORIA) {
            return datos.obtenerHistoria(capitulo)
                    .get("final").getAsString();
        }

        if (estado == Estado.DERROTA) {
            return datos.obtenerMensajeDerrota();
        }

        if (estado == Estado.ENTRE_CAPITULOS) {
            return "PUERTA ABIERTA - ENTER PARA CONTINUAR";
        }

        if (estado == Estado.INTRO) {
            return datos.obtenerHistoria(capitulo)
                    .get("inicio").getAsString();
        }

        return "";
    }

    public Pocima crearPocimaVida() {

        JsonObject configuracion = datos.obtenerItem("pocima_vida");
        return new Pocima(
            configuracion.get("nombre").getAsString(),
            configuracion.get("descripcion").getAsString(),
            configuracion.get("cantidad").getAsInt(),
            configuracion.get("curacion").getAsInt(),
            configuracion.get("efecto").getAsString()
        );
    }

    public void finalizar() { estado = Estado.DERROTA; }
    public boolean estaActivo() { return estado == Estado.JUGANDO; }
    public Estado getEstado() { return estado; }
    public int getCapitulo() { return capitulo; }
    public Caballero getCaballero() { return caballero; }
    public Enemigo getEnemigo() { return enemigo; }
    public int getEnemigoX() { return enemigoX; }
    public int getEnemigoY() { return ENEMIGO_Y; }
    // Consultas adicionales para representar el nivel sin modificar Entidad/Enemigo/Boss.
    public List<Enemigo> getEnemigos() { return Collections.unmodifiableList(enemigos); }
    public int getPosicionEnemigo(Enemigo e) { return posicionesEnemigos.getOrDefault(e, -100); }
    public boolean enemigosDerrotados() {
        for (Enemigo e : enemigos) if (e.estaVivo()) return false;
        return true;
    }
    public int getEnemigosRestantes() {
        int restantes = 0;
        for (Enemigo e : enemigos) if (e.estaVivo()) restantes++;
        return restantes;
    }
}
