
package juego;

import entidades.Caballero;
import entidades.Enemigo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.GradientPaint;
import java.util.Random;

public class PanelJuego extends JPanel {

    private static final int ANCHO = 1280;
    private static final int ALTO = 720;
    private static final int SUELO_Y = 620;

    private final Juego juego;
    private final Timer gameLoop;

    private boolean izquierda;
    private boolean derecha;

    private int monedas = 0;
    private final boolean[] monedasRecogidas = new boolean[3];

    private final int[][] posicionesMonedas = {
        {280, 565},
        {760, 565},
        {1020, 565}
    };

    public PanelJuego() {

        setPreferredSize(new Dimension(ANCHO, ALTO));
        setFocusable(true);
        setBackground(Color.BLACK);
        setDoubleBuffered(true);

        juego = new Juego();

        configurarControles();

        gameLoop = new Timer(16, e -> {
            actualizarControles();
            juego.actualizar();
            actualizarMonedas();
            repaint();
        });

        juego.iniciar();
        gameLoop.start();
    }

    // ============================================
    // CONTROLES
    // ============================================

    private void configurarControles() {

        InputMap input = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap acciones = getActionMap();

        input.put(KeyStroke.getKeyStroke("pressed A"), "izqOn");
        input.put(KeyStroke.getKeyStroke("released A"), "izqOff");

        input.put(KeyStroke.getKeyStroke("pressed D"), "derOn");
        input.put(KeyStroke.getKeyStroke("released D"), "derOff");

        input.put(KeyStroke.getKeyStroke("pressed SPACE"), "saltar");
        input.put(KeyStroke.getKeyStroke("pressed J"), "atacar");

        input.put(
            KeyStroke.getKeyStroke("pressed ENTER"),
            "continuarHistoria"
            );

        acciones.put("continuarHistoria", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                juego.continuarHistoria();
            }
        });
        acciones.put("izqOn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                izquierda = true;
            }
        });

        acciones.put("izqOff", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                izquierda = false;
            }
        });

        acciones.put("derOn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                derecha = true;
            }
        });

        acciones.put("derOff", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                derecha = false;
            }
        });

        acciones.put("saltar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                juego.getCaballero().saltar();
            }
        });

        acciones.put("atacar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                juego.atacarEnemigo();
            }
        });
    }

    private void actualizarControles() {

        int direccion = 0;

        if (izquierda) direccion--;
        if (derecha) direccion++;

        juego.getCaballero().establecerDireccion(direccion);
    }

    private void actualizarMonedas() {

        Caballero caballero = juego.getCaballero();

        for (int i = 0; i < posicionesMonedas.length; i++) {

            if (monedasRecogidas[i]) continue;

            int monedaX = posicionesMonedas[i][0];
            int monedaY = posicionesMonedas[i][1];

            Rectangle personaje = new Rectangle(
                caballero.getX(),
                caballero.getY(),
                Caballero.ANCHO,
                Caballero.ALTO
            );

            Rectangle moneda = new Rectangle(
                monedaX,
                monedaY,
                22,
                28
            );

            if (personaje.intersects(moneda)) {
                monedasRecogidas[i] = true;
                monedas++;
            }
        }
    }

    // ============================================
    // RENDERIZADO PRINCIPAL
    // ============================================

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_OFF
        );

        dibujarCielo(g2);
        dibujarMontanas(g2);
        dibujarCastillo(g2);
        dibujarRuinas(g2);

        dibujarPlataforma(g2, 0, SUELO_Y, ANCHO, 100);
        dibujarPlataforma(g2, 560, 420, 260, 36);
        dibujarPlataforma(g2, 960, 340, 250, 36);

        dibujarEscalera(g2, 1070, 375, 245);

        dibujarCofre(g2, 1110, 286);

        dibujarMonedas(g2);

        dibujarCaballero(g2);
        dibujarEnemigo(g2);

        dibujarHUD(g2);
        dibujarHistoria(g2);
        g2.dispose();
    }

    // ============================================
    // FONDO MEDIEVAL
    // ============================================

    private void dibujarCielo(Graphics2D g) {

        GradientPaint cielo = new GradientPaint(
            0, 0, new Color(66, 70, 133),
            0, 570, new Color(252, 149, 109)
        );

        g.setPaint(cielo);
        g.fillRect(0, 0, ANCHO, ALTO);

        // Sol
        g.setColor(new Color(255, 214, 123));
        g.fillOval(620, 190, 150, 150);

        g.setColor(new Color(255, 236, 164));
        g.fillOval(643, 213, 104, 104);

        // Nubes retro
        Random r = new Random(31);

        for (int i = 0; i < 28; i++) {

            int x = r.nextInt(ANCHO);
            int y = 45 + r.nextInt(250);

            g.setColor(new Color(245, 151, 150, 155));

            g.fillRect(x, y, 75, 12);
            g.fillRect(x + 18, y - 10, 55, 10);
            g.fillRect(x + 42, y + 10, 65, 8);
        }
    }

    private void dibujarMontanas(Graphics2D g) {

        g.setColor(new Color(107, 103, 153));

        int[] x1 = {0, 150, 290, 450, 640, 800, 1000, 1280};
        int[] y1 = {440, 260, 440, 300, 455, 275, 400, 330};

        Polygon montanas = new Polygon();

        for (int i = 0; i < x1.length; i++) {
            montanas.addPoint(x1[i], y1[i]);
        }

        montanas.addPoint(1280, 620);
        montanas.addPoint(0, 620);

        g.fillPolygon(montanas);

        // Segunda capa
        g.setColor(new Color(65, 80, 124));

        Polygon capa = new Polygon();

        capa.addPoint(0, 500);
        capa.addPoint(140, 365);
        capa.addPoint(300, 485);
        capa.addPoint(480, 350);
        capa.addPoint(650, 495);
        capa.addPoint(880, 370);
        capa.addPoint(1040, 470);
        capa.addPoint(1190, 380);
        capa.addPoint(1280, 450);
        capa.addPoint(1280, 620);
        capa.addPoint(0, 620);

        g.fillPolygon(capa);

        // Bosque lejano
        Random r = new Random(7);

        for (int x = 0; x < ANCHO; x += 24) {

            int altura = 35 + r.nextInt(85);

            g.setColor(new Color(35, 64, 93));

            int[] xs = {x, x + 12, x + 24};
            int[] ys = {540, 540 - altura, 540};

            g.fillPolygon(xs, ys, 3);
        }
    }

    private void dibujarCastillo(Graphics2D g) {

        int x = 940;
        int y = 260;

        Color piedra = new Color(48, 56, 100);
        Color sombra = new Color(35, 43, 81);

        g.setColor(piedra);
        g.fillRect(x, y, 260, 210);

        // Torres
        int[] torresX = {950, 1020, 1090, 1160};

        for (int tx : torresX) {

            g.setColor(sombra);
            g.fillRect(tx, y - 85, 42, 185);

            g.setColor(piedra);

            int[] px = {tx - 8, tx + 21, tx + 50};
            int[] py = {y - 85, y - 140, y - 85};

            g.fillPolygon(px, py, 3);

            // Ventana
            g.setColor(new Color(255, 191, 104));
            g.fillRect(tx + 17, y - 42, 8, 20);
        }
    }
    private void dibujarHistoria(Graphics2D g) {

    // Mostrar nombre del capítulo
        g.setColor(new Color(20, 20, 38, 210));
        g.fillRoundRect(360, 20, 560, 48, 12, 12);

        g.setColor(new Color(255, 219, 112));
        g.setFont(new Font("Monospaced", Font.BOLD, 19));

        String titulo = juego.getTituloCapitulo();

        FontMetrics fm = g.getFontMetrics();

        g.drawString(
            titulo,
            (1280 - fm.stringWidth(titulo)) / 2,
            51
        );

        String mensaje = juego.getMensajeHistoria();

        if (mensaje.isEmpty()) {
            return;
        }

    // Panel narrativo inferior
        g.setColor(new Color(17, 19, 35, 235));
        g.fillRoundRect(170, 490, 940, 105, 20, 20);

        g.setColor(new Color(221, 176, 87));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(170, 490, 940, 105, 20, 20);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 19));

        FontMetrics texto = g.getFontMetrics();

        g.drawString(
            mensaje,
            (1280 - texto.stringWidth(mensaje)) / 2,
            548
        );
    }

    private void dibujarRuinas(Graphics2D g) {

        g.setColor(new Color(55, 57, 79));

        g.fillRect(0, 300, 105, 320);
        g.fillRect(100, 400, 50, 220);
        g.fillRect(0, 265, 150, 35);

        // Piedras visibles
        g.setColor(new Color(97, 101, 113));

        for (int y = 310; y < 610; y += 25) {
            for (int x = 0; x < 145; x += 42) {
                g.drawRect(x, y, 40, 22);
            }
        }

        // Enredaderas
        g.setColor(new Color(44, 124, 65));

        for (int y = 295; y < 475; y += 16) {
            g.fillRect(85, y, 9, 13);
            g.fillRect(76, y + 8, 13, 7);
        }
    }

    // ============================================
    // TERRENO Y OBJETOS
    // ============================================

    private void dibujarPlataforma(
        Graphics2D g,
        int x,
        int y,
        int ancho,
        int alto
    ) {

        // Base de piedra
        g.setColor(new Color(44, 46, 62));
        g.fillRect(x, y, ancho, alto);

        // Bloques
        for (int fila = 0; fila < alto; fila += 24) {
            for (int col = 0; col < ancho; col += 40) {

                g.setColor(new Color(88, 91, 110));

                g.drawRect(
                    x + col,
                    y + fila,
                    Math.min(38, ancho - col),
                    Math.min(22, alto - fila)
                );
            }
        }

        // Césped
        g.setColor(new Color(55, 135, 54));
        g.fillRect(x, y - 9, ancho, 12);

        g.setColor(new Color(125, 201, 67));
        g.fillRect(x, y - 12, ancho, 5);

        // Puntas del césped
        for (int i = x; i < x + ancho; i += 13) {
            g.fillRect(i, y - 18, 4, 8);
        }
    }

    private void dibujarEscalera(
        Graphics2D g,
        int x,
        int y,
        int alto
    ) {

        g.setColor(new Color(104, 60, 34));

        g.fillRect(x, y, 9, alto);
        g.fillRect(x + 38, y, 9, alto);

        g.setColor(new Color(185, 118, 62));

        for (int i = y; i < y + alto; i += 25) {
            g.fillRect(x, i, 47, 7);
        }
    }

    private void dibujarCofre(Graphics2D g, int x, int y) {

        g.setColor(new Color(73, 37, 27));
        g.fillRect(x, y, 75, 54);

        g.setColor(new Color(174, 82, 35));
        g.fillRect(x + 5, y + 8, 65, 42);

        g.setColor(new Color(245, 184, 48));
        g.fillRect(x, y + 7, 75, 7);
        g.fillRect(x, y + 43, 75, 7);

        g.fillRect(x + 31, y + 10, 12, 30);

        g.setColor(Color.BLACK);
        g.fillRect(x + 35, y + 22, 5, 8);
    }

    private void dibujarMonedas(Graphics2D g) {

        for (int i = 0; i < posicionesMonedas.length; i++) {

            if (monedasRecogidas[i]) continue;

            int x = posicionesMonedas[i][0];
            int y = posicionesMonedas[i][1];

            g.setColor(new Color(119, 65, 18));
            g.fillOval(x - 2, y - 2, 26, 32);

            g.setColor(new Color(255, 189, 24));
            g.fillOval(x, y, 22, 28);

            g.setColor(new Color(255, 239, 112));
            g.fillOval(x + 5, y + 5, 11, 18);
        }
    }

    // ============================================
    // PERSONAJES
    // ============================================

    private void dibujarCaballero(Graphics2D g) {

        Caballero c = juego.getCaballero();

        if (!c.estaVivo()) return;

        int x = c.getX();
        int y = c.getY();

        // Capa
        g.setColor(new Color(155, 28, 45));
        g.fillPolygon(
            new int[]{x + 12, x - 18, x + 18},
            new int[]{y + 20, y + 63, y + 55},
            3
        );

        // Piernas
        g.setColor(new Color(90, 104, 124));
        g.fillRect(x + 8, y + 47, 13, 23);
        g.fillRect(x + 26, y + 47, 13, 23);

        // Armadura
        g.setColor(new Color(165, 181, 197));
        g.fillRect(x + 6, y + 23, 35, 29);

        g.setColor(new Color(85, 103, 133));
        g.fillRect(x + 12, y + 29, 23, 15);

        // Casco
        g.setColor(new Color(196, 210, 225));
        g.fillRect(x + 5, y + 3, 37, 25);

        g.setColor(new Color(39, 48, 63));
        g.fillRect(x + 12, y + 14, 26, 8);

        // Penacho rojo
        g.setColor(new Color(211, 45, 55));
        g.fillRect(x + 19, y - 8, 10, 14);
        g.fillRect(x + 10, y - 11, 16, 7);

        // Espada
        g.setColor(new Color(118, 77, 39));
        g.fillRect(x + 37, y + 34, 22, 6);

        g.setColor(new Color(225, 232, 238));
        g.fillPolygon(
            new int[]{x + 54, x + 76, x + 54},
            new int[]{y + 35, y + 38, y + 42},
            3
        );
    }

    private void dibujarEnemigo(Graphics2D g) {

        Enemigo enemigo = juego.getEnemigo();

        if (enemigo == null || !enemigo.estaVivo()) {
            return;
        }

        int x = juego.getEnemigoX();
        int y = juego.getEnemigoY();

        // Piernas
        g.setColor(new Color(62, 115, 38));
        g.fillRect(x + 7, y + 49, 12, 21);
        g.fillRect(x + 25, y + 49, 12, 21);

        // Cuerpo
        g.setColor(new Color(91, 69, 39));
        g.fillRect(x + 5, y + 27, 37, 28);

        // Cabeza verde
        g.setColor(new Color(95, 166, 61));
        g.fillRect(x + 4, y + 4, 38, 27);

        // Orejas
        g.fillRect(x - 8, y + 14, 13, 9);
        g.fillRect(x + 41, y + 14, 13, 9);

        // Ojos
        g.setColor(new Color(235, 51, 45));
        g.fillRect(x + 11, y + 14, 6, 5);
        g.fillRect(x + 29, y + 14, 6, 5);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 13));

        g.drawString(
            "VIDA: " + enemigo.getVida(),
            x - 12,
            y - 16
        );
    }

    // ============================================
    // INTERFAZ
    // ============================================

    private void dibujarHUD(Graphics2D g) {

        Caballero caballero = juego.getCaballero();

        // Panel de vida
        g.setColor(new Color(21, 24, 41, 220));
        g.fillRoundRect(15, 15, 260, 70, 12, 12);

        // Corazones
        for (int i = 0; i < 3; i++) {

            g.setColor(
                caballero.getVida() > i * 34
                ? new Color(224, 40, 55)
                : new Color(76, 77, 91)
            );

            dibujarCorazon(g, 35 + i * 35, 32);
        }

        // Barra de vida
        g.setColor(new Color(55, 55, 65));
        g.fillRect(33, 64, 210, 12);

        g.setColor(new Color(222, 44, 51));
        g.fillRect(
            33,
            64,
            (int) (210 * Math.min(100, caballero.getVida()) / 100.0),
            12
        );

        // Contador de monedas
        g.setColor(new Color(21, 24, 41, 220));
        g.fillRoundRect(1110, 20, 145, 53, 12, 12);

        g.setColor(new Color(255, 199, 34));
        g.fillOval(1127, 33, 20, 26);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 24));
        g.drawString("x " + monedas, 1165, 53);

        // Controles
        g.setColor(new Color(20, 24, 35, 210));
        g.fillRoundRect(15, 665, 410, 36, 8, 8);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 15));
        g.drawString(
            "A/D: MOVER   ESPACIO: SALTAR   J: ATACAR",
            27,
            688
        );

        if (!juego.getEnemigo().estaVivo()) {

            g.setColor(new Color(255, 229, 95));
            g.setFont(new Font("Monospaced", Font.BOLD, 27));

            g.drawString("ENEMIGO DERROTADO", 460, 100);
        }
    }

    private void dibujarCorazon(Graphics2D g, int x, int y) {

        g.fillOval(x, y, 14, 14);
        g.fillOval(x + 12, y, 14, 14);

        g.fillPolygon(
            new int[]{x, x + 26, x + 13},
            new int[]{y + 8, y + 8, y + 27},
            3
        );
    }
}
