package practica;

import aima.search.framework.HeuristicFunction;

public class GasolinaHeuristicFunction implements HeuristicFunction {

    @Override
    public double getHeuristicValue(Object state) {
        EstadoGasolina estado = (EstadoGasolina) state;
        return estado.calcularValorHeuristico();
    }
}