package juego;

import java.util.ArrayList;
import java.util.List;

import entidades.Entidad;
import escenario.Estructura;

public class Juego {

    private List<Entidad> entidades;

    private List<Estructura> estructuras;

    private boolean activo;

    public Juego() {

        entidades = new ArrayList<>();

        estructuras = new ArrayList<>();

        activo = false;
    }

    public void iniciar() {

        activo = true;

        System.out.println("Juego iniciado");
    }

    public void actualizar() {

        if (!activo) {
            return;
        }

        for (Entidad entidad : entidades) {

            entidad.update();

        }
    }

    public void finalizar() {

        activo = false;

        System.out.println("Juego finalizado");
    }

    public boolean estaActivo() {

        return activo;
    }
}