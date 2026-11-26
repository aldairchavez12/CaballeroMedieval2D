package juego;

import java.awt.*;
import java.util.List;
import escenario.Estructura;
import escenario.Cofre;

public final class RenderizadorEscenario {

    private final int ANCHO;
    private final int ALTO;
    private final int SUELO_Y;
    private final int ANCHO_NIVEL;
    private final int META_X;

    public RenderizadorEscenario(
            int ancho,
            int alto,
            int sueloY,
            int anchoNivel,
            int metaX) {

        this.ANCHO = ancho;
        this.ALTO = alto;
        this.SUELO_Y = sueloY;
        this.ANCHO_NIVEL = anchoNivel;
        this.META_X = metaX;
    }

    public void dibujarFondo(
            Graphics2D g,
            APIClient.Clima climaActual,
            int cap,
            int camaraX) {
        Color colorCielo;

        switch (climaActual) {

            case LLUVIOSO:
                colorCielo = new Color(65, 75, 100);
                break;

            case NUBLADO:
                colorCielo = new Color(110, 115, 140);
                break;

            default:
                colorCielo = new Color(95, 155, 210);
                break;
        }

        g.setColor(colorCielo);
        g.fillRect(0, 0, ANCHO, ALTO);
        
        Color arriba = cap == 1 ? new Color(27, 50, 85) : cap == 2 ? new Color(30, 36, 63) : new Color(53, 23, 58);
        Color abajo = cap == 1 ? new Color(234, 155, 111) : cap == 2 ? new Color(111, 106, 136) : new Color(155, 62, 81);
        g.setPaint(new GradientPaint(0, 0, arriba, 0, 620, abajo));
        g.fillRect(0, 0, ANCHO, ALTO);
        g.setColor(cap == 3 ? new Color(230, 99, 105) : new Color(255, 213, 142));
        g.fillOval(920 - camaraX / 20, 115, 145, 145);
        // Parallax: varias capas se desplazan a velocidades distintas.
        for (int i = 0; i < 15; i++) {
            int x = i * 190 - (camaraX / 6) % 190;
            g.setColor(new Color(77, 84, 117));
            g.fillPolygon(new int[]{x - 110, x + 15, x + 150}, new int[]{590, 290 + (i % 3) * 36, 590}, 3);
        }
        for (int i = 0; i < 18; i++) {
            int x = i * 105 - (camaraX / 3) % 105;
            if (cap == 1) {
                g.setColor(new Color(30, 66, 71));
                g.fillPolygon(new int[]{x, x + 50, x + 100}, new int[]{608, 435 - (i%3)*30, 608}, 3);
            } else if (cap == 2) {
                g.setColor(new Color(44, 48, 77));
                g.fillRect(x + 10, 390, 65, 220);
                g.fillRect(x, 380, 85, 16);
            } else {
                g.setColor(new Color(55, 32, 62));
                g.fillRect(x + 20, 390, 58, 220);
                g.fillPolygon(new int[]{x+10,x+49,x+88}, new int[]{390,325,390}, 3);
            }
        }
        
    }

    public void dibujarEscenario(
            Graphics2D g,
            int cap,
            int camaraX) {
        
        g.setColor(new Color(35, 39, 54));
        g.fillRect(0, SUELO_Y, ANCHO_NIVEL, 100);
        g.setColor(cap == 1 ? new Color(90, 149, 67) : cap == 2 ? new Color(106, 120, 130) : new Color(162, 76, 85));
        g.fillRect(0, SUELO_Y - 13, ANCHO_NIVEL, 13);
        // Detalles del suelo solo en rango visible (evita trabajo por frame innecesario).
        for (int x = Math.max(0, camaraX - 50); x < Math.min(ANCHO_NIVEL, camaraX + ANCHO + 60); x += 44) {
            for (int y = SUELO_Y + 12; y < ALTO; y += 26) {
                g.setColor(new Color(88, 89, 104));
                g.drawRect(x, y, 40, 23);
            }
        }
        // El escenario cambia gradualmente durante el avance, no solo al cambiar de capitulo.
        for (int x = 300; x < ANCHO_NIVEL; x += 460) {
            int seccion = x / 1200;
            if (seccion == 0) {
                g.setColor(new Color(43, 84, 69));
                g.fillRect(x + 15, 435, 18, 175);
                g.fillOval(x - 30, 374, 105, 100);
                g.fillOval(x - 10, 343, 65, 90);
            } else if (seccion == 1) {
                g.setColor(new Color(60, 63, 86));
                g.fillRect(x - 5, 430, 90, 180);
                g.fillRect(x - 14, 417, 108, 20);
            } else if (seccion == 2) {
                g.setColor(new Color(69, 57, 80));
                g.fillRect(x, 365, 70, 240);
                g.fillPolygon(new int[]{x-15,x+35,x+85}, new int[]{370,305,370}, 3);
            } else {
                g.setColor(new Color(46, 37, 62));
                g.fillRect(x, 400, 80, 210);
                g.setColor(new Color(223, 93, 85));
                g.fillRect(x+29, 455, 18, 25);
            }
        }
        // Hitos narrativos en el recorrido.
        String[] zonas = {"EL SENDERO OLVIDADO", "LAS RUINAS ANTIGUAS", "LA GUARDIA DE LAS SOMBRAS", "LA ULTIMA PUERTA"};
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        for (int i=0; i<zonas.length; i++) {
            g.setColor(new Color(245, 215, 145));
            g.drawString(zonas[i], 120 + i * 1200, 235);
        }
    }

