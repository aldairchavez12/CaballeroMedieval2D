package juego;

import java.awt.*;

public final class MenuJuego {

    private final int ANCHO;
    private final int ALTO;

    public MenuJuego(int ancho, int alto) {

        this.ANCHO = ancho;
        this.ALTO = alto;
    }

    public void dibujarPausa(Graphics2D g,
                             boolean mostrarControles,
                             Point raton) {

        g.setColor(new Color(7, 10, 21, 180));
        g.fillRect(0, 0, ANCHO, ALTO);

        g.setColor(new Color(245, 205, 112));
        g.setFont(new Font("SansSerif", Font.BOLD, 36));

        centrar(g, "PARTIDA EN PAUSA", 175);

        if (mostrarControles) {

            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.PLAIN, 22));

            centrar(g, "A / D - Moverse", 260);
            centrar(g, "ESPACIO - Saltar", 300);
            centrar(g, "J / CLIC DERECHO - Atacar", 340);
            centrar(g, "CLIC IZQUIERDO - Usar objeto", 380);
            centrar(g, "E - Abrir cofre", 420);
            centrar(g, "H - Curar por 5 monedas", 455);

            dibujarBotonMenu(g, "VOLVER", 490, raton);

        } else {

            dibujarBotonMenu(g, "REANUDAR PARTIDA", 230, raton);
            dibujarBotonMenu(g, "REINICIAR CAPITULO", 300, raton);
            dibujarBotonMenu(g, "CONTROLES", 370, raton);
            dibujarBotonMenu(g, "SALIR DEL JUEGO", 440, raton);
        }
    }

    public void dibujarMenuDerrota(Graphics2D g,
                                   Point raton) {

        g.setColor(new Color(12, 5, 10, 235));
        g.fillRect(0, 0, ANCHO, ALTO);

        g.setColor(new Color(225, 85, 85));
        g.setFont(new Font("SansSerif", Font.BOLD, 52));

        centrar(g, "HAS SIDO DERROTADO", 220);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 20));

        centrar(g, "Valdoria aun necesita de ti.", 270);

        dibujarBotonMenu(g, "REINTENTAR CAPITULO", 330, raton);
        dibujarBotonMenu(g, "SALIR DEL JUEGO", 410, raton);

        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g.setColor(Color.LIGHT_GRAY);

        centrar(g, "Tambien puedes presionar R", 520);
    }

    private void dibujarBotonMenu(Graphics2D g,
                                 String texto,
                                 int y,
                                 Point raton) {

        int ancho = 420;
        int alto = 54;
        int x = (ANCHO - ancho) / 2;

        

        boolean encima = raton != null
            && new Rectangle(x, y, ancho, alto)
                .contains(raton);

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(x + 4, y + 4, ancho, alto);

        g.setColor(encima
            ? new Color(90, 100, 125)
            : new Color(48, 55, 75));

        g.fillRect(x, y, ancho, alto);

        g.setColor(encima
            ? new Color(255, 220, 130)
            : new Color(155, 130, 85));

        g.drawRect(x, y, ancho, alto);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 19));

        centrar(g, texto, y + 34);
    }

    public boolean contieneBoton(Point punto, int y) {

        return new Rectangle(
            (ANCHO - 420) / 2,
            y, 420, 54
        ).contains(punto);
    }

    private void centrar(Graphics2D g,
                         String texto, int y) {

        int x = (ANCHO -
            g.getFontMetrics().stringWidth(texto)) / 2;

        g.drawString(texto, x, y);
    }
}