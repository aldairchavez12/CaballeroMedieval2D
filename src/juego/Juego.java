
package juego;

import entidades.Caballero;
import entidades.Enemigo;
import entidades.Boss;
import entidades.Entidad;
import escenario.Estructura;

import java.util.ArrayList;
import java.util.List;

public class Juego {

    public enum Estado {
        INTRO,
        JUGANDO,
        ENTRE_CAPITULOS,
        VICTORIA,
        DERROTA
    }

    private final List<Entidad> entidades;
    private final List<Estructura> estructuras;

    private final Caballero caballero;
    private Enemigo enemigo;

    private Estado estado;
    private int capitulo;

    // Posicion del enemigo en el escenario
    private int enemigoX;
    private static final int ENEMIGO_Y = 550;

    private static final int ANCHO_ENEMIGO = 45;
    private static final int VELOCIDAD_ENEMIGO = 2;

    private static final int DISTANCIA_DETECCION = 340;
    private static final int DISTANCIA_ATAQUE_ENEMIGO = 65;
    private static final int ALCANCE_CABALLERO = 90;

    // Intervalos entre ataques
    private static final long COOLDOWN_CABALLERO =
            350_000_000L;

    private static final long COOLDOWN_ENEMIGO =
            1_200_000_000L;

    private long ultimoAtaqueCaballero;
    private long ultimoAtaqueEnemigo;

    private int golpesEnemigo;

    public Juego() {

        entidades = new ArrayList<>();
        estructuras = new ArrayList<>();

        caballero = new Caballero(
                "Caballero de Valdoria",
                100,
                15,
                0,
                null,
                null
        );

        entidades.add(caballero);

        capitulo = 1;
        estado = Estado.INTRO;

        prepararCapitulo();
    }

    public void iniciar() {
        estado = Estado.INTRO;
        System.out.println("La Fortaleza Oscura");
    }

    private void prepararCapitulo() {

        entidades.removeIf(e -> e instanceof Enemigo);

        switch (capitulo) {

            case 1:
                enemigo = new Enemigo(
                        "Goblin",
                        45,
                        7,
                        10,
                        2.0
                );
                break;

            case 2:
                enemigo = new Enemigo(
                        "Guardia Oscuro",
                        75,
                        12,
                        25,
                        2.5
                );
                break;

            case 3:
                enemigo = new Boss(
                        "Senor de las Sombras",
                        120,
                        16,
                        100,
                        2.0,
                        14,
                        1
                );
                break;

            default:
                enemigo = null;
                return;
        }

        enemigoX = 850;
        ultimoAtaqueCaballero = 0;
        ultimoAtaqueEnemigo = 0;
        golpesEnemigo = 0;

        entidades.add(enemigo);
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

        if (estado != Estado.JUGANDO) {
            return;
        }

        // El Caballero actualiza movimiento y gravedad
        if (caballero.estaVivo()) {
            caballero.update();
        }

        if (enemigo != null && enemigo.estaVivo()) {
            actualizarIAEnemigo();
        }

        if (!caballero.estaVivo()) {
            estado = Estado.DERROTA;
            System.out.println("El Caballero ha sido derrotado.");
        }
    }

