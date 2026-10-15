/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package entidades;

/**
 *
 * @author JuLoq
 */

public class Boss extends Enemigo {

    private int ataqueEspecial;

    public Boss(String nombre, int vida, int ataque,
                int recompensa, int ataqueEspecial) {

        super(nombre, vida, ataque, recompensa);
        this.ataqueEspecial = ataqueEspecial;
    }

    @Override
    public void atacar() {
        int danioTotal = ataque + ataqueEspecial;

        System.out.println(nombre +
                " usa un ataque especial y causa "
                + danioTotal + " de daño.");
    }

}
