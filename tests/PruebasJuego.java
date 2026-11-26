import entidades.*;
import escenario.*;
import interfaces.*;
import items.*;
import items.herramientas.*;
import juego.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class PruebasJuego {

    private static int aprobadas = 0;
    private static int fallidas = 0;

    public static void main(String[] args) {

        System.out.println(
            "========================================"
        );

        System.out.println(
            " PRUEBAS - CABALLERO MEDIEVAL 2D"
        );

        System.out.println(
            "========================================"
        );

        probar("P01 - Singleton", () -> {

            Juego a = Juego.getInstancia();
            Juego b = Juego.getInstancia();

            verificar(a == b,
                "Existen instancias diferentes.");
        });

        probar("P02 - Composicion", () -> {

            Caballero a = crearCaballero();
            Caballero b = crearCaballero();

            verificar(a.getInventario() != null,
                "Inventario inexistente.");

            verificar(
                a.getInventario() != b.getInventario(),
                "Los caballeros comparten inventario."
            );
        });

        probar("P03 - Capacidad inventario", () -> {

            Inventario inventario = new Inventario(1);

            verificar(
                inventario.agregarItem(crearEspada()),
                "No se agrego el objeto."
            );

            verificar(
                !inventario.agregarItem(crearEspada()),
                "Se supero la capacidad."
            );

            verificar(inventario.estaLleno(),
                "No reconoce el inventario lleno.");
        });

        probar("P04 - Encapsulamiento", () -> {

            Inventario inventario = new Inventario(2);

            inventario.agregarItem(crearEspada());

            List<Item> copia =
                inventario.obtenerItems();

            copia.clear();

            verificar(
                inventario.cantidadItems() == 1,
                "La lista externa modifico el inventario."
            );
        });

        probar("P05 - Ataque basico", () -> {

            Caballero c = crearCaballero();
            Enemigo e = crearEnemigo();

            c.atacar(e);

            verificar(e.getVida() == 95,
                "Danio basico incorrecto.");
        });

        probar("P06 - Strategy espada", () -> {

            Caballero c = crearCaballero();
            Enemigo e = crearEnemigo();

            c.equiparArma(crearEspada());

            c.atacar(e);

            verificar(e.getVida() == 80,
                "Danio de espada incorrecto.");
        });

        probar("P07 - Furia", () -> {

            Caballero c = crearCaballero();
            Enemigo e = crearEnemigo();

            c.equiparArma(crearEspada());
            c.activarFuria(10, 1);

            c.atacar(e);

            verificar(e.getVida() == 70,
                "No se aplico la bonificacion.");
        });

        probar("P08 - Duracion Furia", () -> {

            Caballero c = crearCaballero();

            c.activarFuria(10, 1);

            verificar(c.tieneFuriaActiva(),
                "Furia no activada.");

            for (int i = 0; i < 63; i++) {
                c.actualizarHabilidad();
            }

            verificar(!c.tieneFuriaActiva(),
                "Furia no finalizo.");

            verificar(c.getBonoAtaque() == 0,
                "La bonificacion permanece activa.");
        });

        probar("P09 - Observer", () -> {

            Caballero c = crearCaballero();

            AtomicInteger eventos =
                new AtomicInteger();

            c.agregarObservador(evento -> {

                if ("vida".equals(
                        evento.getPropertyName())) {

                    eventos.incrementAndGet();
                }
            });

            c.recibirDanio(10);
            c.curar(5);

            verificar(eventos.get() == 2,
                "No se notificaron ambos cambios.");
        });

        probar("P10 - Economia", () -> {

            Caballero c = crearCaballero();

            c.agregarMonedas(10);

            verificar(c.gastarMonedas(5),
                "Gasto valido rechazado.");

            verificar(c.getMonedas() == 5,
                "Saldo incorrecto.");
        });

        probar("P11 - Saldo insuficiente", () -> {

            Caballero c = crearCaballero();

            c.agregarMonedas(2);

            verificar(!c.gastarMonedas(5),
                "Permitio un gasto sin saldo.");

            verificar(c.getMonedas() == 2,
                "Se modifico el saldo.");

            verificar(!c.gastarMonedas(-1),
                "Permitio gasto negativo.");
        });

        probar("P12 - Moneda unica", () -> {

            Moneda moneda =
                new Moneda(100, 550, 1);

            int primera =
                moneda.recoger(100, 550, 45, 70);

            int segunda =
                moneda.recoger(100, 550, 45, 70);

            verificar(primera == 1,
                "No recogio la moneda.");

            verificar(segunda == 0,
                "Se recogio dos veces.");
        });

        probar("P13 - Cofre unico", () -> {

            Inventario inventario =
                new Inventario(12);

            Cofre cofre =
                new Cofre(100, 580, crearEspada());

            verificar(cofre.abrir(inventario),
                "No se abrio el cofre.");

            verificar(cofre.estaAbierto(),
                "Estado incorrecto.");

            verificar(!cofre.abrir(inventario),
                "El cofre se abrio dos veces.");

            verificar(
                inventario.cantidadItems() == 1,
                "Se duplicaron objetos."
            );
        });

        probar("P14 - Cofre inventario lleno", () -> {

            Inventario inventario =
                new Inventario(1);

            Espada espada = crearEspada();

            inventario.agregarItem(espada);

            Cofre cofre =
                new Cofre(100, 580, crearEspada());

            verificar(!cofre.abrir(inventario),
                "Abrio con inventario lleno.");

            verificar(!cofre.estaAbierto(),
                "Se perdio el contenido.");

            inventario.eliminarItem(espada);

            verificar(cofre.abrir(inventario),
                "No permitio abrirlo posteriormente.");
        });

        probar("P15 - Curacion limitada", () -> {

            Caballero c = crearCaballero();

            c.recibirDanio(10);
            c.curar(500);

            verificar(
                c.getVida() == c.getVidaMaxima(),
                "La vida excede el maximo."
            );
        });

        probar("P16 - Strategy arco", () -> {

            Caballero c = crearCaballero();

            Arco arco = crearArco();

            Flecha flecha = new Flecha(
                "Flecha", "Prueba", 8, 12, 3
            );

            AtomicInteger disparos =
                new AtomicInteger();

            c.configurarLanzador(
                (x, y, direccion, alcance,
                    municion, bono) -> {

                    disparos.incrementAndGet();
                }
            );

            c.equiparArma(arco);

            c.getInventario().agregarItem(flecha);

            int durabilidad =
                arco.getDurabilidad();

            verificar(c.ejecutarAtaque(null),
                "No se ejecuto el disparo.");

            verificar(disparos.get() == 1,
                "No se lanzo el proyectil.");

            verificar(flecha.getCantidad() == 2,
                "No se consumio municion.");

            verificar(
                arco.getDurabilidad() < durabilidad,
                "No se redujo la durabilidad."
            );
        });

        probar("P17 - Arco sin municion", () -> {

            Caballero c = crearCaballero();

            AtomicInteger disparos =
                new AtomicInteger();

            c.configurarLanzador(
                (x, y, direccion, alcance,
                    municion, bono) -> {

                    disparos.incrementAndGet();
                }
            );

            Arco arco = crearArco();

            c.equiparArma(arco);

            int durabilidad = arco.getDurabilidad();

            verificar(!c.ejecutarAtaque(null),
                "Disparo sin municion.");

            verificar(disparos.get() == 0,
                "Se genero un proyectil.");

            verificar(
                arco.getDurabilidad() == durabilidad,
                "Se desgasto sin disparar."
            );
        });

        probar("P18 - Desequipar arma", () -> {

            Caballero c = crearCaballero();
            Espada espada = crearEspada();

            c.getInventario().agregarItem(espada);
            c.equiparArma(espada);

            verificar(c.eliminarObjeto(espada),
                "No elimino el objeto.");

            verificar(c.getArmaEquipada() == null,
                "El arma continua equipada.");

            Enemigo e = crearEnemigo();

            c.atacar(e);

            verificar(e.getVida() == 95,
                "No regreso al ataque basico.");
        });

        probar("P19 - Limites escenario", () -> {

            Caballero c = crearCaballero();

            c.configurarLimiteMovimiento(500);

            c.establecerDireccion(1);

            for (int i = 0; i < 200; i++) {
                c.update();
            }

            verificar(
                c.getX() == 500 - Caballero.ANCHO,
                "Supero el limite derecho."
            );

            c.establecerDireccion(-1);

            for (int i = 0; i < 200; i++) {
                c.update();
            }

            verificar(c.getX() == 0,
                "Supero el limite izquierdo.");
        });

        probar("P20 - Impacto proyectil", () -> {

            GestorProyectiles gestor =
                new GestorProyectiles();

            Enemigo enemigo = crearEnemigo();

            enemigo.establecerPosicion(200);

            Flecha flecha = new Flecha(
                "Flecha", "Prueba", 8, 12, 1
            );

            gestor.disparar(
                100, 585, 1, 280,
                flecha, 20
            );

            verificar(enemigo.getVida() == 100,
                "Danio aplicado antes del impacto.");

            for (int i = 0; i < 30; i++) {

                gestor.actualizar(
                    Collections.singletonList(enemigo),
                    550
                );
            }

            verificar(enemigo.getVida() == 72,
                "Danio del proyectil incorrecto.");

            verificar(
                gestor.obtenerVistaProyectiles().isEmpty(),
                "El proyectil no desaparecio."
            );
        });

        System.out.println(
            "\n========================================"
        );

        System.out.println(" RESULTADOS FINALES");

        System.out.println(
            "========================================"
        );

        System.out.println(
            "Aprobadas: " + aprobadas
        );

        System.out.println(
            "Fallidas: " + fallidas
        );

        System.out.println(
            "Total: " + (aprobadas + fallidas)
        );

        if (fallidas > 0) {
            System.exit(1);
        }
    }

    private static Caballero crearCaballero() {

        return new Caballero(
            "Caballero de prueba",
            100, 15, 0, null, null
        );
    }

    private static Enemigo crearEnemigo() {

        return new Enemigo(
            "Enemigo de prueba",
            100, 10, 5, 2
        );
    }

    private static Espada crearEspada() {

        return new Espada(
            "Espada de prueba",
            "Prueba",
            100, 20, 90
        );
    }

    private static Arco crearArco() {

        return new Arco(
            "Arco de prueba",
            "Prueba",
            100, 10, 280
        );
    }

    private static void verificar(
            boolean condicion, String mensaje) {

        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    private static void probar(
            String nombre, Runnable prueba) {

        try {

            prueba.run();

            aprobadas++;

            System.out.println(
                "[OK] " + nombre
            );

        } catch (AssertionError | Exception error) {

            fallidas++;

            System.out.println(
                "[ERROR] " + nombre
                + ": " + error.getMessage()
            );
        }
    }
}