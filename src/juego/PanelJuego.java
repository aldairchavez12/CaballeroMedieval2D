package juego;

import entidades.*;
import escenario.Estructura;
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
    private boolean mostrarControles;
    private int fotograma;
    private int capituloVisual;
    private int camaraX;
    private int zonaVisual = -1;

    private final boolean[] monedasRecogidas = new boolean[TOTAL_MONEDAS];
    private final int[][] posicionesMonedas = {{320,565}, {950,565}, {1700,565}, {2540,565}, {3450,565}, {4450,565}};

private int avisoDuracion;
    private String aviso = "";
    private int efectoAtaque;
    private int vidaAnterior;
    private int efectoDanio;
    private int enemigoVidaAnterior;
    private final Random efectos = new Random(2026);


    private final APIClient apiClient = new APIClient();

    private final RenderizadorClima renderizadorClima =
        new RenderizadorClima(ANCHO, ALTO);

    private APIClient.Clima climaActual =
            APIClient.Clima.NUBLADO;

    private final RenderizadorInventario renderizadorInventario =
        new RenderizadorInventario(
            ANCHO, ALTO, SLOTS_HOTBAR
        );

    private final MenuJuego menuJuego =
        new MenuJuego(ANCHO, ALTO);

    private final RenderizadorEntidades renderizadorEntidades =
        new RenderizadorEntidades(ANCHO, SUELO_Y);

    private final RenderizadorEscenario renderizadorEscenario =
        new RenderizadorEscenario(
            ANCHO,
            ALTO,
            SUELO_Y,
            Juego.ANCHO_NIVEL,
            Juego.META_X
        );

    private final RenderizadorHUD renderizadorHUD =
        new RenderizadorHUD(ANCHO, ALTO);

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

        ControladorEntrada entrada =
            new ControladorEntrada(this);

        entrada.registrarTecla(
            "pressed A", "izquierdaOn",
            () -> izquierda = puedeJugar()
        );

        entrada.registrarTecla(
            "released A", "izquierdaOff",
            () -> izquierda = false
        );

        entrada.registrarTecla(
            "pressed D", "derechaOn",
            () -> derecha = puedeJugar()
        );

        entrada.registrarTecla(
            "released D", "derechaOff",
            () -> derecha = false
        );

        entrada.registrarTecla(
            "pressed SPACE", "saltar",
            () -> {
                if (puedeJugar()) {
                    juego.getCaballero().saltar();
                }
            }
        );

        entrada.registrarTecla(
            "pressed J", "atacar",
            this::ejecutarAtaque
        );

        entrada.registrarTecla(
            "pressed E", "interactuar",
            () -> {
                if (puedeJugar()) abrirCofre();
            }
        );

        entrada.registrarTecla(
            "pressed ENTER", "continuar",
            this::continuarPartida
        );

        entrada.registrarTecla(
            "pressed ESCAPE", "pausar",
            this::alternarPausa
        );

        entrada.registrarTecla(
            "pressed R", "reintentar",
            this::reintentarPartida
        );

        entrada.registrarTecla(
            "pressed X", "eliminarItem",
            this::eliminarSeleccionado
        );

        entrada.registrarTecla(
            "pressed H", "comprarCuracion",
            this::comprarCuracion
        );

        entrada.registrarTecla(
            "pressed I", "abrirInventario",
            this::alternarInventario
        );

        entrada.registrarSeleccion(
            SLOTS_HOTBAR,
            this::seleccionarObjeto
        );

        entrada.registrarRueda(
            this::desplazarSeleccion
        );

        entrada.registrarClic(
            this::procesarClic
        );
    }

    private boolean puedeJugar() {

        return juego.estaActivo()
            && !pausado
            && !inventarioAbierto;
    }

    private void detenerMovimiento() {

        izquierda = false;
        derecha = false;

        juego.getCaballero().establecerDireccion(0);
    }

    private void ejecutarAtaque() {

        if (!puedeJugar()) return;

        juego.atacarEnemigo();
        efectoAtaque = 10;
    }

    private void continuarPartida() {

        if (pausado) {

            pausado = false;
            mostrarControles = false;

        } else if (!inventarioAbierto) {

            juego.continuarHistoria();
        }

        detenerMovimiento();
        repaint();
    }

    private void alternarPausa() {

        if (!juego.estaActivo()) return;

        if (inventarioAbierto) {

            alternarInventario();
            return;
        }

        pausado = !pausado;
        mostrarControles = false;

        detenerMovimiento();
        repaint();
    }

    private void alternarInventario() {

        if (pausado || !juego.estaActivo()) return;

        inventarioAbierto = !inventarioAbierto;

        detenerMovimiento();

        if (!inventarioAbierto) {

            slotSeleccionado = Math.min(
                slotSeleccionado, SLOTS_HOTBAR - 1
            );
        }

        repaint();
    }

    private void seleccionarObjeto(int indice) {

        if (pausado || !juego.estaActivo()) return;

        slotSeleccionado = indice;
        repaint();
    }

    private void desplazarSeleccion(int desplazamiento) {

        if (pausado || !juego.estaActivo()) return;

        int capacidad = inventarioAbierto
            ? juego.getInventario().getCapacidadMaxima()
            : SLOTS_HOTBAR;

        slotSeleccionado = Math.floorMod(
            slotSeleccionado + desplazamiento,
            capacidad
        );

        repaint();
    }

    private void eliminarSeleccionado() {

        if (pausado || !juego.estaActivo()) return;

        List<Item> objetos =
            juego.getInventario().obtenerItems();

        if (slotSeleccionado < 0
                || slotSeleccionado >= objetos.size()) {
            return;
        }

        Item seleccionado =
            objetos.get(slotSeleccionado);

        if (juego.getCaballero()
                .eliminarObjeto(seleccionado)) {

            avisar("ELIMINADO: "
                + seleccionado.getNombre());

            repaint();
        }
    }

    private void comprarCuracion() {

        if (!puedeJugar()) return;

        switch (juego.comprarCuracion()) {

            case EXITOSA:

                avisar("CURACION: -"
                    + Juego.COSTO_CURACION + " MONEDAS");

                break;

            case SALDO_INSUFICIENTE:

                avisar("NECESITAS "
                    + Juego.COSTO_CURACION + " MONEDAS");

                break;

            case VIDA_COMPLETA:

                avisar("YA TIENES TODA LA VIDA");

                break;

            case NO_DISPONIBLE:

                return;
        }

        repaint();
    }

    private void reintentarPartida() {

        if (juego.getEstado() != Juego.Estado.DERROTA) {
            return;
        }

        juego.reiniciarCapitulo();

        java.util.Arrays.fill(
            monedasRecogidas, false
        );

camaraX = 0;
        zonaVisual = -1;

        pausado = false;
        mostrarControles = false;
        inventarioAbierto = false;
        slotSeleccionado = 0;

        detenerMovimiento();

        efectoAtaque = 0;
        efectoDanio = 0;

        vidaAnterior =
            juego.getCaballero().getVida();

        avisar("CAPITULO REINICIADO");

        repaint();
    }

    private void procesarClic(MouseEvent e) {

        if (procesarClicMenu(e)) return;

        if (inventarioAbierto) {

            procesarClicInventario(e);
            return;
        }

        if (!puedeJugar()) return;

        if (SwingUtilities.isLeftMouseButton(e)) {

            if (juego.usarItem(slotSeleccionado)) {

                avisar("OBJETO UTILIZADO");
            }

        } else if (SwingUtilities.isRightMouseButton(e)) {

            ejecutarAtaque();
        }

        repaint();
    }

    private void actualizarPantalla() {
        fotograma++;
        if (avisoDuracion > 0) avisoDuracion--;
        if (efectoAtaque > 0) efectoAtaque--;
        if (efectoDanio > 0) efectoDanio--;

        if (juego.getCapitulo() != capituloVisual) {
            capituloVisual = juego.getCapitulo();
            for (int i = 0; i < monedasRecogidas.length; i++) monedasRecogidas[i] = false;
            
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
                    juego.recogerMonedas(1);
                    avisar("+1 MONEDA");
                }
            }
        }
    }

