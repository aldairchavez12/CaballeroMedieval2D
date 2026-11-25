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
    private final APIClient apiClient = new APIClient();

    private APIClient.Clima climaActual =
            APIClient.Clima.NUBLADO;

    public PanelJuego() {
        setPreferredSize(new Dimension(ANCHO, ALTO));
        setFocusable(true);
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        climaActual = APIClient.Clima.LLUVIOSO;
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
        apiClient.obtenerClima().thenAccept(clima -> {

            SwingUtilities.invokeLater(() -> {

                climaActual = clima;

                System.out.println(
                    "Clima obtenido mediante API: " + clima
                );

                repaint();
            });
        });
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
        
        input.put(KeyStroke.getKeyStroke("pressed X"), "eliminarItem");
        input.put(KeyStroke.getKeyStroke("pressed ESCAPE"),"pausar");
        
        input.put(
            KeyStroke.getKeyStroke("pressed R"),
            "reintentar"
        );

        acciones.put("reintentar", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (juego.getEstado() != Juego.Estado.DERROTA) {
                    return;
                }

                juego.reiniciarCapitulo();

                // Reiniciar elementos visuales del capítulo
                for (int i = 0; i < monedasRecogidas.length; i++) {
                    monedasRecogidas[i] = false;
                }

                cofreAbierto = false;
                monedas = 0;

                camaraX = 0;
                zonaVisual = -1;

                izquierda = false;
                derecha = false;
                pausado = false;

                efectoAtaque = 0;
                efectoDanio = 0;

                vidaAnterior = juego.getCaballero().getVida();

                avisar("CAPITULO REINICIADO");

                repaint();
            }
        });

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

            @Override
            public void actionPerformed(ActionEvent e) {

                if (juego.getEstado() != Juego.Estado.JUGANDO) {
                    return;
                }

                pausado = !pausado;

                izquierda = false;
                derecha = false;

                juego.getCaballero().establecerDireccion(0);

                repaint();
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

            if (direccion > 0) {
                mirandoDerecha = true;
            } else if (direccion < 0) {
                mirandoDerecha = false;
            }

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
    private void dibujarIconoPocima(Graphics2D g, int x, int y) {
        g.setColor(new Color(180, 230, 255));
        g.fillOval(x + 8, y + 8, 16, 18);

        g.setColor(new Color(220, 50, 80));
        g.fillOval(x + 10, y + 14, 12, 10);

        g.setColor(new Color(180, 140, 80));
        g.fillRect(x + 12, y + 4, 8, 5);
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


    private void dibujarHotbar(Graphics2D g) {

        int tamano = 44;
        int separacion = 4;
        int total = SLOTS_HOTBAR * (tamano + separacion)
                - separacion;

        int inicioX = (ANCHO - total) / 2;
        int inicioY = ALTO - 62;

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
                dibujarIconoItem(g, item, x + 12, y + 12);
                switch (tipo) {
                    case "Espada": icono = ""; break;
                    case "Arco": icono = ""; break;
                    case "Flecha": icono = ""; break;
                    case "Escudo": icono = ""; break;
                    case "Armadura": icono = ""; break;
                    case "Pocima": icono = ""; break;
                    case "Comida": icono = ""; break;
                    case "Habilidad": icono = ""; break;
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
                if (item instanceof Herramienta) {

                Herramienta herramienta = (Herramienta) item;

                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 11));

                g.drawString(
                    "D:" + herramienta.getDurabilidad(),
                    x + 5,
                    y + tamano - 5
                );
            }

            if (item instanceof Flecha) {

                Flecha flecha = (Flecha) item;

                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 11));

                g.drawString(
                    "x" + flecha.getCantidad(),
                    x + 5,
                    y + tamano - 5
                );
            }
            
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
        juego.getGestorProyectiles().dibujar(mundo);
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
        

        
        if (efectoAtaque > 0 && juego.estaActivo()) {

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
    private void dibujarClima(Graphics2D g) {

        switch (climaActual) {

            case LLUVIOSO:

                // Oscurecer ligeramente el escenario
                g.setColor(new Color(35, 50, 75, 65));
                g.fillRect(0, 0, ANCHO, ALTO);

                // Gotas de lluvia animadas
                g.setStroke(new BasicStroke(2));

                for (int i = 0; i < lluviaX.length; i++) {

                    int x = lluviaX[i];
                    int y = (lluviaY[i] + fotograma * 8) % ALTO;

                    g.setColor(new Color(190, 220, 255, 170));
                    g.drawLine(x, y, x - 7, y + 18);
                }

                g.setStroke(new BasicStroke(1));
                break;

            case NUBLADO:

                // Capa gris semitransparente
                g.setColor(new Color(55, 65, 85, 75));
                g.fillRect(0, 0, ANCHO, ALTO);
                break;

            case SOLEADO:
                // Sin superposición: conserva los colores del capítulo
                break;
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
        if (juego.getEstado() == Juego.Estado.DERROTA) {

            g.setColor(new Color(245, 205, 112));
            g.setFont(new Font("SansSerif", Font.BOLD, 20));

            centrar(g, "PRESIONA R PARA REINTENTAR", 405);
        }
        centrar(g, "ESC PARA REANUDAR", 488);
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
