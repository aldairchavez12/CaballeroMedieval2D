package juego;

import javax.swing.*;
import java.awt.event.*;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public final class ControladorEntrada {

    private final JComponent componente;

    public ControladorEntrada(JComponent componente) {

        this.componente = componente;
    }

    public void registrarTecla(
            String tecla,
            String nombre,
            Runnable accion) {

        componente.getInputMap(
            JComponent.WHEN_IN_FOCUSED_WINDOW
        ).put(
            KeyStroke.getKeyStroke(tecla),
            nombre
        );

        componente.getActionMap().put(
            nombre,
            new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    accion.run();
                }
            }
        );
    }

    public void registrarSeleccion(
            int cantidad,
            IntConsumer accion) {

        for (int i = 1; i <= cantidad; i++) {

            final int indice = i - 1;

            registrarTecla(
                "pressed " + i,
                "seleccionarSlot" + i,
                () -> accion.accept(indice)
            );
        }
    }

    public void registrarRueda(IntConsumer accion) {

        componente.addMouseWheelListener(
            e -> accion.accept(e.getWheelRotation())
        );
    }

    public void registrarClic(
            Consumer<MouseEvent> accion) {

        componente.addMouseListener(
            new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    accion.accept(e);
                }
            }
        );
    }
}