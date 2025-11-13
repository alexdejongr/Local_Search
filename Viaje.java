package practica;

import IA.Gasolina.Gasolinera;
import java.util.ArrayList;
import java.util.List;

public class Viaje {
    // Lista de peticiones que sirve este viaje (máximo 2)
    private List<Integer> peticionesServidas; // Guarda los IDs de las peticiones

    // Kilómetros totales del viaje (Centro -> P1 -> P2 -> Centro)
    private double kilometros;

    public Viaje() {
        this.peticionesServidas = new ArrayList<>();
        this.kilometros = 0.0;
    }

    //constructor copia viaje
    public Viaje(Viaje otro) {
        this.peticionesServidas = new ArrayList<>(otro.peticionesServidas);
        this.kilometros = otro.kilometros;
    }

    public List<Integer> getPeticionesServidas() {
        return peticionesServidas;
    }

    public double getKilometros() {
        return kilometros;
    }

    public void setKilometros(double kilometros) {
        this.kilometros = kilometros;
    }

    public void addPeticion(int idPeticion) {
        this.peticionesServidas.add(idPeticion);
    }

    public void removePeticionById(Integer idPeticion) {
        this.peticionesServidas.remove(idPeticion);
    }

    public void removePeticionByIdx(int index) {
        this.peticionesServidas.remove(index);
    }
}