package entidades;

import interfaces.Atacable;

public class Aliado extends Entidad {

    private final String tipoApoyo;
    private final int poderApoyo;

    private int x;
    private int y;
    private int objetivoX;

    private static final int VELOCIDAD = 3;
    private static final int DISTANCIA_SEGUIMIENTO = 90;
    private static final int X_INICIAL = 60;
    private static final int Y_INICIAL = 550;

    public Aliado(String nombre, int vida, int ataqueBase,
                  String tipoApoyo, int poderApoyo) {

        super(nombre, vida, ataqueBase);

        this.tipoApoyo = tipoApoyo;
        this.poderApoyo = poderApoyo;

        reiniciar();
    }

    @Override
    public void update() {
        if (estaVivo()) {
            moverse();
        }
    }

    @Override
    public void moverse() {

        int diferencia = objetivoX - x;

        if (Math.abs(diferencia) <= DISTANCIA_SEGUIMIENTO) {
            return;
        }

        int desplazamiento = Math.min(
            VELOCIDAD,
            Math.abs(diferencia) - DISTANCIA_SEGUIMIENTO
        );

        x += Integer.compare(diferencia, 0) * desplazamiento;
    }

    public void seguir(Caballero caballero) {

        if (caballero == null || !caballero.estaVivo()) {
            return;
        }

        objetivoX = caballero.getX();
        y = caballero.getY();

        update();
    }

    @Override
    public void atacar(Atacable objetivo) {

        if (!estaVivo() || objetivo == null) {
            return;
        }

        int danio = getAtaqueBase();

        if ("Combate".equalsIgnoreCase(tipoApoyo)) {
            danio += Math.max(0, poderApoyo);
        }

        objetivo.recibirDanio(danio);
    }

    public void ayudar(Caballero caballero) {
        seguir(caballero);
    }

    public void reiniciar() {
        x = X_INICIAL;
        y = Y_INICIAL;
        objetivoX = X_INICIAL;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