private void abrirCofre() {

        if (!puedeJugar()) return;

        String resultado = juego.abrirCofreCercano();

        if (!resultado.isEmpty()) {

            avisar(resultado);
            repaint();
        }
    }

@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        renderizadorEscenario.dibujarFondo(
            g2,
            climaActual,
            juego.getCapitulo(),
            camaraX
        );
        Graphics2D mundo = (Graphics2D) g2.create();
        mundo.translate(-camaraX, 0);
        renderizadorEscenario.dibujarEscenario(
            mundo,
            juego.getCapitulo(),
            camaraX
        );
        renderizadorEscenario.dibujarEstructuras(
            mundo,
            juego.getEstructuras()
        );
        renderizadorEscenario.dibujarMonedas(
            mundo,
            posicionesMonedas,
            monedasRecogidas,
            fotograma
        );
        renderizadorEscenario.dibujarCofre(
            mundo,
            juego.getCofres(),
            juego.getCaballero().getX(),
            juego.estaActivo(),
            pausado
        );
        renderizadorEscenario.dibujarMeta(
            mundo,
            juego.enemigosDerrotados()
        );
        Graphics2D graficoCaballero = (Graphics2D) mundo.create();

        renderizadorEntidades.dibujarCaballero(
            graficoCaballero,
            juego.getCaballero(),
            efectoAtaque,
            juego.estaActivo()
        );

        graficoCaballero.dispose();
        renderizadorEntidades.dibujarAliado(
            mundo,
            juego.getGestorAliados().getAliado()
        );
        juego.getGestorProyectiles().dibujar(mundo);
        renderizadorEntidades.dibujarEnemigo(
            mundo,
            juego.getEnemigos(),
            juego::getPosicionEnemigo,
            juego.getEnemigoY(),
            juego.getCapitulo(),
            camaraX
        );
        mundo.dispose();
        renderizadorClima.dibujar(g2, climaActual, fotograma);
        
        renderizadorHUD.dibujarHUD(
            g2,
            juego.getCaballero(),
            juego.getMonedas(),
            juego.getEnemigosRestantes(),
            juego.getTituloCapitulo(),
            climaActual,
            aviso,
            avisoDuracion,
            Juego.META_X
        );
        renderizadorInventario.dibujarHotbar(
            g2,
            juego.getInventario(),
            slotSeleccionado
        );
        if (juego.getEstado() != Juego.Estado.DERROTA) {

            Juego.Estado estado = juego.getEstado();

            String tituloNarrativa =
                estado == Juego.Estado.VICTORIA
                    ? "VICTORIA"
                    : estado == Juego.Estado.ENTRE_CAPITULOS
                        ? "CAPITULO COMPLETADO"
                        : "LA FORTALEZA OSCURA";

            renderizadorHUD.dibujarNarrativa(
                g2,
                juego.getMensajeHistoria(),
                tituloNarrativa,
                estado == Juego.Estado.INTRO
                    || estado == Juego.Estado.ENTRE_CAPITULOS
            );
        }
        if (efectoDanio > 0) {
            g2.setColor(new Color(190, 20, 25, Math.min(75, efectoDanio * 4)));
            g2.fillRect(0, 0, ANCHO, ALTO);
        }
        if (inventarioAbierto && juego.estaActivo()) {
            renderizadorInventario.dibujarInventario(
            g2,
            juego.getInventario(),
            slotSeleccionado
        );
        }

        if (pausado) menuJuego.dibujarPausa(
            g2, mostrarControles, getMousePosition()
        );

        if (juego.getEstado() == Juego.Estado.DERROTA) {
            menuJuego.dibujarMenuDerrota(
            g2, getMousePosition()
        );
        }
        g2.dispose();
       
    }

