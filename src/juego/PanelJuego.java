package juego;

import entidades.*;
import items.*;
import items.consumibles.*;
import items.herramientas.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.List;
import java.util.Random;
import java.awt.geom.AffineTransform;
/**
 * Presentacion y controles del juego. Conserva el contrato con Juego,
 * Caballero, Enemigo, Boss e interfaces del UML original.
 * No crea ni sustituye clases del dominio.
 */
public class PanelJuego extends JPanel {

    private static final int SLOTS_HOTBAR = 9;

    private int slotSeleccionado = 0;
    private boolean mirandoDerecha = true;

    private static final int ANCHO = 1280;
    private static final int ALTO = 720;
    private static final int SUELO_Y = 620;
    private static final int TOTAL_MONEDAS = 6;
    private boolean inventarioAbierto = false;

    private final Juego juego;
    private final Timer gameLoop;
    private boolean izquierda;
    private boolean derecha;
    private boolean pausado;
    private int fotograma;
    private int capituloVisual;
    private int camaraX;
    private int zonaVisual = -1;
    private int monedas;
    private final boolean[] monedasRecogidas = new boolean[TOTAL_MONEDAS];
    private final int[][] posicionesMonedas = {{320,565}, {950,565}, {1700,565}, {2540,565}, {3450,565}, {4450,565}};
    private boolean cofreAbierto;
    private int avisoDuracion;
    private String aviso = "";
    private int efectoAtaque;
    private int vidaAnterior;
    private int efectoDanio;
    private int enemigoVidaAnterior;
    private final Random efectos = new Random(2026);
    private final int[] lluviaX = new int[75];
    private final int[] lluviaY = new int[75];

    public PanelJuego() {
        setPreferredSize(new Dimension(ANCHO, ALTO));
        setFocusable(true);
        setBackground(Color.BLACK);
        setDoubleBuffered(true);

        // Singleton: el constructor de Juego es privado.
        juego = Juego.getInstancia();
        capituloVisual = juego.getCapitulo();
        vidaAnterior = juego.getCaballero().getVida();
        enemigoVidaAnterior = juego.getEnemigo() == null ? 0 : juego.getEnemigo().getVida();

        juego.getCaballero().agregarObservador(evento -> {

            int vidaAnterior = (int) evento.getOldValue();
            int vidaActual = (int) evento.getNewValue();

            if (vidaActual < vidaAnterior) {

                efectoDanio = 14;

            } else if (vidaActual > vidaAnterior) {

                avisar(
                    "VIDA RECUPERADA: +"
                    + (vidaActual - vidaAnterior)
                );
            }

            repaint();
        });

        for (int i = 0; i < lluviaX.length; i++) {
            lluviaX[i] = efectos.nextInt(ANCHO);
            lluviaY[i] = efectos.nextInt(ALTO);
        }

        configurarControles();
        gameLoop = new Timer(16, e -> actualizarPantalla());
        juego.iniciar();
        gameLoop.start();
    }

