public interface Attackable {
    void takeDamage(int damage);
}

abstract class Entity implements Attackable {
    private String name;
    private int life;
    private int base_attack;

    public Entity(String name, int life, int base_attack) {
        this.name = name;
        this.life = life;
        this.base_attack = base_attack;
    }

    public abstract void update();
    public abstract void move();
    public abstract void attack(Attackable target);

    @Override
    public void takeDamage(int damage) {
        this.life -= damage;
        System.out.println(this.name + " está recibiendo " + damage + " de daño. Vida restante: " + this.life);
    }

    public boolean isAlive() {
        return this.life > 0;
    }

    public String getName() { return name; }
    public int getLife() { return life; }
    public int getBaseAttack() { return base_attack; }
}