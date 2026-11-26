package juego;

import java.awt.*;
import java.util.List;

import items.Item;
import items.Inventario;
import items.herramientas.*;
import items.consumibles.*;

public final class RenderizadorInventario {

    private final int ANCHO;
    private final int ALTO;
    private final int SLOTS_HOTBAR;

    public RenderizadorInventario(
            int ancho,
            int alto,
            int slotsHotbar) {

        this.ANCHO = ancho;
        this.ALTO = alto;
        this.SLOTS_HOTBAR = slotsHotbar;
    }

    private void dibujarIconoItem(Graphics2D g, Item item, int x, int y) {
        if (item instanceof Espada) {
            dibujarIconoEspada(g, x, y);
        } else if (item instanceof Arco) {
            dibujarIconoArco(g, x, y);
        } else if (item instanceof Escudo) {
            dibujarIconoEscudo(g, x, y);
        } else if (item instanceof Armadura) {
            dibujarIconoArmadura(g, x, y);
        } else if (item instanceof Pocima) {
            dibujarIconoPocima(g, x, y);
        } else if (item instanceof Comida) {
            dibujarIconoComida(g, x, y);
        } else if (item instanceof Flecha) {
            dibujarIconoFlecha(g, x, y);
        } else if (item instanceof Habilidad) {
            dibujarIconoHabilidad(g, x, y);
        }
    }

    private void dibujarIconoEspada(Graphics2D g, int x, int y) {
        g.setColor(new Color(220, 225, 235));
        g.fillRect(x + 10, y + 4, 18, 4);

        g.fillPolygon(
            new int[]{x + 28, x + 34, x + 28},
            new int[]{y + 4, y + 6, y + 8},
            3
        );

        g.setColor(new Color(200, 170, 70));
        g.fillRect(x + 6, y + 3, 5, 6);

        g.setColor(new Color(120, 80, 45));
        g.fillRect(x + 2, y + 4, 5, 4);
    }

    private void dibujarIconoArco(Graphics2D g, int x, int y) {

        // Madera del arco
        g.setColor(new Color(150, 95, 43));
        g.setStroke(new BasicStroke(4));

        g.drawArc(x + 8, y + 2, 23, 35, -90, 180);

        // Cuerda
        g.setColor(new Color(225, 225, 210));
        g.setStroke(new BasicStroke(1));
        g.drawLine(x + 19, y + 2, x + 19, y + 37);

        // Flecha preparada
        g.setColor(new Color(185, 145, 85));
        g.drawLine(x + 4, y + 20, x + 34, y + 20);

        g.setColor(Color.LIGHT_GRAY);
        g.fillPolygon(
            new int[]{x + 34, x + 39, x + 34},
            new int[]{y + 17, y + 20, y + 23},
            3
        );
    }

    private void dibujarIconoEscudo(Graphics2D g, int x, int y) {

        // Silueta del escudo
        g.setColor(new Color(90, 115, 150));
        g.fillPolygon(
            new int[]{x + 5, x + 33, x + 30, x + 19, x + 8},
            new int[]{y + 5, y + 5, y + 26, y + 39, y + 26},
            5
        );

        // Borde dorado
        g.setColor(new Color(230, 190, 75));
        g.setStroke(new BasicStroke(2));
        g.drawPolygon(
            new int[]{x + 5, x + 33, x + 30, x + 19, x + 8},
            new int[]{y + 5, y + 5, y + 26, y + 39, y + 26},
            5
        );

        // Emblema
        g.fillRect(x + 17, y + 11, 4, 20);
        g.fillRect(x + 11, y + 17, 16, 4);

        g.setStroke(new BasicStroke(1));
    }

    private void dibujarIconoArmadura(Graphics2D g, int x, int y) {

        // Hombreras
        g.setColor(new Color(120, 140, 165));
        g.fillRoundRect(x + 2, y + 9, 13, 13, 4, 4);
        g.fillRoundRect(x + 25, y + 9, 13, 13, 4, 4);

        // Peto
        g.setColor(new Color(190, 205, 225));
        g.fillPolygon(
            new int[]{x + 11, x + 29, x + 32, x + 20, x + 8},
            new int[]{y + 7, y + 7, y + 29, y + 39, y + 29},
            5
        );

        // Detalles metálicos
        g.setColor(new Color(80, 105, 135));
        g.setStroke(new BasicStroke(2));
        g.drawLine(x + 20, y + 10, x + 20, y + 32);
        g.drawLine(x + 11, y + 21, x + 29, y + 21);

        g.setStroke(new BasicStroke(1));
    }

