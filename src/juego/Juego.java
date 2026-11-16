package juego;

import entidades.Caballero;
import entidades.Enemigo;
import entidades.Boss;
import entidades.Entidad;
import escenario.Estructura;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Control de partida: mismo contrato publico original, con recorrido lateral. */
public class Juego {
    public static final int ANCHO_NIVEL = 4800;
    public static final int META_X = 4670;
    public enum Estado { INTRO, JUGANDO, ENTRE_CAPITULOS, VICTORIA, DERROTA }

    private static Juego instancia;
    public static synchronized Juego getInstancia() {
        if (instancia == null) instancia = new Juego();
        return instancia;
    }

    private final List<Entidad> entidades;
    private final List<Estructura> estructuras;
    private final Caballero caballero;
    private final List<Enemigo> enemigos = new ArrayList<>();
    private final Map<Enemigo, Integer> posicionesEnemigos = new IdentityHashMap<>();
    private final Map<Enemigo, Long> ultimoAtaquePorEnemigo = new IdentityHashMap<>();
    private Enemigo enemigo;
    private Estado estado;
    private int capitulo;
    private int enemigoX;
    private static final int ENEMIGO_Y = 550;
    private static final int ANCHO_ENEMIGO = 45;
    private static final int VELOCIDAD_ENEMIGO = 2;
    private static final int DISTANCIA_DETECCION = 340;
    private static final int DISTANCIA_ATAQUE_ENEMIGO = 65;
    private static final int ALCANCE_CABALLERO = 90;
    private static final long COOLDOWN_CABALLERO = 350_000_000L;
    private static final long COOLDOWN_ENEMIGO = 1_200_000_000L;
    private long ultimoAtaqueCaballero;
    private long ultimoAtaqueEnemigo;
    private int golpesEnemigo;

    private Juego() {
        entidades = new ArrayList<>();
        estructuras = new ArrayList<>();
        caballero = new Caballero("Caballero de Valdoria", 100, 15, 0, null, null);
        entidades.add(caballero);
        capitulo = 1;
        estado = Estado.INTRO;
        prepararCapitulo();
    }

    public void iniciar() {
        estado = Estado.INTRO;
        System.out.println("La Fortaleza Oscura");
    }

    /** Fabrica simple: no altera constructores de Enemigo ni Boss. */
    private Enemigo crearEnemigo(int tipo) {
        switch (tipo) {
            case 1: return new Enemigo("Goblin explorador", 30, 5, 5, 2.0);
            case 2: return new Enemigo("Guardia de la fortaleza", 45, 8, 10, 2.0);
            case 3: return new Enemigo("Sombra errante", 55, 10, 15, 2.0);
            case 4: return new Enemigo("Guardian del bosque", 75, 10, 25, 2.0);
            case 5: return new Enemigo("Capitan oscuro", 95, 12, 25, 2.0);
            case 6: return new Boss("Senor de las Sombras", 120, 16, 100, 2.0, 14, 1);
            default: throw new IllegalArgumentException("Tipo de enemigo desconocido: " + tipo);
        }
    }

    private void agregarEnemigo(int tipo, int x) {
        Enemigo nuevo = crearEnemigo(tipo);
        enemigos.add(nuevo);
        entidades.add(nuevo);
        posicionesEnemigos.put(nuevo, x);
    }

    private void prepararCapitulo() {
        entidades.removeIf(e -> e instanceof Enemigo);
        enemigos.clear();
        posicionesEnemigos.clear();
        ultimoAtaquePorEnemigo.clear();
        // Cambiar capitulo reinicia el recorrido sin reemplazar al Caballero.
        caballero.reiniciarPosicion();
        if (capitulo < 1 || capitulo > 3) {
            enemigo = null;
            return;
        }
        int tipoMenor = capitulo;
        agregarEnemigo(tipoMenor, 740);
        agregarEnemigo(tipoMenor, 1520);
        agregarEnemigo(tipoMenor, 2350);
        agregarEnemigo(tipoMenor, 3140);
        agregarEnemigo(capitulo + 3, 4160);
        enemigo = enemigos.get(0);
        enemigoX = posicionesEnemigos.get(enemigo);
        ultimoAtaqueCaballero = 0;
        ultimoAtaqueEnemigo = 0;
        golpesEnemigo = 0;
    }

    public void continuarHistoria() {
        if (estado == Estado.INTRO) {
            estado = Estado.JUGANDO;
            System.out.println("Comienza el capitulo " + capitulo);
        } else if (estado == Estado.ENTRE_CAPITULOS) {
            capitulo++;
            prepararCapitulo();
            estado = Estado.INTRO;
        }
    }

