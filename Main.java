package practica;

import IA.Gasolina.CentrosDistribucion;
import IA.Gasolina.Gasolineras;
import aima.search.framework.Problem;
import aima.search.framework.Search;
import aima.search.framework.SearchAgent;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws Exception {
        // Fichero donde enviar los resultados (comentar las 2 lineas para terminal)
        //PrintStream ficheroResultado = new PrintStream(new FileOutputStream("resultados.txt", true));
        //System.setOut(ficheroResultado);

        // Configuración del escenario del problema
        int numCentros = 10;
        int numCamionesPorCentro = 1;
        int numGasolineras = 100;
        int semilla = 1234;
        String estrategia = "greedy"; // random o greedy
        String algoritmo = "SA"; // HC o SA

        // Parámetros SA (por defecto)
        int maxIteraciones = 50000;
        int iteracionesPorPasoTemp = 100;
        double k = 25.0;
        double lambda = 0.001;

        // Parsear argumentos de línea de comandos
        // Formato: algoritmo estrategia semilla [numCentros] [numGasolineras] [numCamionesPorCentro] [k] [lambda] [maxIter] [iterTemp]
        if (args.length > 0) {
            algoritmo = args[0]; // HC o SA
        }
        if (args.length > 1) {
            estrategia = args[1]; // random o greedy
        }
        if (args.length > 2) {
            semilla = Integer.parseInt(args[2]);
        }
        if (args.length > 3) {
            numCentros = Integer.parseInt(args[3]);
        }
        if (args.length > 4) {
            numGasolineras = Integer.parseInt(args[4]);
        }
        if (args.length > 5) {
            numCamionesPorCentro = Integer.parseInt(args[5]);
        }
        if (args.length > 6 && "SA".equals(algoritmo)) {
            k = Double.parseDouble(args[6]);
        }
        if (args.length > 7 && "SA".equals(algoritmo)) {
            lambda = Double.parseDouble(args[7]);
        }
        if (args.length > 8 && "SA".equals(algoritmo)) {
            maxIteraciones = Integer.parseInt(args[8]);
        }
        if (args.length > 9 && "SA".equals(algoritmo)) {
            iteracionesPorPasoTemp = Integer.parseInt(args[9]);
        }

        CentrosDistribucion centros = new CentrosDistribucion(
            numCentros,
            numCamionesPorCentro,
            semilla
        );
        Gasolineras gasolineras = new Gasolineras(numGasolineras, semilla);

        // Creación del estado inicial
        EstadoGasolina estadoInicial = new EstadoGasolina(
            centros,
            gasolineras,
            estrategia
        );
        System.out.println("Creado el estado inicial.");
        System.out.println(
            "Valor heurístico inicial: " +
                String.format("%.2f", estadoInicial.calcularValorHeuristico())
        );
        System.out.println("\n");
        estadoInicial.imprimirResumenSolucionI();

        // Ejecutar algoritmo seleccionado
        if ("HC".equals(algoritmo)) {
            runHillClimbing(estadoInicial);
        } else if ("SA".equals(algoritmo)) {
            runSimulatedAnnealing(estadoInicial, maxIteraciones, iteracionesPorPasoTemp, k, lambda);
        } else {
            System.out.println("Algoritmo no reconocido: " + algoritmo);
        }
    }

    /**
     * Ejecuta el algoritmo Hill Climbing e imprime los resultados.
     */
    public static void runHillClimbing(EstadoGasolina estadoInicial)
        throws Exception {
        System.out.println("Ejecutando Hill Climbing...");

        Problem problem = new Problem(
            estadoInicial,
            new GasolinaSuccessorFunction(),
            new GasolinaGoalTest(),
            new GasolinaHeuristicFunction()
        );
        Search search = new HillClimbingSearch();

        long startTime = System.currentTimeMillis();
        SearchAgent agent = new SearchAgent(problem, search);
        long endTime = System.currentTimeMillis();

        printResults(agent, search, endTime - startTime);
    }

    /**
     * Ejecuta el algoritmo Simulated Annealing e imprime los resultados.
     */
    public static void runSimulatedAnnealing(EstadoGasolina estadoInicial,
                                             int maxIteraciones,
                                             int iteracionesPorPasoTemp,
                                             double k,
                                             double lambda)
        throws Exception {
        System.out.println("Ejecutando Simulated Annealing...");
        System.out.println("Parámetros: k=" + k + ", lambda=" + lambda + 
                         ", maxIter=" + maxIteraciones + 
                         ", iterPorTemp=" + iteracionesPorPasoTemp);

        Problem problem = new Problem(
            estadoInicial,
            new GasolinaSuccessorFunctionSA(),
            new GasolinaGoalTest(),
            new GasolinaHeuristicFunction()
        );
        Search search = new SimulatedAnnealingSearch(
            maxIteraciones,
            iteracionesPorPasoTemp,
            (int) k,
            lambda
        );

        long startTime = System.currentTimeMillis();
        SearchAgent agent = new SearchAgent(problem, search);
        long endTime = System.currentTimeMillis();

        // Imprimir resultados
        printResults(agent, search, endTime - startTime);
    }

    /*
     * Imprime las métricas y el resultado final de la búsqueda.
     */
    private static void printResults(
        SearchAgent agent,
        Search search,
        long executionTime
    ) {
        System.out.println("Búsqueda finalizada.");
        System.out.println("Tiempo de ejecución: " + executionTime + " ms");

        // Obtener el estado final
        EstadoGasolina estadoFinal = (EstadoGasolina) search.getGoalState();

        if (estadoFinal != null) {
            estadoFinal.imprimirResumenSolucion();
            // Obtenemos los valores usando los métodos de la clase estado
            double valorHeuristico = estadoFinal.calcularValorHeuristico();
            double beneficioNeto = estadoFinal.calcularBeneficioNeto();

            System.out.println("\n RESULTADOS ");
            System.out.println(
                "Valor heurístico final (coste para AIMA): " +
                    String.format("%.2f", valorHeuristico)
            );
            System.out.println(
                "Beneficio Neto Real (Ganado - Coste KM): " +
                    String.format("%.2f", beneficioNeto)
            );

            // Métricas de aima
            Properties properties = agent.getInstrumentation();
            if (properties.getProperty("nodesExpanded") != null) {
                System.out.println(
                    "Nodos expandidos: " +
                        properties.getProperty("nodesExpanded")
                );
            }

            System.out.println("\n");
        } else {
            System.out.println("No se encontró una solución.");
        }
    }
}
