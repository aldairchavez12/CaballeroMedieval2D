/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */



/**
 *
 * @author JuLoq
*/
package entidades;

import interfaces.Atacable;

public class Enemigo extends Entidad {

    protected int recompensa;

    public Enemigo(String nombre, int vida, int ataque, int recompensa) {
        super(nombre, vida, ataque);
        this.recompensa = recompensa;
    }

    @Override
    public void update() {
        System.out.println(nombre + " patrulla la zona.");
    }

    public void atacar(Atacable objetivo) {
        System.out.println(nombre + " ataca.");
        objetivo.recibirDanio(ataque);
    }

    public void recibirDanio(int danio) {
        vida -= danio;
        System.out.println(nombre + " recibe " + danio + " de daño.");
    }
}