    private void configurarControles() {
        InputMap input = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap acciones = getActionMap();
        input.put(KeyStroke.getKeyStroke("pressed A"), "izquierdaOn");
        input.put(KeyStroke.getKeyStroke("released A"), "izquierdaOff");
        input.put(KeyStroke.getKeyStroke("pressed D"), "derechaOn");
        input.put(KeyStroke.getKeyStroke("released D"), "derechaOff");
        input.put(KeyStroke.getKeyStroke("pressed SPACE"), "saltar");
        input.put(KeyStroke.getKeyStroke("pressed J"), "atacar");
        input.put(KeyStroke.getKeyStroke("pressed E"), "interactuar");
        input.put(KeyStroke.getKeyStroke("pressed ENTER"), "continuar");
        input.put(KeyStroke.getKeyStroke("pressed ESC"), "pausar");
        
        input.put(KeyStroke.getKeyStroke("pressed X"), "eliminarItem");

        acciones.put("eliminarItem", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (pausado || !juego.estaActivo()) {
                    return;
                }

                List<Item> objetos = juego.getInventario()
                        .obtenerItems();

                if (slotSeleccionado >= objetos.size()) {
                    return;
                }

                Item seleccionado = objetos.get(slotSeleccionado);

                if (juego.getInventario().eliminarItem(seleccionado)) {
                    avisar("ELIMINADO: " + seleccionado.getNombre());
                    repaint();
                }
            }
        });
        acciones.put("izquierdaOn", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { izquierda = true; }
        });
        acciones.put("izquierdaOff", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { izquierda = false; }
        });
        acciones.put("derechaOn", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { derecha = true; }
        });
        acciones.put("derechaOff", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { derecha = false; }
        });
        acciones.put("saltar", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (!pausado && juego.estaActivo()) juego.getCaballero().saltar();
            }
        });
        acciones.put("atacar", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (!pausado && juego.estaActivo()) {
                    juego.atacarEnemigo();
                    efectoAtaque = 10;
                }
            }
        });
        acciones.put("interactuar", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { abrirCofre(); }
        });
        acciones.put("continuar", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (pausado) { pausado = false; return; }
                juego.continuarHistoria();
            }
        });
        acciones.put("pausar", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (juego.getEstado() == Juego.Estado.JUGANDO) {
                    pausado = !pausado;
                    izquierda = false;
                    derecha = false;
                }
            }
        });
        

        // ============================================
        // CONTROLES DEL INVENTARIO
        // ============================================

        for (int i = 1; i <= 9; i++) {

            final int indice = i - 1;

            input.put(
                KeyStroke.getKeyStroke("pressed " + i),
                "usarObjeto" + i
            );

            acciones.put("usarObjeto" + i, new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    if (inventarioAbierto && !pausado) {

                        if (juego.usarItem(indice)) {
                            avisar("OBJETO UTILIZADO");
                        }
                    }
                }
            });
        }
    for (int i = 1; i <= SLOTS_HOTBAR; i++) {

        final int indice = i - 1;

        input.put(
            KeyStroke.getKeyStroke("pressed " + i),
            "seleccionarSlot" + i
        );

        acciones.put(
            "seleccionarSlot" + i,
            new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    if (!pausado && juego.estaActivo()) {
                        slotSeleccionado = indice;
                        repaint();
                    }
                }
            }
        );
    }
    addMouseWheelListener(e -> {

        if (pausado || !juego.estaActivo()) {
            return;
        }

        int desplazamiento = e.getWheelRotation();

        slotSeleccionado = Math.floorMod(
            slotSeleccionado + desplazamiento,
            SLOTS_HOTBAR
        );

        repaint();
    });

    addMouseListener(new MouseAdapter() {

        @Override
        public void mousePressed(MouseEvent e) {

            if (pausado || !juego.estaActivo()) {
                return;
            }

            if (SwingUtilities.isLeftMouseButton(e)) {

                if (juego.usarItem(slotSeleccionado)) {
                    avisar("OBJETO UTILIZADO");
                }

            } else if (SwingUtilities.isRightMouseButton(e)) {

                juego.atacarEnemigo();
                efectoAtaque = 10;
            }

            repaint();
        }

    });


    }

    private void actualizarPantalla() {
        fotograma++;
        if (avisoDuracion > 0) avisoDuracion--;
        if (efectoAtaque > 0) efectoAtaque--;
        if (efectoDanio > 0) efectoDanio--;

        if (juego.getCapitulo() != capituloVisual) {
            capituloVisual = juego.getCapitulo();
            for (int i = 0; i < monedasRecogidas.length; i++) monedasRecogidas[i] = false;
            cofreAbierto = false;
            camaraX = 0;
            zonaVisual = -1;
            avisar("NUEVO CAPITULO: " + capituloVisual);
        }

        if (!pausado && !inventarioAbierto && juego.estaActivo()) {
            int direccion = (derecha ? 1 : 0) - (izquierda ? 1 : 0);
            juego.getCaballero().establecerDireccion(direccion);
            juego.actualizar();
            actualizarMonedas();
            camaraX = Math.max(0, Math.min(Juego.ANCHO_NIVEL - ANCHO,
                    juego.getCaballero().getX() - ANCHO / 3));
            int nuevaZona = Math.min(3, juego.getCaballero().getX() / 1200);
            if (nuevaZona != zonaVisual) {
                zonaVisual = nuevaZona;
                if (nuevaZona == 1) avisar("LAS SOMBRAS CRECEN... PROTEGE EL REINO");
                else if (nuevaZona == 2) avisar("LOS GUARDIANES VIGILAN ESTE CAMINO");
                else if (nuevaZona == 3) avisar("LA PUERTA DEL DESTINO ESTA CERCA");
            }
        } else {
            juego.getCaballero().establecerDireccion(0);
        }

        int vidaActual = juego.getCaballero().getVida();
        if (vidaActual < vidaAnterior) efectoDanio = 14;
        vidaAnterior = vidaActual;
        Enemigo enemigo = juego.getEnemigo();
        if (enemigo != null) enemigoVidaAnterior = enemigo.getVida();
        repaint();
        int direccion = (derecha ? 1 : 0) - (izquierda ? 1 : 0);

        if (direccion > 0) {
            mirandoDerecha = true;
        } else if (direccion < 0) {
            mirandoDerecha = false;
        }

        juego.getCaballero().establecerDireccion(direccion);
    }

    private void avisar(String mensaje) {
        aviso = mensaje;
        avisoDuracion = 130;
    }

    private void actualizarMonedas() {
        Caballero c = juego.getCaballero();
        Rectangle jugador = new Rectangle(c.getX(), c.getY(), Caballero.ANCHO, Caballero.ALTO);
        for (int i = 0; i < TOTAL_MONEDAS; i++) {
            if (!monedasRecogidas[i]) {
                Rectangle moneda = new Rectangle(posicionesMonedas[i][0], posicionesMonedas[i][1], 22, 28);
                if (jugador.intersects(moneda)) {
                    monedasRecogidas[i] = true;
                    monedas++;
                    avisar("+1 MONEDA");
                }
            }
        }
    }

    private void abrirCofre() {
        if (pausado || !juego.estaActivo() || cofreAbierto) return;
        Caballero c = juego.getCaballero();
        Rectangle jugador = new Rectangle(c.getX(), c.getY(), Caballero.ANCHO, Caballero.ALTO);
        Rectangle cofre = new Rectangle(2770, 556, 75, 54);
        if (jugador.intersects(new Rectangle(cofre.x - 35, cofre.y - 35, cofre.width + 70, cofre.height + 45))) {
           Pocima pocima = juego.crearPocimaVida();

            if (juego.recogerItem(pocima)) {
                cofreAbierto = true;
                monedas += 5;
                avisar("OBTUVISTE UNA POCIMA Y 5 MONEDAS");
            } else {
                avisar("INVENTARIO LLENO");
            }
        }
    }

    private void dibujarHotbar(Graphics2D g) {

        int tamano = 58;
        int separacion = 5;
        int total = SLOTS_HOTBAR * (tamano + separacion)
                - separacion;

        int inicioX = (ANCHO - total) / 2;
        int inicioY = ALTO - 92;

        List<Item> objetos = juego.getInventario()
                .obtenerItems();

        g.setFont(new Font("SansSerif", Font.BOLD, 12));

        for (int i = 0; i < SLOTS_HOTBAR; i++) {

            int x = inicioX + i * (tamano + separacion);
            int y = inicioY;

            boolean seleccionado = i == slotSeleccionado;

            g.setColor(new Color(22, 25, 36, 225));
            g.fillRoundRect(x, y, tamano, tamano, 8, 8);

            g.setStroke(new BasicStroke(seleccionado ? 3 : 1));

            g.setColor(
                seleccionado
                    ? new Color(255, 210, 90)
                    : new Color(135, 140, 155)
            );

            g.drawRoundRect(x, y, tamano, tamano, 8, 8);

            g.setColor(Color.LIGHT_GRAY);
            g.drawString(String.valueOf(i + 1), x + 5, y + 13);

            if (i < objetos.size()) {

                Item item = objetos.get(i);
                String nombre = item.getNombre();

                String icono = "?";

                String tipo = item.getClass().getSimpleName();

                switch (tipo) {
                    case "Espada": icono = "E"; break;
                    case "Arco": icono = "A"; break;
                    case "Flecha": icono = "F"; break;
                    case "Escudo": icono = "S"; break;
                    case "Armadura": icono = "D"; break;
                    case "Pocima": icono = "P"; break;
                    case "Comida": icono = "C"; break;
                    case "Habilidad": icono = "H"; break;
                }

                g.setColor(new Color(245, 216, 150));
                g.setFont(new Font("SansSerif", Font.BOLD, 27));

                FontMetrics fm = g.getFontMetrics();
                g.drawString(
                    icono,
                    x + (tamano - fm.stringWidth(icono)) / 2,
                    y + 39
                );

                g.setFont(new Font("SansSerif", Font.BOLD, 12));
            }
        }

        g.setStroke(new BasicStroke(1));

        if (slotSeleccionado < objetos.size()) {

            Item seleccionado = objetos.get(slotSeleccionado);

            String nombre = seleccionado.getNombre();

            g.setFont(new Font("SansSerif", Font.BOLD, 15));
            FontMetrics fm = g.getFontMetrics();

            int textoX = (ANCHO - fm.stringWidth(nombre)) / 2;

            g.setColor(new Color(255, 230, 165));
            g.drawString(nombre, textoX, inicioY - 12);
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        dibujarFondo(g2);
        Graphics2D mundo = (Graphics2D) g2.create();
        mundo.translate(-camaraX, 0);
        dibujarEscenario(mundo);
        dibujarMonedas(mundo);
        dibujarCofre(mundo);
        dibujarMeta(mundo);
        Graphics2D graficoCaballero = (Graphics2D) mundo.create();

        dibujarCaballero(graficoCaballero);

        graficoCaballero.dispose();

        dibujarEnemigo(mundo);
        mundo.dispose();
        dibujarHUD(g2);
        dibujarHotbar(g2);
        dibujarNarrativa(g2);
        if (efectoDanio > 0) {
            g2.setColor(new Color(190, 20, 25, Math.min(75, efectoDanio * 4)));
            g2.fillRect(0, 0, ANCHO, ALTO);
        }
        if (pausado) dibujarPausa(g2);
        g2.dispose();
       
    }

    private void dibujarInventario(Graphics2D g) {

        g.setColor(new Color(10, 12, 25, 220));
        g.fillRect(0, 0, ANCHO, ALTO);

        g.setColor(new Color(35, 40, 60));
        g.fillRoundRect(300, 130, 680, 450, 20, 20);

        g.setColor(new Color(245, 205, 112));
        g.setFont(new Font("SansSerif", Font.BOLD, 30));
        g.drawString("INVENTARIO", 530, 185);

        java.util.List<Item> objetos =
                juego.getInventario().obtenerItems();

        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g.setColor(Color.WHITE);

        if (objetos.isEmpty()) {
            g.drawString("No tienes objetos", 350, 250);
        }

        for (int i = 0; i < objetos.size() && i < 9; i++) {

            Item item = objetos.get(i);

            g.drawString(
                (i + 1) + ". " + item.getNombre(),
                350,
                240 + i * 30
            );
        }

        g.setColor(new Color(245, 205, 112));
        g.setFont(new Font("SansSerif", Font.BOLD, 16));

        g.drawString(
            "I: Cerrar | 1-9: Utilizar objeto",
            350,
            540
        );
        
    }

    private void dibujarFondo(Graphics2D g) {
        int cap = juego.getCapitulo();
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
        if (cap == 2) {
            for (int i = 0; i < lluviaX.length; i++) {
                int x = (lluviaX[i] + fotograma * 3) % ANCHO;
                int y = (lluviaY[i] + fotograma * 8) % ALTO;
                g.setColor(new Color(185, 202, 229, 95));
                g.drawLine(x, y, x - 9, y + 22);
            }
        }
    }

    private void dibujarEscenario(Graphics2D g) {
        int cap = juego.getCapitulo();
        g.setColor(new Color(35, 39, 54));
        g.fillRect(0, SUELO_Y, Juego.ANCHO_NIVEL, 100);
        g.setColor(cap == 1 ? new Color(90, 149, 67) : cap == 2 ? new Color(106, 120, 130) : new Color(162, 76, 85));
        g.fillRect(0, SUELO_Y - 13, Juego.ANCHO_NIVEL, 13);
        // Detalles del suelo solo en rango visible (evita trabajo por frame innecesario).
        for (int x = Math.max(0, camaraX - 50); x < Math.min(Juego.ANCHO_NIVEL, camaraX + ANCHO + 60); x += 44) {
            for (int y = SUELO_Y + 12; y < ALTO; y += 26) {
                g.setColor(new Color(88, 89, 104));
                g.drawRect(x, y, 40, 23);
            }
        }
        // El escenario cambia gradualmente durante el avance, no solo al cambiar de capitulo.
        for (int x = 300; x < Juego.ANCHO_NIVEL; x += 460) {
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

    private void dibujarMeta(Graphics2D g) {
        int x = Juego.META_X;
        boolean abierta = juego.enemigosDerrotados();
        g.setColor(new Color(40, 40, 64));
        g.fillRoundRect(x, 415, 95, 194, 13, 13);
        g.setColor(abierta ? new Color(103, 224, 180) : new Color(218, 86, 97));
        g.fillRoundRect(x + 14, 433, 67, 161, 14, 14);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 17));
        g.drawString(abierta ? "PORTAL ABIERTO" : "DERROTA A LOS ENEMIGOS", x - 110, 395);
    }

    private void dibujarMonedas(Graphics2D g) {
        for (int i = 0; i < TOTAL_MONEDAS; i++) {
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

    private void dibujarCofre(Graphics2D g) {
        int x = 2770, y = 556;
        g.setColor(new Color(68, 39, 29));
        g.fillRoundRect(x, y + 9, 75, 54, 8, 8);
        g.setColor(cofreAbierto ? new Color(99, 72, 39) : new Color(178, 94, 43));
        g.fillRoundRect(x + 5, y + (cofreAbierto ? -19 : 1), 65, 39, 8, 8);
        g.setColor(new Color(246, 189, 58));
        g.fillRect(x, y + 39, 75, 7);
        g.fillRect(x + 29, y + (cofreAbierto ? -14 : 12), 14, 27);
        if (!cofreAbierto && juego.estaActivo() && !pausado) {
            Caballero c = juego.getCaballero();
            if (Math.abs(c.getX() - x) < 115) {
                dibujarBurbuja(g, "E  ABRIR COFRE", x - 15, 495);
            }
        }
    }

    private void dibujarCaballero(Graphics2D g) {
        Caballero c = juego.getCaballero();
        if (!c.estaVivo()) return;
        int x = c.getX(), y = c.getY();
        AffineTransform transformacionOriginal = g.getTransform();

        if (!mirandoDerecha) {

            g.translate(2 * x + Caballero.ANCHO, 0);
            g.scale(-1, 1);
        }
        // Sombra sobre el suelo
        g.setColor(new Color(15, 14, 26, 100));
        g.fillOval(x - 10, SUELO_Y - 9, 66, 12);
        // Capa
        g.setColor(new Color(172, 34, 57));
        g.fillPolygon(new int[]{x + 10, x - 16, x + 14}, new int[]{y + 17, y + 64, y + 53}, 3);
        g.setColor(new Color(87, 102, 125));
        g.fillRect(x + 7, y + 46, 13, 23);
        g.fillRect(x + 26, y + 46, 13, 23);
        g.setColor(new Color(169, 185, 205));
        g.fillRoundRect(x + 5, y + 23, 37, 31, 5, 5);
        g.setColor(new Color(79, 101, 130));
        g.fillRect(x + 11, y + 31, 24, 14);
        g.setColor(new Color(205, 217, 229));
        g.fillRoundRect(x + 4, y + 2, 39, 27, 7, 7);
        g.setColor(new Color(36, 47, 63));
        g.fillRect(x + 12, y + 15, 26, 7);
        g.setColor(new Color(215, 41, 53));
        g.fillRect(x + 19, y - 8, 11, 15);
        g.fillRect(x + 10, y - 11, 16, 7);
        // Espada visible
        g.setColor(new Color(124, 85, 43));
        g.fillRect(x + 37, y + 34, 22, 6);
        g.setColor(new Color(237, 241, 246));
        g.fillPolygon(new int[]{x + 54, x + 78, x + 54}, new int[]{y + 35, y + 38, y + 42}, 3);
        if (efectoAtaque > 0 && juego.estaActivo()) {
            g.setColor(new Color(255, 237, 157, Math.min(240, efectoAtaque * 22)));
            g.setStroke(new BasicStroke(5));
            g.drawArc(x + 33, y - 15, 100, 104, -100, 115);
            g.setStroke(new BasicStroke(1));
            g.setTransform(transformacionOriginal);
        }
    }

    private void dibujarEnemigo(Graphics2D g) {
        for (Enemigo enemigo : juego.getEnemigos()) {
            if (!enemigo.estaVivo()) continue;
            int x = juego.getPosicionEnemigo(enemigo);
            if (x < camaraX - 100 || x > camaraX + ANCHO + 100) continue;
            int y = juego.getEnemigoY();
            boolean jefe = x > 3700 || enemigo instanceof Boss;
            int cap = juego.getCapitulo();
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
            int total = jefe ? (cap == 1 ? 75 : cap == 2 ? 95 : 120) : (cap == 1 ? 30 : cap == 2 ? 45 : 55);
            g.setColor(new Color(42, 35, 46));
            g.fillRoundRect(x - 10, y - 26, 76, 9, 5, 5);
            g.setColor(new Color(226, 73, 88));
            g.fillRoundRect(x - 8, y - 24, 72 * enemigo.getVida() / total, 5, 5, 5);
        }
    }

    private void dibujarHUD(Graphics2D g) {
        Caballero c = juego.getCaballero();
        g.setColor(new Color(20, 24, 40, 221));
        g.fillRoundRect(18, 18, 280, 88, 16, 16);
        g.setColor(new Color(240, 206, 128));
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.drawString("CABALLERO DE VALDORIA", 33, 41);
        g.setColor(new Color(68, 69, 80));
        g.fillRoundRect(33, 58, 248, 20, 9, 9);
        g.setColor(c.getVida() > 30 ? new Color(207, 63, 74) : new Color(235, 103, 63));
        g.fillRoundRect(33, 58, (int) (248 * Math.max(0, Math.min(100, c.getVida())) / 100.0), 20, 9, 9);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString(c.getVida() + " / 100 HP", 119, 73);

        g.setColor(new Color(20, 24, 40, 221));
        g.fillRoundRect(1085, 20, 170, 55, 14, 14);
        g.setColor(new Color(255, 190, 44));
        g.fillOval(1105, 34, 22, 27);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.drawString("x " + monedas, 1140, 55);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString("ENEMIGOS: " + juego.getEnemigosRestantes(), 1090, 96);
        g.setColor(new Color(29, 32, 47, 195));
        g.fillRoundRect(425, 81, 430, 14, 8, 8);
        g.setColor(new Color(246, 198, 101));
        g.fillRoundRect(427, 83, Math.min(426, 426 * juego.getCaballero().getX() / Juego.META_X), 10, 7, 7);

        g.setColor(new Color(20, 24, 40, 208));
        g.fillRoundRect(315, 19, 660, 50, 13, 13);
        g.setColor(new Color(248, 217, 135));
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        String titulo = juego.getTituloCapitulo();
        g.drawString(titulo, (ANCHO - g.getFontMetrics().stringWidth(titulo)) / 2, 51);

        g.setColor(new Color(18, 20, 31, 224));
        g.fillRoundRect(22, 664, 770, 38, 10, 10);
        g.setColor(new Color(239, 239, 242));
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("A/D  AVANZAR     ESPACIO  SALTAR     J  ATACAR     E  COFRE     P  PAUSA", 40, 688);
        if (avisoDuracion > 0) {
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            int w = g.getFontMetrics().stringWidth(aviso) + 50;
            g.setColor(new Color(27, 33, 44, 225));
            g.fillRoundRect((ANCHO - w) / 2, 100, w, 48, 14, 14);
            g.setColor(new Color(251, 218, 116));
            g.drawString(aviso, (ANCHO - g.getFontMetrics().stringWidth(aviso)) / 2, 131);
        }
    }

    private void dibujarBurbuja(Graphics2D g, String texto, int x, int y) {
        g.setColor(new Color(25, 28, 39, 228));
        g.fillRoundRect(x, y, 170, 39, 12, 12);
        g.setColor(new Color(249, 215, 113));
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString(texto, x + 18, y + 25);
    }

    private void dibujarNarrativa(Graphics2D g) {
        String mensaje = juego.getMensajeHistoria();
        if (mensaje.isEmpty()) return;
        g.setColor(new Color(10, 13, 25, 190));
        g.fillRect(0, 0, ANCHO, ALTO);
        g.setColor(new Color(25, 29, 47, 247));
        g.fillRoundRect(180, 240, 920, 190, 22, 22);
        g.setColor(new Color(224, 184, 97));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(180, 240, 920, 190, 22, 22);
        g.setFont(new Font("SansSerif", Font.BOLD, 29));
        String cabecera = juego.getEstado() == Juego.Estado.VICTORIA ? "¡VICTORIA!" :
                juego.getEstado() == Juego.Estado.DERROTA ? "FIN DE LA PARTIDA" :
                juego.getEstado() == Juego.Estado.ENTRE_CAPITULOS ? "CAPITULO COMPLETADO" : "LA FORTALEZA OSCURA";
        centrar(g, cabecera, 311);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 19));
        centrar(g, mensaje, 361);
        if (juego.getEstado() == Juego.Estado.INTRO || juego.getEstado() == Juego.Estado.ENTRE_CAPITULOS) {
            g.setFont(new Font("SansSerif", Font.BOLD, 15));
            g.setColor(new Color(242, 210, 127));
            centrar(g, "PULSA ENTER PARA CONTINUAR", 397);
        }
    }

    private void dibujarPausa(Graphics2D g) {
        g.setColor(new Color(7, 10, 21, 205));
        g.fillRect(0, 0, ANCHO, ALTO);
        g.setColor(new Color(30, 35, 54));
        g.fillRoundRect(320, 173, 640, 370, 22, 22);
        g.setColor(new Color(245, 205, 112));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(320, 173, 640, 370, 22, 22);
        g.setFont(new Font("SansSerif", Font.BOLD, 40));
        centrar(g, "PARTIDA EN PAUSA", 242);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 20));
        centrar(g, "A / D     Moverse", 313);
        centrar(g, "ESPACIO     Saltar", 350);
        centrar(g, "J     Atacar", 387);
        centrar(g, "E     Abrir cofre cercano", 424);
        g.setColor(new Color(245, 205, 112));
        g.setFont(new Font("SansSerif", Font.BOLD, 19));
        centrar(g, "P O ENTER PARA CONTINUAR", 488);
    }

    private void centrar(Graphics2D g, String texto, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(texto, (ANCHO - fm.stringWidth(texto)) / 2, y);
    }
}
