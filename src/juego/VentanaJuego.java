package juego;

import javax.swing.JFrame;

public class VentanaJuego extends JFrame {

    public VentanaJuego() {
        setTitle("Caballero Medieval 2D");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        PanelJuego panelJuego = new PanelJuego();
        add(panelJuego);

        pack();
        setLocationRelativeTo(null);
    }
}