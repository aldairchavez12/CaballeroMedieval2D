class Aliado extends Entidad {
    private String tipoApoyo;
    private int poderApoyo;

    public Aliado(String nombre, int vida, int ataqueBase, String tipoApoyo, int poderApoyo) {
        super(nombre, vida, ataqueBase);
        this.tipoApoyo = tipoApoyo;
        this.poderApoyo = poderApoyo;
    }

    @Override
    public void update() {
        System.out.println("Actualizando estado del aliado...");
    }

    @Override
    public void moverse() {
        System.out.println("El aliado se está moviendo.");
    }

    @Override
    public void atacar(Atacable objetivo) {
        System.out.println("El aliado está atacando.");
        if (objetivo != null) {
            objetivo.recibirDanio(getAtaqueBase());
        }
    }

    public void ayudar(Caballero caballero) {
        System.out.println("El aliado está ayudando al caballero con " + this.tipoApoyo);
    }
}