package interfaces;

import items.herramientas.Flecha;

@FunctionalInterface
public interface LanzadorProyectiles {

    void disparar(
        double x,
        double y,
        int direccion,
        int alcance,
        Flecha flecha,
        int bonoDanio
    );
}