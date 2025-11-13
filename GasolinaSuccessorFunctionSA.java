package practica;

import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GasolinaSuccessorFunctionSA implements SuccessorFunction {

    @Override
    public List<Successor> getSuccessors(Object state) {
        ArrayList<Successor> successors = new ArrayList<>();
        EstadoGasolina currentState = (EstadoGasolina) state;
        Random random = new Random();

        // Elegir un operador aleatorio
        int operator = random.nextInt(3);

        // Crear un estado sucesor aplicando el operador 
        EstadoGasolina successorState = new EstadoGasolina(currentState);

        boolean success = false;
        switch (operator) {
            case 0: // Mover petición
                success = tryMovePeticion(successorState, random);
                break;
            case 1: // Intercambiar peticiones
                success = tryIntercambiarPeticiones(successorState, random);
                break;
            case 2: // Asignar petición no servida
                success = tryAsignarPeticion(successorState, random);
                break;
        }

        // Si el operador generó un estado válido, lo añadimos
        if (success && successorState.esValido()) {
            String action = "Operador " + operator;
            successors.add(new Successor(action, successorState));
        }

        return successors;
    }

    // Intenta mover una petición aleatoria a un nuevo viaje aleatorio.
    private boolean tryMovePeticion(EstadoGasolina state, Random random) {
        int cOrigen = random.nextInt(state.getNumCamiones());
        if (state.getViajesPorCamion()[cOrigen].isEmpty()) return false;

        int vOrigen = random.nextInt(state.getViajesPorCamion()[cOrigen].size());
        if (state.getViajesPorCamion()[cOrigen].get(vOrigen).getPeticionesServidas().isEmpty()) return false;

        int pIdxOrigen = random.nextInt(state.getViajesPorCamion()[cOrigen].get(vOrigen).getPeticionesServidas().size());

        int cDestino = random.nextInt(state.getNumCamiones());
        // El viaje destino puede ser uno nuevo (hasta 5)
        int vDestino = random.nextInt(5);

        // No mover al mismo sitio si el viaje solo tiene 1 petición
        if (cOrigen == cDestino && vOrigen == vDestino && state.getViajesPorCamion()[cOrigen].get(vOrigen).getPeticionesServidas().size() == 1) return false;

        // Validar capacidad del viaje destino: máximo 2 peticiones
        int sizeDestino = (vDestino < state.getViajesPorCamion()[cDestino].size())
                ? state.getViajesPorCamion()[cDestino].get(vDestino).getPeticionesServidas().size()
                : 0; // si el viaje no existe aún, capacidad disponible
        if (sizeDestino >= 2) return false;

        state.moverPeticion(cOrigen, vOrigen, pIdxOrigen, cDestino, vDestino);
        return true;
    }

    // Intenta intercambiar dos peticiones aleatorias.
    private boolean tryIntercambiarPeticiones(EstadoGasolina state, Random random) {


        int c1 = random.nextInt(state.getNumCamiones());
        if (state.getViajesPorCamion()[c1].isEmpty()) return false;
        int v1 = random.nextInt(state.getViajesPorCamion()[c1].size());
        if (state.getViajesPorCamion()[c1].get(v1).getPeticionesServidas().isEmpty()) return false;
        int p1_idx = random.nextInt(state.getViajesPorCamion()[c1].get(v1).getPeticionesServidas().size());

        int c2 = random.nextInt(state.getNumCamiones());
        if (state.getViajesPorCamion()[c2].isEmpty()) return false;
        int v2 = random.nextInt(state.getViajesPorCamion()[c2].size());
        if (state.getViajesPorCamion()[c2].get(v2).getPeticionesServidas().isEmpty()) return false;
        int p2_idx = random.nextInt(state.getViajesPorCamion()[c2].get(v2).getPeticionesServidas().size());

        state.intercambiarPeticiones(c1, v1, p1_idx, c2, v2, p2_idx);
        return true;
    }

    // Intenta asignar una petición no servida a un viaje aleatorio.
    private boolean tryAsignarPeticion(EstadoGasolina state, Random random) {
        if (state.getPeticionesNoAsignadas().isEmpty()) return false;

        int pNoAsignadaIdx = random.nextInt(state.getPeticionesNoAsignadas().size());
        int cDestino = random.nextInt(state.getNumCamiones());
        int vDestino = random.nextInt(5);

        // Validar capacidad del viaje destino: máximo 2 peticiones
        int sizeDestino = (vDestino < state.getViajesPorCamion()[cDestino].size())
                ? state.getViajesPorCamion()[cDestino].get(vDestino).getPeticionesServidas().size()
                : 0; // si no existe, se creará un viaje nuevo (capacidad 0)
        if (sizeDestino >= 2) return false;

        state.asignarPeticion(pNoAsignadaIdx, cDestino, vDestino);
        return true;
    }
}