    private void dibujarIconoPocima(Graphics2D g, int x, int y) {
        g.setColor(new Color(180, 230, 255));
        g.fillOval(x + 8, y + 8, 16, 18);

        g.setColor(new Color(220, 50, 80));
        g.fillOval(x + 10, y + 14, 12, 10);

        g.setColor(new Color(180, 140, 80));
        g.fillRect(x + 12, y + 4, 8, 5);
    }

    private void dibujarIconoComida(Graphics2D g, int x, int y) {

        // Manzana
        g.setColor(new Color(210, 45, 55));
        g.fillOval(x + 6, y + 11, 28, 27);

        // Brillo
        g.setColor(new Color(250, 110, 100));
        g.fillOval(x + 11, y + 15, 9, 12);

        // Tallo
        g.setColor(new Color(105, 65, 35));
        g.fillRect(x + 19, y + 5, 3, 10);

        // Hoja
        g.setColor(new Color(65, 160, 75));
        g.fillOval(x + 21, y + 4, 12, 7);
    }

    private void dibujarIconoFlecha(Graphics2D g, int x, int y) {

        // Cuerpo de la flecha
        g.setColor(new Color(165, 115, 65));
        g.setStroke(new BasicStroke(3));

        g.drawLine(x + 5, y + 33, x + 32, y + 6);

        // Punta
        g.setColor(new Color(210, 220, 235));
        g.fillPolygon(
            new int[]{x + 28, x + 38, x + 35},
            new int[]{y + 5, y + 2, y + 12},
            3
        );

        // Plumas
        g.setColor(new Color(220, 65, 65));
        g.drawLine(x + 5, y + 33, x + 5, y + 23);
        g.drawLine(x + 5, y + 33, x + 15, y + 33);

        g.setStroke(new BasicStroke(1));
    }

    private void dibujarIconoHabilidad(Graphics2D g, int x, int y) {

        // Aura de habilidad
        g.setColor(new Color(110, 45, 180));
        g.fillOval(x + 4, y + 4, 32, 32);

        // Núcleo
        g.setColor(new Color(190, 110, 255));
        g.fillOval(x + 10, y + 10, 20, 20);

        // Rayo mágico
        g.setColor(new Color(250, 225, 140));
        g.fillPolygon(
            new int[]{
                x + 22, x + 14, x + 19,
                x + 13, x + 27, x + 22
            },
            new int[]{
                y + 7, y + 22, y + 22,
                y + 35, y + 18, y + 18
            },
            6
        );
    }

    public void dibujarHotbar(Graphics2D g,
                              Inventario inventario,
                              int slotSeleccionado) {

        int tamano = 52;
        int separacion = 5;

        int total = SLOTS_HOTBAR * (tamano + separacion)
                    - separacion;

        int inicioX = (ANCHO - total) / 2;
        int inicioY = ALTO - 65;

        List<Item> objetos =
            inventario.obtenerItems();

        g.setColor(new Color(8, 10, 18, 210));
        g.fillRoundRect(
            inicioX - 10, inicioY - 8,
            total + 20, tamano + 16, 12, 12
        );

        for (int i = 0; i < SLOTS_HOTBAR; i++) {

            int x = inicioX + i * (tamano + separacion);
            int y = inicioY;

            boolean seleccionado = i == slotSeleccionado;

            g.setColor(new Color(40, 44, 57));
            g.fillRect(x, y, tamano, tamano);

            g.setColor(new Color(70, 75, 90));
            g.fillRect(x + 2, y + 2, tamano - 4, 2);

            g.setStroke(new BasicStroke(
                seleccionado ? 3 : 1
            ));

            g.setColor(seleccionado
                ? new Color(255, 210, 90)
                : new Color(115, 120, 135));

            g.drawRect(x, y, tamano, tamano);

            g.setStroke(new BasicStroke(1));

            if (i < objetos.size()) {

                Item item = objetos.get(i);

                Graphics2D icono =
                    (Graphics2D) g.create();

                try {

                    icono.clipRect(
                        x + 4, y + 10,
                        tamano - 8, tamano - 19
                    );

                    icono.translate(x + 11, y + 10);
                    icono.scale(0.75, 0.75);

                    dibujarIconoItem(
                        icono, item, 0, 0
                    );

                } finally {
                    icono.dispose();
                }

                if (item instanceof Herramienta) {

                    Herramienta h =
                        (Herramienta) item;

                    double porcentaje = Math.max(
                        0, Math.min(1,
                            (double) h.getDurabilidad()
                            / Math.max(1,
                                h.getDurabilidadMaxima())
                        )
                    );

                    g.setColor(Color.BLACK);
                    g.fillRect(x + 6, y + 43, 40, 5);

                    g.setColor(Color.getHSBColor(
                        (float) (porcentaje / 3.0),
                        0.85f, 0.95f
                    ));

                    g.fillRect(
                        x + 7, y + 44,
                        (int) (38 * porcentaje), 3
                    );

                } else {

                    int cantidad = 1;

                    if (item instanceof Flecha) {
                        cantidad =
                            ((Flecha) item).getCantidad();
                    }

                    if (item instanceof Consumible) {
                        cantidad =
                            ((Consumible) item).getCantidad();
                    }

                    String texto =
                        String.valueOf(cantidad);

                    g.setFont(new Font(
                        "SansSerif", Font.BOLD, 12
                    ));

                    int tx = x + tamano - 5
                        - g.getFontMetrics()
                            .stringWidth(texto);

                    g.setColor(Color.BLACK);
                    g.drawString(texto, tx + 1, y + 48);

                    g.setColor(Color.WHITE);
                    g.drawString(texto, tx, y + 47);
                }
            }

            g.setFont(new Font(
                "SansSerif", Font.BOLD, 10
            ));

            g.setColor(Color.LIGHT_GRAY);
            g.drawString(
                String.valueOf(i + 1), x + 4, y + 11
            );
        }

        if (slotSeleccionado < objetos.size()) {

            g.setColor(new Color(255, 230, 165));
            g.setFont(new Font(
                "SansSerif", Font.BOLD, 15
            ));

            centrar(g,
                objetos.get(slotSeleccionado).getNombre(),
                inicioY - 16
            );
        }
    }