    public void dibujarEstructuras(
            Graphics2D g,
            List<Estructura> estructuras) {

        for (Estructura e : estructuras) {

            if (e.estaDestruida()) {
                continue;
            }

            int x = e.getX();
            int y = e.getY();

            g.setColor(new Color(125, 80, 45));
            g.fillRect(x, y, 45, 620 - y);

            g.setColor(new Color(70, 40, 25));
            g.drawRect(x, y, 45, 620 - y);
            g.drawLine(x, y, x + 45, 620);
            g.drawLine(x + 45, y, x, 620);

            g.setColor(Color.DARK_GRAY);
            g.fillRect(x, y - 12, 45, 6);

            int anchoVida = 45 * e.getResistencia()
                / Math.max(1, e.getResistenciaMaxima());

            g.setColor(Color.GREEN);
            g.fillRect(x, y - 12, anchoVida, 6);
        }
    }

    public void dibujarMeta(
            Graphics2D g,
            boolean abierta) {
        int x = META_X;
        
        g.setColor(new Color(40, 40, 64));
        g.fillRoundRect(x, 415, 95, 194, 13, 13);
        g.setColor(abierta ? new Color(103, 224, 180) : new Color(218, 86, 97));
        g.fillRoundRect(x + 14, 433, 67, 161, 14, 14);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 17));
        g.drawString(abierta ? "PORTAL ABIERTO" : "DERROTA A LOS ENEMIGOS", x - 110, 395);
    }

    public void dibujarMonedas(
            Graphics2D g,
            int[][] posicionesMonedas,
            boolean[] monedasRecogidas,
            int fotograma) {
        for (int i = 0; i < posicionesMonedas.length; i++) {
            if (monedasRecogidas[i]) continue;
            int x = posicionesMonedas[i][0];
            int y = posicionesMonedas[i][1] + (int) (3 * Math.sin(fotograma * 0.08 + i));
            g.setColor(new Color(130, 73, 12));
            g.fillOval(x - 2, y - 2, 26, 32);
            g.setColor(new Color(255, 193, 37));
            g.fillOval(x, y, 22, 28);
            g.setColor(new Color(255, 239, 157));
            g.fillOval(x + 6, y + 5, 9, 17);
        }
    }

    public void dibujarCofre(
            Graphics2D g,
            List<Cofre> cofres,
            int jugadorX,
            boolean partidaActiva,
            boolean pausado) {

        for (Cofre cofre : cofres) {

            int x = cofre.getX();
            int y = cofre.getY();

            boolean abierto = cofre.estaAbierto();

            g.setColor(new Color(55, 30, 18));
            g.fillRoundRect(x, y, 58, 40, 6, 6);

            g.setColor(new Color(155, 91, 40));
            g.fillRect(x + 4, y + 12, 50, 24);

            g.setColor(new Color(190, 125, 55));
            g.fillRoundRect(
                x,
                abierto ? y - 12 : y,
                58, 17, 5, 5
            );

            g.setColor(new Color(240, 190, 65));
            g.fillRect(x, y + 18, 58, 4);
            g.fillRect(x + 24, y + 13, 10, 14);

            g.setColor(Color.BLACK);
            g.fillRect(x + 28, y + 17, 3, 5);

            if (!abierto
                    && partidaActiva
                    && !pausado
                    && Math.abs(jugadorX - x) < 100) {

                dibujarBurbuja(
                    g,
                    "E  ABRIR COFRE",
                    x - 55,
                    y - 60
                );
            }
        }
    }

    private void dibujarBurbuja(Graphics2D g, String texto, int x, int y) {
        g.setColor(new Color(25, 28, 39, 228));
        g.fillRoundRect(x, y, 170, 39, 12, 12);
        g.setColor(new Color(249, 215, 113));
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString(texto, x + 18, y + 25);
    }
}