package entidades;

public abstract class Entidad {

    protected String nombre;
    protected int vida;
    protected int ataque;

    public Entidad(String nombre, int vida, int ataque) {
        this.nombre = nombre;
        this.vida = vida;
        this.ataque = ataque;
    }

    public abstract void update();

}