    public void dibujarInventario(Graphics2D g,
                                  Inventario inventario,
                                  int slotSeleccionado) {

        g.setColor(new Color(8, 10, 18, 220));
        g.fillRect(0, 0, ANCHO, ALTO);

        g.setColor(new Color(30, 35, 50));
        g.fillRoundRect(320, 120, 640, 480, 18, 18);

        g.setColor(new Color(240, 200, 110));
        g.drawRoundRect(320, 120, 640, 480, 18, 18);

        g.setFont(new Font("SansSerif", Font.BOLD, 30));
        centrar(g, "INVENTARIO", 175);

        List<Item> objetos =
            inventario.obtenerItems();

        g.setFont(new Font("SansSerif", Font.PLAIN, 15));

        centrar(g,
            objetos.size() + " / "
            + inventario.getCapacidadMaxima(),
            204
        );

        for (int i = 0; i < 12; i++) {

            int x = 462 + (i % 4) * 90;
            int y = 230 + (i / 4) * 90;

            g.setColor(new Color(48, 53, 70));
            g.fillRect(x, y, 80, 80);

            g.setColor(i == slotSeleccionado
                ? new Color(255, 210, 90)
                : new Color(110, 115, 130));

            g.setStroke(new BasicStroke(
                i == slotSeleccionado ? 3 : 1
            ));

            g.drawRect(x, y, 80, 80);
            g.setStroke(new BasicStroke(1));

            if (i < objetos.size()) {

                Item item = objetos.get(i);

                Graphics2D icono = (Graphics2D) g.create();

                try {
                    icono.clipRect(x + 8, y + 8, 64, 64);
                    icono.translate(x + 15, y + 10);
                    icono.scale(1.25, 1.25);

                    dibujarIconoItem(icono, item, 0, 0);

                } finally {
                    icono.dispose();
                }

                if (item instanceof Herramienta) {

                    Herramienta h = (Herramienta) item;

                    double porcentaje =
                        (double) h.getDurabilidad()
                        / Math.max(1, h.getDurabilidadMaxima());

                    porcentaje = Math.max(
                        0, Math.min(1, porcentaje)
                    );

                    g.setColor(Color.BLACK);
                    g.fillRect(x + 8, y + 68, 64, 6);

                    g.setColor(Color.getHSBColor(
                        (float) (porcentaje / 3),
                        0.85f, 0.95f
                    ));

                    g.fillRect(
                        x + 9, y + 69,
                        (int) (62 * porcentaje), 4
                    );
                }
            }
        }

        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.setColor(new Color(255, 220, 145));

        if (slotSeleccionado < objetos.size()) {

            centrar(g,
                objetos.get(slotSeleccionado).getNombre(),
                520
            );
        }

        g.setFont(new Font("SansSerif", Font.PLAIN, 14));

        centrar(g,
            "Izquierdo: seleccionar | Derecho: usar",
            553
        );

        centrar(g,
            "X: eliminar | I: cerrar",
            576
        );
    }

    private void centrar(Graphics2D g,
                         String texto, int y) {

        int x = (ANCHO -
            g.getFontMetrics().stringWidth(texto)) / 2;

        g.drawString(texto, x, y);
    }
}