    public void actualizar() {
        if (estado != Estado.JUGANDO) return;
        if (caballero.estaVivo()) caballero.update();
        for (Enemigo e : enemigos) {
            if (e.estaVivo()) actualizarIAEnemigo(e);
        }
        if (!caballero.estaVivo()) {
            estado = Estado.DERROTA;
            System.out.println("El Caballero ha sido derrotado.");
            return;
        }
        boolean todosDerrotados = true;
        for (Enemigo e : enemigos) {
            if (e.estaVivo()) { todosDerrotados = false; break; }
        }
        if (todosDerrotados && caballero.getX() >= META_X - 60) {
            estado = capitulo == 3 ? Estado.VICTORIA : Estado.ENTRE_CAPITULOS;
            System.out.println(capitulo == 3 ? "Victoria: Valdoria ha sido liberada." : "Capitulo completado: " + capitulo);
        }
    }

    private void actualizarIAEnemigo(Enemigo e) {
        int x = posicionesEnemigos.get(e);
        int diferencia = (caballero.getX() + Caballero.ANCHO / 2) - (x + ANCHO_ENEMIGO / 2);
        int distancia = Math.abs(diferencia);
        if (distancia <= DISTANCIA_DETECCION && distancia > DISTANCIA_ATAQUE_ENEMIGO) {
            x += Integer.compare(diferencia, 0) * VELOCIDAD_ENEMIGO;
            x = Math.max(0, Math.min(ANCHO_NIVEL - ANCHO_ENEMIGO, x));
            posicionesEnemigos.put(e, x);
        }
        boolean alturaCompatible = Math.abs(caballero.getY() - ENEMIGO_Y) < 55;
        if (distancia <= DISTANCIA_ATAQUE_ENEMIGO && alturaCompatible) {
            long ahora = System.nanoTime();
            long anterior = ultimoAtaquePorEnemigo.getOrDefault(e, 0L);
            if (ahora - anterior >= COOLDOWN_ENEMIGO) {
                if (e instanceof Boss) {
                    golpesEnemigo++;
                    if (golpesEnemigo % 3 == 0) ((Boss) e).ataqueEspecial(caballero);
                    else e.atacar(caballero);
                } else e.atacar(caballero);
                ultimoAtaquePorEnemigo.put(e, ahora);
                ultimoAtaqueEnemigo = ahora;
                System.out.println("Vida del Caballero: " + caballero.getVida());
            }
        }
    }

    public void atacarEnemigo() {
        if (estado != Estado.JUGANDO || !caballero.estaVivo()) return;
        long ahora = System.nanoTime();
        if (ahora - ultimoAtaqueCaballero < COOLDOWN_CABALLERO) return;
        int centroCaballero = caballero.getX() + Caballero.ANCHO / 2;
        Enemigo objetivo = null;
        int distanciaMin = Integer.MAX_VALUE;
        for (Enemigo e : enemigos) {
            if (!e.estaVivo()) continue;
            int centro = posicionesEnemigos.get(e) + ANCHO_ENEMIGO / 2;
            int distancia = Math.abs(centroCaballero - centro);
            if (distancia <= ALCANCE_CABALLERO && Math.abs(caballero.getY() - ENEMIGO_Y) < 60 && distancia < distanciaMin) {
                objetivo = e;
                distanciaMin = distancia;
            }
        }
        if (objetivo == null) return;
        caballero.atacar(objetivo);
        ultimoAtaqueCaballero = ahora;
        enemigo = objetivo;
        enemigoX = posicionesEnemigos.get(objetivo);
        System.out.println(objetivo.getNombre() + " - Vida: " + objetivo.getVida());
    }

    public String getTituloCapitulo() {
        switch (capitulo) {
            case 1: return "CAPITULO I - BOSQUE DE LAS SOMBRAS";
            case 2: return "CAPITULO II - FORTALEZA ABANDONADA";
            case 3: return "CAPITULO III - TRONO DE LA OSCURIDAD";
            default: return "";
        }
    }

    public String getMensajeHistoria() {
        if (estado == Estado.VICTORIA) return "VICTORIA: EL REINO DE VALDORIA ES LIBRE";
        if (estado == Estado.DERROTA) return "DERROTA: EL CABALLERO HA CAIDO";
        if (estado == Estado.ENTRE_CAPITULOS) return "PUERTA ABIERTA - ENTER PARA CONTINUAR";
        if (estado == Estado.INTRO) {
            switch (capitulo) {
                case 1: return "Cruza el bosque y derrota al guardian. ENTER para empezar.";
                case 2: return "Libera la fortaleza del ejercito oscuro. ENTER para empezar.";
                case 3: return "Alcanza el trono y derrota al Senor de las Sombras. ENTER.";
                default: return "";
            }
        }
        return "";
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
