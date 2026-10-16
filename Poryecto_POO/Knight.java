class Knight extends Entity {
    private int experience;
    private Herramienta weapon;
    private Herramienta protection;

    public Knight(String name, int life, int base_attack, int experience, Herramienta weapon, Herramienta protection) {
        super(name, life, base_attack);
        this.experience = experience;
        this.weapon = weapon;
        this.protection = protection;
    }

    @Override
    public void update() {
        System.out.println("Actualizando estado del caballero...");
    }

    @Override
    public void move() {
        System.out.println("El caballero se está moviendo.");
    }

    @Override
    public void attack(Attackable target) {
        System.out.println("El caballero está atacando.");
        if (target != null) {
            target.takeDamage(getBaseAttack());
        }
    }

    public void jump() {
        System.out.println("El caballero está saltando.");
    }

    public void equipWeapon(Herramienta weapon) {
        this.weapon = weapon;
        System.out.println("El caballero ha equipado un arma.");
    }

    public void equipProtection(Herramienta protection) {
        this.protection = protection;
        System.out.println("El caballero ha equipado una defensa.");
    }
}