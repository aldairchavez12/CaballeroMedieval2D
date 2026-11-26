package juego;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.function.ToIntFunction;

import entidades.*;
import items.herramientas.*;

public final class RenderizadorEntidades {

    private final int ANCHO;
    private final int SUELO_Y;

    public RenderizadorEntidades(
            int ancho,
            int sueloY) {

        this.ANCHO = ancho;
        this.SUELO_Y = sueloY;
    }

    public void dibujarCaballero(
            Graphics2D g,
            Caballero c,
            int efectoAtaque,
            boolean partidaActiva) {
        
        if (!c.estaVivo()) return;
        int x = c.getX(), y = c.getY();
        AffineTransform transformacionOriginal = g.getTransform();

        if (!c.miraDerecha()) {

            g.translate(2 * x + Caballero.ANCHO, 0);
            g.scale(-1, 1);
        }
        // Sombra sobre el suelo
        g.setColor(new Color(15, 14, 26, 100));
        g.fillOval(x - 10, SUELO_Y - 9, 66, 12);
        // Capa
        g.setColor(new Color(172, 34, 57));
        g.fillPolygon(new int[]{x + 10, x - 16, x + 14}, new int[]{y + 17, y + 64, y + 53}, 3);
        
        boolean tieneArmadura =
            c.getArmaduraEquipada() != null
            && c.getArmaduraEquipada().getDurabilidad() > 0;

        if (!tieneArmadura) {

            // Ropa medieval básica
            g.setColor(new Color(105, 70, 45));
            g.fillRoundRect(x + 5, y + 23, 37, 31, 5, 5);

            // Cinturón de cuero
            g.setColor(new Color(65, 42, 28));
            g.fillRect(x + 5, y + 45, 37, 6);

            // Cabeza sin casco
            g.setColor(new Color(224, 180, 135));
            g.fillOval(x + 8, y + 2, 31, 29);

            // Cabello
            g.setColor(new Color(70, 43, 27));
            g.fillArc(x + 8, y, 31, 19, 0, 180);
            // Pierna izquierda
            g.setColor(new Color(80, 50, 35));
            g.fillRect(x + 10, y + 54, 8, 18);

            // Pierna derecha
            g.fillRect(x + 28, y + 54, 8, 18);

            // Botas
            g.setColor(new Color(45, 30, 20));
            g.fillRect(x + 8, y + 70, 12, 5);
            g.fillRect(x + 26, y + 70, 12, 5);

        } else {

            // Peto de armadura
            g.setColor(new Color(169, 185, 205));
            g.fillRoundRect(x + 5, y + 23, 37, 31, 5, 5);

            // Detalles metálicos
            g.setColor(new Color(79, 101, 130));
            g.fillRect(x + 11, y + 31, 24, 14);

            // Casco metálico
            g.setColor(new Color(205, 217, 229));
            g.fillRoundRect(x + 4, y + 2, 39, 27, 7, 7);

            // Visera
            g.setColor(new Color(36, 47, 63));
            g.fillRect(x + 12, y + 15, 26, 7);

            // Adorno rojo del casco
            g.setColor(new Color(215, 41, 53));
            g.fillRect(x + 19, y - 8, 11, 15);
            g.fillRect(x + 10, y - 11, 16, 7);

            // Protección metálica de piernas
            g.setColor(new Color(169, 185, 205));
            g.fillRect(x + 7, y + 46, 13, 23);
            g.fillRect(x + 26, y + 46, 13, 23);
            // Grebas metálicas
            g.setColor(new Color(170, 185, 205));
            g.fillRect(x + 10, y + 54, 8, 18);
            g.fillRect(x + 28, y + 54, 8, 18);

            // Botas
            g.setColor(new Color(70, 80, 95));
            g.fillRect(x + 8, y + 70, 12, 5);
            g.fillRect(x + 26, y + 70, 12, 5);
        }

        // Espada visible

            
        Herramienta arma = c.getArmaEquipada();

        if (arma instanceof Espada
                && arma.getDurabilidad() > 0) {

            // Mango de la espada
            g.setColor(new Color(120, 75, 40));
            g.fillRect(x + 37, y + 35, 10, 6);

            // Guarda dorada
            g.setColor(new Color(215, 175, 65));
            g.fillRect(x + 46, y + 30, 5, 16);

            // Hoja metálica
            g.setColor(new Color(220, 230, 240));
            g.fillRect(x + 51, y + 33, 22, 8);

            // Punta
            g.fillPolygon(
                new int[]{x + 73, x + 82, x + 73},
                new int[]{y + 33, y + 37, y + 41},
                3
            );

        } else if (arma instanceof Arco
                && arma.getDurabilidad() > 0) {

            // Cuerpo del arco
            g.setColor(new Color(140, 85, 35));
            g.setStroke(new BasicStroke(4));
            g.drawArc(x + 28, y + 12, 34, 56, -90, 180);

            // Cuerda
            g.setColor(Color.LIGHT_GRAY);
            g.setStroke(new BasicStroke(1));
            g.drawLine(x + 45, y + 12, x + 45, y + 68);
        }

        // Escudo equipado
        if (c.getEscudoEquipado() != null
                && c.getEscudoEquipado().getDurabilidad() > 0) {

            g.setColor(new Color(105, 118, 145));
            g.fillOval(x - 16, y + 24, 29, 37);

            g.setColor(new Color(235, 195, 90));
            g.drawOval(x - 16, y + 24, 29, 37);
        }

        // Armadura equipada

if (efectoAtaque > 0 && partidaActiva) {

            g.setColor(new Color(
                255, 237, 157,
                Math.min(240, efectoAtaque * 22)
            ));

            if (arma instanceof Espada
                    && arma.getDurabilidad() > 0) {

                // Corte amplio con espada
                g.setStroke(new BasicStroke(5));
                g.drawArc(x + 33, y - 15, 100, 104, -100, 115);

            } else if (arma == null
                    || arma.getDurabilidad() <= 0) {

                // Golpe corto con los puños
                g.setStroke(new BasicStroke(4));
                g.drawArc(x + 30, y + 20, 30, 28, -80, 120);
            }

            g.setStroke(new BasicStroke(1));
        }

        // Restaurar SIEMPRE la transformación gráfica
        g.setTransform(transformacionOriginal);
    }

public void dibujarEnemigo(
            Graphics2D g,
            List<Enemigo> enemigos,
            ToIntFunction<Enemigo> obtenerPosicion,
            int enemigoY,
            int cap,
            int camaraX) {
        for (Enemigo enemigo : enemigos) {
            if (!enemigo.estaVivo()) continue;
            int x = obtenerPosicion.applyAsInt(enemigo);
            if (x < camaraX - 100 || x > camaraX + ANCHO + 100) continue;
            int y = enemigoY;
            boolean jefe = enemigo instanceof Boss;
            
            Color piel = cap == 1 ? new Color(105, 164, 78) : cap == 2 ? new Color(140, 150, 169) : new Color(165, 84, 130);
            g.setColor(new Color(15, 18, 25, 85));
            g.fillOval(x - 5, SUELO_Y - 8, 55, 12);
            g.setColor(jefe ? new Color(77, 42, 84) : new Color(83, 77, 78));
            g.fillRect(x + 5, y + 25, 37, 36);
            g.fillRect(x + 8, y + 51, 11, 19);
            g.fillRect(x + 26, y + 51, 11, 19);
            g.setColor(piel);
            g.fillRoundRect(x + 4, y + 3, 38, 30, 7, 7);
            g.setColor(new Color(247, 79, 79));
            g.fillRect(x + 11, y + 16, 7, 5);
            g.fillRect(x + 28, y + 16, 7, 5);
            if (jefe) {
                g.setColor(new Color(74, 37, 76));
                g.fillPolygon(new int[]{x, x + 13, x + 8}, new int[]{y + 9, y - 25, y + 9}, 3);
                g.fillPolygon(new int[]{x + 37, x + 31, x + 47}, new int[]{y + 9, y - 25, y + 9}, 3);
            }
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString(enemigo.getNombre(), x - 20, y - 36);
            int total = Math.max(1, enemigo.getVidaMaxima());
            g.setColor(new Color(42, 35, 46));
            g.fillRoundRect(x - 10, y - 26, 76, 9, 5, 5);
            g.setColor(new Color(226, 73, 88));
            g.fillRoundRect(x - 8, y - 24, Math.max(0, Math.min(72, (int)
            (72.0 * enemigo.getVida() / total))), 5, 5, 5);
        }
    }

