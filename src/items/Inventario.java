package items;

import java.util.ArrayList;
import java.util.List;

public class Inventario {

    private List<Item> items;
    private int capacidadMaxima;

    public Inventario(int capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
        this.items = new ArrayList<>();
    }

    public boolean agregarItem(Item item) {

        if (item == null || estaLleno()) {
            return false;
        }

        items.add(item);
        return true;
    }

    public boolean eliminarItem(Item item) {

        if (item == null) {
            return false;
        }

        return items.remove(item);
    }

    public List<Item> obtenerItems() {
        return new ArrayList<>(items);
    }

    public boolean estaLleno() {
        return items.size() >= capacidadMaxima;
    }

    public int cantidadItems() {
        return items.size();
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }
}