private void procesarClicInventario(MouseEvent e) {

        if (pausado || !juego.estaActivo()) {
            return;
        }

        for (int i = 0; i < 12; i++) {

            Rectangle casilla = new Rectangle(
                462 + (i % 4) * 90,
                230 + (i / 4) * 90,
                80, 80
            );

            if (!casilla.contains(e.getPoint())) {
                continue;
            }

            slotSeleccionado = i;

            if (SwingUtilities.isRightMouseButton(e)) {

                if (juego.usarItem(i)) {
                    avisar("OBJETO UTILIZADO");
                }
            }

            repaint();
            return;
        }
    }

private boolean clicBoton(MouseEvent e, int y) {
        return menuJuego.contieneBoton(e.getPoint(), y);
    }

    private boolean procesarClicMenu(MouseEvent e) {

        boolean derrota =
            juego.getEstado() == Juego.Estado.DERROTA;

        if (!pausado && !derrota) {
            return false;
        }

        if (!SwingUtilities.isLeftMouseButton(e)) {
            return true;
        }

        if (derrota) {

            if (clicBoton(e, 330)) {

                getActionMap().get("reintentar")
                    .actionPerformed(
                        new ActionEvent(this, 0, "reintentar")
                    );

            } else if (clicBoton(e, 410)) {

                salirDelJuego();
            }

        } else if (mostrarControles) {

            if (clicBoton(e, 490)) {
                mostrarControles = false;
            }

        } else {

            if (clicBoton(e, 230)) {

                pausado = false;
                mostrarControles = false;

            } else if (clicBoton(e, 300)) {

                int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "Deseas reiniciar el capitulo actual?",
                    "Reiniciar capitulo",
                    JOptionPane.YES_NO_OPTION
                );

                if (respuesta == JOptionPane.YES_OPTION) {

                    juego.finalizar();

                    getActionMap().get("reintentar")
                        .actionPerformed(
                            new ActionEvent(this, 0, "reintentar")
                        );
                }

            } else if (clicBoton(e, 370)) {

                mostrarControles = true;

            } else if (clicBoton(e, 440)) {

                salirDelJuego();
            }
        }

        repaint();
        return true;
    }

    private void salirDelJuego() {

        int respuesta = JOptionPane.showConfirmDialog(
            this,
            "Deseas salir del videojuego?",
            "Salir",
            JOptionPane.YES_NO_OPTION
        );

        if (respuesta == JOptionPane.YES_OPTION) {
            gameLoop.stop();
            System.exit(0);
        }
    }

    private void centrar(Graphics2D g, String texto, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(texto, (ANCHO - fm.stringWidth(texto)) / 2, y);
    }
}