    public void dibujarAliado(
            Graphics2D g,
            Aliado aliado) {

        

        if (!aliado.estaVivo()) {
            return;
        }

        int x = aliado.getX();
        int y = aliado.getY();

        // Piernas
        g.setColor(new Color(55, 70, 105));
        g.fillRect(x + 10, y + 45, 10, 24);
        g.fillRect(x + 27, y + 45, 10, 24);

        // Cuerpo
        g.setColor(new Color(45, 115, 160));
        g.fillRoundRect(x + 5, y + 20, 38, 32, 7, 7);

        // Cinturón
        g.setColor(new Color(160, 120, 65));
        g.fillRect(x + 5, y + 43, 38, 6);

        // Cabeza
        g.setColor(new Color(220, 180, 140));
        g.fillOval(x + 10, y, 28, 28);

        // Cabello
        g.setColor(new Color(75, 45, 25));
        g.fillArc(x + 10, y - 2, 28, 19, 0, 180);

        // Emblema
        g.setColor(new Color(240, 210, 95));
        g.fillOval(x + 19, y + 30, 10, 10);

        // Nombre
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(Color.WHITE);
        g.drawString("ALIADO", x - 2, y - 30);

        // Barra de vida
        g.setColor(new Color(65, 65, 65));
        g.fillRoundRect(x - 7, y - 23, 62, 7, 4, 4);

        int anchoVida = 60 * aliado.getVida()
                / aliado.getVidaMaxima();

        g.setColor(new Color(65, 200, 110));
        g.fillRoundRect(x - 6, y - 22, anchoVida, 5, 3, 3);
    }
}