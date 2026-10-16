package items.herramientas;

import entidades.Entidad;

public class Escudo extends Herramienta {
    private int bloqueo;
    private int resistencia;

    public Escudo(String nombre, String descripcion, int durabilidad, int bloqueo, int resistencia) {
        super(nombre, descripcion, durabilidad);
        this.bloqueo = bloqueo;
        this.resistencia = resistencia;
    }

    public int getBloqueo() {
        return bloqueo;
    }

    @Override
    public void usar(Entidad usuario) {
        System.out.println(usuario.getNombre() + " bloquéa con el escudo " + nombre + ".");
        reducirDurabilidad(1);
    }
}