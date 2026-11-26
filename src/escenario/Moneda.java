package escenario;

public final class Moneda {

    public static final int ANCHO = 22;
    public static final int ALTO = 28;

    private final int x;
    private final int y;
    private final int valor;

    private boolean recogida;

    public Moneda(int x, int y, int valor) {

        if (valor <= 0) {

            throw new IllegalArgumentException(
                "El valor debe ser positivo."
            );
        }

        this.x = x;
        this.y = y;
        this.valor = valor;
        this.recogida = false;
    }

    public int recoger(
            int jugadorX,
            int jugadorY,
            int jugadorAncho,
            int jugadorAlto) {

        if (recogida) return 0;

        boolean colision =
            jugadorX < x + ANCHO
            && jugadorX + jugadorAncho > x
            && jugadorY < y + ALTO
            && jugadorY + jugadorAlto > y;

        if (!colision) return 0;

        recogida = true;

        return valor;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getValor() {
        return valor;
    }

    public boolean estaRecogida() {
        return recogida;
    }
}