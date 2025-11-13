package practica;

import aima.search.framework.GoalTest;

/**
 * Clase que determina si se ha alcanzado un estado objetivo.
 * Para búsqueda local, este método siempre devuelve false, ya que el algoritmo
 * se detiene cuando encuentra un óptimo local, no un estado final predefinido.
 */
public class GasolinaGoalTest implements GoalTest {

    /**
     * Comprueba si el estado proporcionado es un estado objetivo.
     *
     * @param aState El estado a comprobar.
     * @return Siempre false para problemas de búsqueda local.
     */
    @Override
    public boolean isGoalState(Object aState) {
        // En búsqueda local, el criterio de parada no es un estado final,
        // sino la incapacidad de encontrar un estado sucesor mejor.
        return false;
    }
}