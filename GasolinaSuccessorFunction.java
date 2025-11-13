package practica;

import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;
import java.util.ArrayList;
import java.util.List;

/**
 * Generador de successors per a l'algorisme Hill Climbing.
 * Aquesta funció genera TOTS els estats veïns possibles mitjançant tres operadors:
 * 1. Moure una petició d'un viatge a un altre.
 * 2. Intercanviar dues peticions entre dos viatges.
 * 3. Assignar una petició no servida a un viatge.
 */
public class GasolinaSuccessorFunction implements SuccessorFunction {

    @Override
    public List<Successor> getSuccessors(Object aState) {
        ArrayList<Successor> sucesores = new ArrayList<>();
        EstadoGasolina estadoActual = (EstadoGasolina) aState;
/* 
        // OP 1: Moure una petició servida a un altre lloc 
        for (int c1 = 0; c1 < estadoActual.getNumCamiones(); c1++) {
            for (int v1 = 0; v1 < estadoActual.getViajesPorCamion()[c1].size(); v1++) {
                for (int p1_idx = 0; p1_idx < estadoActual.getViajesPorCamion()[c1].get(v1).getPeticionesServidas().size(); p1_idx++) {

                    // Intentem moure la petició a tots els altres llocs possibles
                    for (int c2 = 0; c2 < estadoActual.getNumCamiones(); c2++) {
                        for (int v2 = 0; v2 < 5; v2++) { // Un camió pot tenir fins a 5 viatges
                            if (c1 == c2 && v1 == v2) continue; // No movem una petició al seu propi viatge

                            // Només movem si el viatge de destí té espai (menys de 2 peticions)
                            int sizeDestino = (v2 < estadoActual.getViajesPorCamion()[c2].size()) ?
                                    estadoActual.getViajesPorCamion()[c2].get(v2).getPeticionesServidas().size() : 0;

                            if (sizeDestino < 2) {
                                EstadoGasolina nuevoEstado = new EstadoGasolina(estadoActual);
                                nuevoEstado.moverPeticion(c1, v1, p1_idx, c2, v2);

                                if (nuevoEstado.esValido()) { // Comprovem que cap camió excedeixi els km
                                    String action = String.format("Mover P de C%d-V%d a C%d-V%d", c1, v1, c2, v2);
                                    sucesores.add(new Successor(action, nuevoEstado));
                                }
                            }
                        }
                    }
                }
            }
        }*/

        //  OP 2: Intercanviar dues peticions 
        for (int c1 = 0; c1 < estadoActual.getNumCamiones(); c1++) {
            for (int v1 = 0; v1 < estadoActual.getViajesPorCamion()[c1].size(); v1++) {
                for (int p1_idx = 0; p1_idx < estadoActual.getViajesPorCamion()[c1].get(v1).getPeticionesServidas().size(); p1_idx++) {
                    // Seleccionem la segona petició
                    for (int c2 = c1; c2 < estadoActual.getNumCamiones(); c2++) {
                        int v2_start = (c1 == c2) ? v1 : 0;
                        for (int v2 = v2_start; v2 < estadoActual.getViajesPorCamion()[c2].size(); v2++) {
                            int p2_start = (c1 == c2 && v1 == v2) ? p1_idx + 1 : 0;
                            for (int p2_idx = p2_start; p2_idx < estadoActual.getViajesPorCamion()[c2].get(v2).getPeticionesServidas().size(); p2_idx++) {

                                EstadoGasolina nuevoEstado = new EstadoGasolina(estadoActual);
                                nuevoEstado.intercambiarPeticiones(c1, v1, p1_idx, c2, v2, p2_idx);

                                if (nuevoEstado.esValido()) {
                                    String action = String.format("Swap P(C%d-V%d) <-> P(C%d-V%d)", c1, v1, c2, v2);
                                    sucesores.add(new Successor(action, nuevoEstado));
                                }
                            }
                        }
                    }
                }
            }
        }

        // OP 3: Assignar una petició no servida 
        for (int i = 0; i < estadoActual.getPeticionesNoAsignadas().size(); i++) {
            for (int c = 0; c < estadoActual.getNumCamiones(); c++) {
                for (int v = 0; v < 5; v++) { // 5 viatges possibles
                    int sizeDestino = (v < estadoActual.getViajesPorCamion()[c].size()) ?
                            estadoActual.getViajesPorCamion()[c].get(v).getPeticionesServidas().size() : 0;

                    if (sizeDestino < 2) {
                        EstadoGasolina nuevoEstado = new EstadoGasolina(estadoActual);
                        nuevoEstado.asignarPeticion(i, c, v);
                        if (nuevoEstado.esValido()) {
                            String action = String.format("Asignar P a C%d-V%d", c, v);
                            sucesores.add(new Successor(action, nuevoEstado));
                        }
                    }
                }
            }
        }

        return sucesores;
    }
}