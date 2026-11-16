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

    private final JsonObject datosEnemigos =
            leerJson("enemigos.json");

    private final JsonObject datosNiveles =
            leerJson("niveles.json");

    private final JsonObject datosHistoria =
            leerJson("historia.json");

    private final JsonObject datosItems =
            leerJson("items.json");
    public static final int ANCHO_NIVEL = 4800;
    public static final int META_X = 4670;
    public enum Estado { INTRO, JUGANDO, ENTRE_CAPITULOS, VICTORIA, DERROTA }

    private static Juego instancia;
    public static synchronized Juego getInstancia() {
        if (instancia == null) instancia = new Juego();
        return instancia;
    }
    private final Inventario inventario = new Inventario(12);
    
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

    private static JsonObject leerJson(String archivo) {

        Path ruta = Paths.get("data", archivo);

        try (Reader lector = Files.newBufferedReader(ruta)) {

            JsonObject datos = JsonParser
                    .parseReader(lector)
                    .getAsJsonObject();

            System.out.println(
                    "JSON cargado: " + archivo
            );

            return datos;

        } catch (IOException | RuntimeException e) {

            throw new IllegalStateException(
                    "Error leyendo " + ruta.toAbsolutePath(),
                    e
            );
        }
    }



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


    private Enemigo crearEnemigo(int tipo) {

        JsonArray lista = datosEnemigos
                .getAsJsonArray("enemigos");

        if (lista == null) {
            throw new IllegalStateException(
                "Falta el arreglo enemigos en enemigos.json"
            );
        }

        for (JsonElement elemento : lista) {

            JsonObject datos = elemento.getAsJsonObject();

            if (datos.get("tipo").getAsInt() != tipo) {
                continue;
            }

            CreadorEnemigo creador;

            String clase = datos.get("clase").getAsString();

            switch (clase) {

                case "Enemigo":
                    creador = new CreadorEnemigo.CreadorNormal();
                    break;

                case "Boss":
                    creador = new CreadorEnemigo.CreadorJefe();
                    break;

                default:
                    throw new IllegalArgumentException(
                        "Clase desconocida: " + clase
                    );
            }

            return creador.crear(datos);
        }

        throw new IllegalArgumentException(
            "Enemigo no encontrado: " + tipo
        );
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

        caballero.reiniciarPosicion();

        JsonArray capitulos = datosNiveles
                .getAsJsonArray("capitulos");

        JsonObject nivel = null;

        for (JsonElement elemento : capitulos) {

            JsonObject actual = elemento.getAsJsonObject();

            if (actual.get("id").getAsInt() == capitulo) {
                nivel = actual;
                break;
            }
        }

        if (nivel == null) {
            throw new IllegalArgumentException(
                    "Capitulo no encontrado: " + capitulo
            );
        }

        JsonArray enemigosNivel =
                nivel.getAsJsonArray("enemigos");

        for (JsonElement elemento : enemigosNivel) {

            JsonObject datos = elemento.getAsJsonObject();

            int tipo = datos.get("tipo").getAsInt();
            int posicionX = datos.get("x").getAsInt();

            agregarEnemigo(tipo, posicionX);
        }

        enemigo = enemigos.isEmpty()
                ? null
                : enemigos.get(0);

        enemigoX = enemigo == null
                ? 0
                : posicionesEnemigos.get(enemigo);

        ultimoAtaqueCaballero = 0;
        ultimoAtaqueEnemigo = 0;
        golpesEnemigo = 0;

        System.out.println(
                "Capitulo " + capitulo
                + " cargado desde JSON. Enemigos: "
                + enemigos.size()
        );
    }
    private JsonObject obtenerHistoriaCapitulo() {

        JsonArray capitulos = datosHistoria
                .getAsJsonArray("capitulos");

        for (JsonElement elemento : capitulos) {

            JsonObject datos = elemento.getAsJsonObject();

            if (datos.get("id").getAsInt() == capitulo) {
                return datos;
            }
        }

        throw new IllegalArgumentException(
                "Historia no encontrada: " + capitulo
        );
    }

    public Inventario getInventario() {
        return inventario;
    }

    public boolean recogerItem(Item item) {
        return inventario.agregarItem(item);
    }

    public boolean usarItem(int indice) {

        if (estado != Estado.JUGANDO) {
            return false;
        }

        List<Item> objetos = inventario.obtenerItems();

        if (indice < 0 || indice >= objetos.size()) {
            return false;
        }

        Item item = objetos.get(indice);

        if (item instanceof Consumible
                && !((Consumible) item).disponible()) {
            return false;
        }

        item.usar(caballero);

        if (item instanceof Consumible
                && !((Consumible) item).disponible()) {
            inventario.eliminarItem(item);
        }

        return true;
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

    private JsonObject buscarItem(String id) {

        String[] categorias = {
            "herramientas",
            "consumibles"
        };

        for (String categoria : categorias) {

            JsonArray lista = datosItems.getAsJsonArray(categoria);

            if (lista == null) {
                continue;
            }

            for (JsonElement elemento : lista) {

                JsonObject item = elemento.getAsJsonObject();

                if (item.get("id").getAsString().equals(id)) {
                    return item;
                }
            }
        }

        throw new IllegalArgumentException(
            "Item no encontrado: " + id
        );
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
            if (distancia <= caballero.getAlcanceAtaque() && Math.abs(caballero.getY() - ENEMIGO_Y) < 60 && distancia < distanciaMin) {
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

        JsonArray capitulos = datosNiveles
                .getAsJsonArray("capitulos");

        for (JsonElement elemento : capitulos) {

            JsonObject nivel = elemento.getAsJsonObject();

            if (nivel.get("id").getAsInt() == capitulo) {
                return nivel.get("titulo").getAsString();
            }
        }

        return "";
    }

    public String getMensajeHistoria() {

        if (estado == Estado.VICTORIA) {
            return obtenerHistoriaCapitulo()
                    .get("final").getAsString();
        }

        if (estado == Estado.DERROTA) {
            return datosHistoria
                    .get("derrota").getAsString();
        }

        if (estado == Estado.ENTRE_CAPITULOS) {
            return "PUERTA ABIERTA - ENTER PARA CONTINUAR";
        }

        if (estado == Estado.INTRO) {
            return obtenerHistoriaCapitulo()
                    .get("inicio").getAsString();
        }

        return "";
    }
    public Pocima crearPocimaVida() {

        JsonObject datos = buscarItem("pocima_vida");

        return new Pocima(
            datos.get("nombre").getAsString(),
            datos.get("descripcion").getAsString(),
            datos.get("cantidad").getAsInt(),
            datos.get("curacion").getAsInt(),
            datos.get("efecto").getAsString()
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
