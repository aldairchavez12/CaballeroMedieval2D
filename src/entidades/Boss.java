/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package entidades;

/**
 *
 * @author JuLoq
 */

import interfaces.Atacable;

public class Boss extends Enemigo {

    private int danioEspecial;
    private int fase;

    public Boss(String nombre, int vida, int ataque,
                int recompensa, int danioEspecial, int fase) {

        super(nombre, vida, ataque, recompensa);

        this.danioEspecial = danioEspecial;
        this.fase = fase;
    }

    @Override
    public void atacar(Atacable objetivo) {

        int danioTotal = ataque + danioEspecial;

        System.out.println(nombre
                + " usa un ataque especial en fase "
                + fase);

        objetivo.recibirDanio(danioTotal);
    }
}
