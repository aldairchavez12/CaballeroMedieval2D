
package juego;

import com.google.gson.JsonObject;
import entidades.Enemigo;
import entidades.Boss;

public abstract class CreadorEnemigo {

    public abstract Enemigo crear(JsonObject datos);

    protected String nombre(JsonObject d) {
        return d.get("nombre").getAsString();
    }

    protected int entero(JsonObject d, String campo) {
        return d.get(campo).getAsInt();
    }

    protected double decimal(JsonObject d, String campo) {
        return d.get(campo).getAsDouble();
    }

    public static class CreadorNormal extends CreadorEnemigo {

        @Override
        public Enemigo crear(JsonObject d) {
            return new Enemigo(
                nombre(d),
                entero(d, "vida"),
                entero(d, "ataqueBase"),
                entero(d, "recompensa"),
                decimal(d, "velocidad")
            );
        }
    }

    public static class CreadorJefe extends CreadorEnemigo {

        @Override
        public Enemigo crear(JsonObject d) {
            return new Boss(
                nombre(d),
                entero(d, "vida"),
                entero(d, "ataqueBase"),
                entero(d, "recompensa"),
                decimal(d, "velocidad"),
                entero(d, "danioEspecial"),
                entero(d, "fase")
            );
        }
    }
}
