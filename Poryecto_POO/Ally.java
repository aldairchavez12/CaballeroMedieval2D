class Ally extends Entity {
    private String helpType;
    private int helpPower;

    public Ally(String name, int life, int base_attack, String helpType, int helpPower) {
        super(name, life, base_attack);
        this.helpType = helpType;
        this.helpPower = helpPower;
    }

    @Override
    public void update() {
        System.out.println("Actualizando estado del aliado...");
    }

    @Override
    public void move() {
        System.out.println("El aliado se está moviendo.");
    }

    @Override
    public void attack(Attackable target) {
        System.out.println("El aliado está atacando.");
        if (target != null) {
            target.takeDamage(getBaseAttack());
        }
    }

    public void helpKnight(Knight knight) {
        System.out.println("El aliado está ayudando al caballero.");
    }
}