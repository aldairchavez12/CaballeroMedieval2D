package juego;

import java.awt.*;
import entidades.Caballero;

public final class RenderizadorHUD {

    private final int ANCHO;
    private final int ALTO;

    public RenderizadorHUD(int ancho, int alto) {

        this.ANCHO = ancho;
        this.ALTO = alto;
    }

public void dibujarHUD(
            Graphics2D g,
            Caballero c,
            int monedas,
            int enemigosRestantes,
            String titulo,
            APIClient.Clima climaActual,
            String aviso,
            int avisoDuracion,
            int metaX) {
        
        g.setColor(new Color(20, 24, 40, 221));
        g.fillRoundRect(18, 18, 280, 88, 16, 16);
        g.setColor(new Color(240, 206, 128));
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.drawString("CABALLERO DE VALDORIA", 33, 41);
        g.setColor(new Color(68, 69, 80));
        g.fillRoundRect(33, 58, 248, 20, 9, 9);
        g.setColor(c.getVida() > c.getVidaMaxima() * 0.30 ? new Color(207, 63, 74) : new Color(235, 103, 63));
        g.fillRoundRect(33, 58, (int) (248 * Math.max(0.0, Math.min(1.0,
            (double) c.getVida()
            / Math.max(1, c.getVidaMaxima())))), 20, 9, 9);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString(c.getVida() + " / " + c.getVidaMaxima() + " HP", 119, 73);

        g.setColor(new Color(20, 24, 40, 221));
        g.fillRoundRect(1085, 20, 170, 55, 14, 14);
        g.setColor(new Color(255, 190, 44));
        g.fillOval(1105, 34, 22, 27);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.drawString("x " + monedas, 1140, 55);

        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(new Color(245, 210, 110));
        g.drawString("H: CURAR (5 MONEDAS)", 1090, 76);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString("ENEMIGOS: " + enemigosRestantes, 1090, 96);
        g.setColor(new Color(29, 32, 47, 195));
        g.fillRoundRect(425, 81, 430, 14, 8, 8);
        g.setColor(new Color(246, 198, 101));
        g.fillRoundRect(427, 83, Math.min(426, 426 * c.getX() / metaX), 10, 7, 7);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));

        g.drawString(
            "Clima API: " + climaActual,
            1090,
            125
        );
        g.setColor(new Color(20, 24, 40, 208));
        g.fillRoundRect(315, 19, 660, 50, 13, 13);
        g.setColor(new Color(248, 217, 135));
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        
        g.drawString(titulo, (ANCHO - g.getFontMetrics().stringWidth(titulo)) / 2, 51);

        
        if (avisoDuracion > 0) {
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            int w = g.getFontMetrics().stringWidth(aviso) + 50;
            g.setColor(new Color(27, 33, 44, 225));
            g.fillRoundRect((ANCHO - w) / 2, 100, w, 48, 14, 14);
            g.setColor(new Color(251, 218, 116));
            g.drawString(aviso, (ANCHO - g.getFontMetrics().stringWidth(aviso)) / 2, 131);
        }
        if (c.tieneFuriaActiva()) {

            g.setColor(new Color(230, 80, 60));
            g.setFont(new Font("SansSerif", Font.BOLD, 16));

            g.drawString(
                "FURIA ACTIVA +"
                + c.getBonoAtaque(),
                35,
                165
            );
        }
    }

public void dibujarNarrativa(
            Graphics2D g,
            String mensaje,
            String cabecera,
            boolean mostrarContinuar) {
        
        if (mensaje.isEmpty()) return;
        g.setColor(new Color(10, 13, 25, 190));
        g.fillRect(0, 0, ANCHO, ALTO);
        g.setColor(new Color(25, 29, 47, 247));
        g.fillRoundRect(180, 240, 920, 190, 22, 22);
        g.setColor(new Color(224, 184, 97));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(180, 240, 920, 190, 22, 22);
        g.setFont(new Font("SansSerif", Font.BOLD, 29));
        
        centrar(g, cabecera, 311);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 19));
        centrar(g, mensaje, 361);
        if (mostrarContinuar) {
            g.setFont(new Font("SansSerif", Font.BOLD, 15));
            g.setColor(new Color(242, 210, 127));
            centrar(g, "PULSA ENTER PARA CONTINUAR", 397);
        }
        
        
    }

    private void centrar(Graphics2D g,
                         String texto, int y) {

        int x = (ANCHO -
            g.getFontMetrics().stringWidth(texto)) / 2;

        g.drawString(texto, x, y);
    }
}