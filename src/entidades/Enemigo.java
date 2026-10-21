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
    protected double velocidad; // Añadido según UML

    public Enemigo(String nombre, int vida, int ataqueBase, int recompensa, double velocidad) {
        super(nombre, vida, ataqueBase); // Cambiado a ataqueBase
        this.recompensa = recompensa;
        this.velocidad = velocidad;
    }

    @Override
    public void update() {
        System.out.println(getNombre() + " patrulla la zona.");
    }

    @Override
    public void moverse() { // Añadido obligatoriamente por el UML
        System.out.println(getNombre() + " se mueve a una velocidad de " + velocidad);
    }

    @Override
    public void atacar(Atacable objetivo) {
        System.out.println(getNombre() + " ataca.");
        objetivo.recibirDanio(this.getAtaqueBase());
    }
}
