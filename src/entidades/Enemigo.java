/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package entidades;

public class Enemigo extends Entidad {

    protected int recompensa;

    public Enemigo(String nombre, int vida, int ataque, int recompensa) {
        this.nombre = nombre;
        this.vida = vida;
        this.ataque = ataque;
        this.recompensa = recompensa;
    }

    public void moverse() {
        System.out.println(nombre + " se mueve.");
    }

    public void atacar() {
        System.out.println(nombre + " ataca con " + ataque + " de daño.");
    }

    public void recibirDanio(int danio) {
        vida -= danio;
        System.out.println(nombre + " recibe " + danio + " de daño.");
    }
    /**
     *
     * @author JuLoq
    */
}