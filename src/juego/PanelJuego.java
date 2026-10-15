package juego;

import javax.swing.JPanel;
import javax.swing.Timer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;

public class PanelJuego extends JPanel {

    private static final int ANCHO = 1280;
    private static final int ALTO = 720;

    private final Juego juego;

    private final Timer gameLoop;

    public PanelJuego() {

        setPreferredSize(new Dimension(ANCHO, ALTO));

        setBackground(Color.BLACK);

        setFocusable(true);

        juego = new Juego();

        gameLoop = new Timer(16, e -> {

            juego.actualizar();

            repaint();

        });

        juego.iniciar();

        gameLoop.start();
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        dibujarEscenarioPrueba(g);
    }

    private void dibujarEscenarioPrueba(Graphics g) {

        g.setColor(Color.WHITE);

        g.drawString(
                "Caballero Medieval 2D",
                30,
                40
        );

        // Piso temporal
        g.setColor(Color.GRAY);

        g.fillRect(
                0,
                ALTO - 100,
                ANCHO,
                100
        );
    }
}