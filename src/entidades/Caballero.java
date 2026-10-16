class Caballero extends Entidad {
    private int experiencia;
    private Herramienta arma;
    private Herramienta defensa;

    public Caballero(String nombre, int vida, int ataqueBase, int experiencia, Herramienta arma, Herramienta defensa) {
        super(nombre, vida, ataqueBase);
        this.experiencia = experiencia;
        this.arma = arma;
        this.defensa = defensa;
    }

    @Override
    public void update() {
        System.out.println("Actualizando estado del caballero...");
    }

    @Override
    public void moverse() {
        System.out.println("El caballero se está moviendo.");
    }

    @Override
    public void atacar(Atacable objetivo) {
        System.out.println("El caballero está atacando.");
        if (objetivo != null) {
            objetivo.recibirDanio(getAtaqueBase());
        }
    }

    public void saltar() {
        System.out.println("El caballero está saltando.");
    }

    public void equiparArma(Herramienta arma) {
        this.arma = arma;
        System.out.println("El caballero ha equipado un arma.");
    }

    public void equiparDefensa(Herramienta defensa) {
        this.defensa = defensa;
        System.out.println("El caballero ha equipado una defensa.");
    }
}