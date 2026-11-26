package juego;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.util.Random;

public final class RenderizadorClima {

    private final int ancho;
    private final int alto;

    private final int[] lluviaX = new int[75];
    private final int[] lluviaY = new int[75];

    public RenderizadorClima(int ancho, int alto) {

        this.ancho = ancho;
        this.alto = alto;

        Random random = new Random(2026);

        for (int i = 0; i < lluviaX.length; i++) {

            lluviaX[i] = random.nextInt(ancho);
            lluviaY[i] = random.nextInt(alto);
        }
    }

    public void dibujar(Graphics2D original,
                        APIClient.Clima clima,
                        int fotograma) {

        Graphics2D g = (Graphics2D) original.create();

        try {

            switch (clima) {

                case LLUVIOSO:

                    g.setColor(new Color(35, 50, 75, 65));
                    g.fillRect(0, 0, ancho, alto);

                    g.setStroke(new BasicStroke(2));

                    for (int i = 0; i < lluviaX.length; i++) {

                        int x = lluviaX[i];

                        int y = (int) Math.floorMod(
                            lluviaY[i] + fotograma * 8L,
                            (long) alto
                        );

                        g.setColor(
                            new Color(190, 220, 255, 170)
                        );

                        g.drawLine(x, y, x - 7, y + 18);
                    }

                    break;

                case NUBLADO:

                    g.setColor(new Color(55, 65, 85, 75));
                    g.fillRect(0, 0, ancho, alto);

                    break;

                case SOLEADO:
                    break;
            }

        } finally {
            g.dispose();
        }
    }
}