    private void actualizarIAEnemigo() {

        int centroCaballero =
                caballero.getX() + Caballero.ANCHO / 2;

        int centroEnemigo =
                enemigoX + ANCHO_ENEMIGO / 2;

        int diferencia = centroCaballero - centroEnemigo;

        int distancia = Math.abs(diferencia);

        // El enemigo persigue al jugador cuando lo detecta.
        if (distancia <= DISTANCIA_DETECCION
                && distancia > DISTANCIA_ATAQUE_ENEMIGO) {

            int direccion = Integer.compare(diferencia, 0);

            enemigoX += direccion * VELOCIDAD_ENEMIGO;

            enemigoX = Math.max(
                    0,
                    Math.min(1280 - ANCHO_ENEMIGO, enemigoX)
            );
        }

        // Comprobar si el jugador está a una altura atacable.
        boolean alturaCompatible =
                Math.abs(caballero.getY() - ENEMIGO_Y) < 55;

        if (distancia <= DISTANCIA_ATAQUE_ENEMIGO
                && alturaCompatible
                && caballero.estaVivo()) {

            long ahora = System.nanoTime();

            if (ahora - ultimoAtaqueEnemigo >= COOLDOWN_ENEMIGO) {

                golpesEnemigo++;

                // El Boss usa un ataque especial cada 3 golpes
                if (enemigo instanceof Boss && golpesEnemigo % 3 == 0) {

                    ((Boss) enemigo).ataqueEspecial(caballero);

                } else {

                    enemigo.atacar(caballero);
                }

                ultimoAtaqueEnemigo = ahora;

                System.out.println(
                        "Vida del Caballero: " + caballero.getVida()
                );
            }
        }
    }

    public void atacarEnemigo() {

        if (estado != Estado.JUGANDO) {
            return;
        }

        if (!caballero.estaVivo()
                || enemigo == null
                || !enemigo.estaVivo()) {
            return;
        }

        long ahora = System.nanoTime();

        if (ahora - ultimoAtaqueCaballero < COOLDOWN_CABALLERO) {
            return;
        }

        int centroCaballero =
                caballero.getX() + Caballero.ANCHO / 2;

        int centroEnemigo =
                enemigoX + ANCHO_ENEMIGO / 2;

        int distancia =
                Math.abs(centroCaballero - centroEnemigo);

        boolean alturaCompatible =
                Math.abs(caballero.getY() - ENEMIGO_Y) < 60;

        if (distancia <= ALCANCE_CABALLERO
                && alturaCompatible) {

            caballero.atacar(enemigo);

            ultimoAtaqueCaballero = ahora;

            System.out.println(
                    enemigo.getNombre()
                    + " - Vida: "
                    + enemigo.getVida()
            );

            if (!enemigo.estaVivo()) {

                if (capitulo == 3) {

                    estado = Estado.VICTORIA;

                    System.out.println(
                            "Victoria: Valdoria ha sido liberada."
                    );

                } else {

                    estado = Estado.ENTRE_CAPITULOS;

                    System.out.println(
                            "Capitulo completado: " + capitulo
                    );
                }
            }
        }
    }

    public String getTituloCapitulo() {

        switch (capitulo) {

            case 1:
                return "CAPITULO I - BOSQUE DE LAS SOMBRAS";

            case 2:
                return "CAPITULO II - FORTALEZA ABANDONADA";

            case 3:
                return "CAPITULO III - TRONO DE LA OSCURIDAD";

            default:
                return "";
        }
    }

    public String getMensajeHistoria() {

        if (estado == Estado.VICTORIA) {
            return "VICTORIA: EL REINO DE VALDORIA ES LIBRE";
        }

        if (estado == Estado.DERROTA) {
            return "DERROTA: EL CABALLERO HA CAIDO";
        }

        if (estado == Estado.ENTRE_CAPITULOS) {
            return "ENEMIGO DERROTADO - ENTER PARA CONTINUAR";
        }

        if (estado == Estado.INTRO) {

            switch (capitulo) {

                case 1:
                    return "Derrota al Goblin. ENTER para comenzar.";

                case 2:
                    return "La fortaleza te espera. ENTER para comenzar.";

                case 3:
                    return "Enfrenta al Senor Oscuro. ENTER para comenzar.";

                default:
                    return "";
            }
        }

        return "";
    }

    public void finalizar() {
        estado = Estado.DERROTA;
    }

    public boolean estaActivo() {
        return estado == Estado.JUGANDO;
    }

    public Estado getEstado() {
        return estado;
    }

    public int getCapitulo() {
        return capitulo;
    }

    public Caballero getCaballero() {
        return caballero;
    }

    public Enemigo getEnemigo() {
        return enemigo;
    }

    public int getEnemigoX() {
        return enemigoX;
    }

    public int getEnemigoY() {
        return ENEMIGO_Y;
    }
}
