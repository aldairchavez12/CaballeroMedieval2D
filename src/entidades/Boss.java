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

    public Boss(String nombre, int vida, int ataqueBase, int recompensa, double velocidad, int danioEspecial, int fase) {
        super(nombre, vida, ataqueBase, recompensa, velocidad); // Añadido velocidad
        this.danioEspecial = danioEspecial;
        this.fase = fase;
    }

    @Override
    public void update() { // Añadido según UML
        System.out.println(getNombre() + " (Boss) está ejecutando su lógica en fase " + fase);
    }

    @Override
    public void atacar(Atacable objetivo) {
        // Lógica básica o derivación al ataque especial
        super.atacar(objetivo);
    }

    // Método explícito exigido por el diagrama UML
    public void ataqueEspecial(Atacable objetivo) {
        int danioTotal = this.getAtaqueBase() + this.danioEspecial;
        System.out.println(getNombre() + " usa un ataque especial letal en fase " + fase);
        objetivo.recibirDanio(danioTotal);
    }
}
