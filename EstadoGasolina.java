package practica;

import IA.Gasolina.CentrosDistribucion;
import IA.Gasolina.Gasolinera;
import IA.Gasolina.Gasolineras;
import java.util.ArrayList;
import java.util.List;

public class EstadoGasolina {

    // Datos estáticos del problema (compartidos)
    private static CentrosDistribucion centros;
    private static Gasolineras gasolineras;
    // Peticiones: int[]={idGasolinera, diasPendiente}
    private static List<int[]> todasLasPeticiones;

    // Representación de la solución
    // Asignación de viajes a cada camión por su ID
    private List<Viaje>[] viajesPorCamion;
    // IDs de peticiones no asignadas
    private List<Integer> peticionesNoAsignadas;

    /**
     * Constructor principal. Genera un estado inicial.
     */
    public EstadoGasolina(
        CentrosDistribucion c,
        Gasolineras g,
        String estrategia
    ) {
        // Carga los datos estáticos solo una vez
        if (centros == null) {
            centros = c;
            gasolineras = g;
            inicializarPeticiones();
        }

        // Inicializa las estructuras para esta solución
        this.viajesPorCamion = new ArrayList[centros.size()];
        for (int i = 0; i < centros.size(); i++) {
            this.viajesPorCamion[i] = new ArrayList<>();
        }
        this.peticionesNoAsignadas = new ArrayList<>();

        // Genera el plan inicial según la estrategia
        if ("random".equals(estrategia)) {
            generarSolucionInicial(true);
        } else {
            generarSolucionInicialGreedy();
        }
        // Si la estrategia es simple entonces la condicion es falsa, sino es true y se elige la version aleatoria
        //generarSolucionInicial(!"simple".equals(estrategia));
    }

    /**
     * Constructor de copia. Crea una copia de otro estado
     */
    public EstadoGasolina(EstadoGasolina otro) {
        // Los datos estáticos (centros, gasolineras, etc.) no se copian.

        // Copia profunda de los viajes por camión
        this.viajesPorCamion = new ArrayList[centros.size()];
        for (int i = 0; i < centros.size(); i++) {
            this.viajesPorCamion[i] = new ArrayList<>();
            for (Viaje v : otro.viajesPorCamion[i]) {
                // Esto requiere que tengas un constructor de copia en la clase Viaje
                this.viajesPorCamion[i].add(new Viaje(v));
            }
        }

        // Copia de las peticiones no asignadas
        this.peticionesNoAsignadas = new ArrayList<>(
            otro.peticionesNoAsignadas
        );
    }

    // Procesa los datos para crear una lista plana de peticiones.
    private static void inicializarPeticiones() {
        todasLasPeticiones = new ArrayList<>();
        // Itera sobre todas las gasolineras
        for (int i = 0; i < gasolineras.size(); i++) {
            Gasolinera g = gasolineras.get(i);
            // Obtiene las peticiones de la gasolinera actual
            ArrayList<Integer> peticionesDeLaGasolinera = g.getPeticiones();

            // Crea una entrada por cada petición individual
            for (Integer dias : peticionesDeLaGasolinera) {
                // Guarda [id_gasolinera, dias_espera]
                int[] peticion = new int[] { i, dias };
                todasLasPeticiones.add(peticion);
            }
        }
    }

    // Genera una solución inicial simple: asigna cada petición al primer hueco válido.
    /**
     * Genera una solución inicial aplicando una estrategia específica.
     * @param aleatoria Si es true, las peticiones se procesan en orden aleatorio.
     * Si es false, se procesan en el orden original (greedy).
     */
    private void generarSolucionInicial(boolean aleatoria) {
        //  Crear la lista de índices de peticiones
        List<Integer> indicesPeticiones = new ArrayList<>();
        for (int i = 0; i < todasLasPeticiones.size(); i++) {
            indicesPeticiones.add(i);
        }

        //  Si la estrategia es aleatoria, barajar la lista
        if (aleatoria) {
            java.util.Collections.shuffle(indicesPeticiones);
        }

        // el resto de la lógica es la misma para ambas estrategias

        for (Integer idPeticion : indicesPeticiones) {
            boolean asignada = false;

            // Intentar añadir a un viaje existente
            for (
                int idCamion = 0;
                idCamion < centros.size() && !asignada;
                idCamion++
            ) {
                for (Viaje viajeActual : this.viajesPorCamion[idCamion]) {
                    if (viajeActual.getPeticionesServidas().size() == 1) {
                        double kmActualesCamion = calcularKmCamion(idCamion);
                        double kmViajeActual = viajeActual.getKilometros();
                        double kmNuevosViaje = calcularKmNuevosViaje(
                            viajeActual,
                            idPeticion,
                            idCamion
                        );
                        double kmTotalesHipoteticos =
                            (kmActualesCamion - kmViajeActual) + kmNuevosViaje;

                        if (kmTotalesHipoteticos <= 640.0) {
                            viajeActual.addPeticion(idPeticion);
                            viajeActual.setKilometros(kmNuevosViaje);
                            asignada = true;
                            break;
                        }
                    }
                }
            }

            //  Si no se pudo, intentar crear un nuevo viaje
            if (!asignada) {
                for (
                    int idCamion = 0;
                    idCamion < centros.size() && !asignada;
                    idCamion++
                ) {
                    if (this.viajesPorCamion[idCamion].size() < 5) {
                        Viaje viajeNuevo = new Viaje();
                        double kmNuevosViaje = calcularKmNuevosViaje(
                            viajeNuevo,
                            idPeticion,
                            idCamion
                        );
                        double kmActualesCamion = calcularKmCamion(idCamion);
                        double kmTotalesHipoteticos =
                            kmActualesCamion + kmNuevosViaje;

                        if (kmTotalesHipoteticos <= 640.0) {
                            viajeNuevo.addPeticion(idPeticion);
                            viajeNuevo.setKilometros(kmNuevosViaje);
                            this.viajesPorCamion[idCamion].add(viajeNuevo);
                            asignada = true;
                        }
                    }
                }
            }

            //  Si no hubo forma, va a no asignadas
            if (!asignada) {
                peticionesNoAsignadas.add(idPeticion);
            }
        }
    }

    /**
     * Genera una solución inicial greedy para la inserción más barata
     */
    private void generarSolucionInicialGreedy() {
        // Crear lista de índices de peticiones
        List<Integer> indicesPeticiones = new ArrayList<>();
        for (int i = 0; i < todasLasPeticiones.size(); i++) {
            indicesPeticiones.add(i);
        }

        // Ordenar los índices según los días de antigüedad de forma descendente
        indicesPeticiones.sort((idx1, idx2) -> {
            int dias1 = todasLasPeticiones.get(idx1)[1];
            int dias2 = todasLasPeticiones.get(idx2)[1];
            return Integer.compare(dias2, dias1);
        });

        // Búsqueda de la mejor inserción
        for (Integer idPeticion : indicesPeticiones) {
            int mejorIdCamion = -1;
            int mejorIdViaje = -1;
            double mejorCosteInsercion = Double.MAX_VALUE;

            for (int idCamion = 0; idCamion < centros.size(); idCamion++) {
                double kmActualesCamion = calcularKmCamion(idCamion);

                // Opción 1: Intentar añadir a un viaje existente con solo una petición
                for (
                    int idViaje = 0;
                    idViaje < this.viajesPorCamion[idCamion].size();
                    idViaje++
                ) {
                    Viaje viajeActual = this.viajesPorCamion[idCamion].get(
                        idViaje
                    );
                    if (viajeActual.getPeticionesServidas().size() == 1) {
                        double kmViajeActual = viajeActual.getKilometros();
                        double kmNuevosViaje = calcularKmNuevosViaje(
                            viajeActual,
                            idPeticion,
                            idCamion
                        );
                        double kmTotalesHipoteticos =
                            (kmActualesCamion - kmViajeActual) + kmNuevosViaje;

                        if (kmTotalesHipoteticos <= 640.0) {
                            double costeInsercion =
                                kmNuevosViaje - kmViajeActual;
                            if (costeInsercion < mejorCosteInsercion) {
                                mejorCosteInsercion = costeInsercion;
                                mejorIdCamion = idCamion;
                                mejorIdViaje = idViaje;
                            }
                        }
                    }
                }

                // Opción 2: Intentar crear un nuevo viaje
                if (this.viajesPorCamion[idCamion].size() < 5) {
                    Viaje viajeNuevoTemporal = new Viaje();
                    double kmNuevosViaje = calcularKmNuevosViaje(
                        viajeNuevoTemporal,
                        idPeticion,
                        idCamion
                    );
                    double kmTotalesHipoteticos =
                        kmActualesCamion + kmNuevosViaje;

                    if (kmTotalesHipoteticos <= 640.0) {
                        double costeInsercion = kmNuevosViaje;
                        if (costeInsercion < mejorCosteInsercion) {
                            mejorCosteInsercion = costeInsercion;
                            mejorIdCamion = idCamion;
                            mejorIdViaje = -1;
                        }
                    }
                }
            }
            // Aplicar la mejor inserción encontrada
            if (mejorIdCamion != -1) {
                // Añadir a viaje existente
                if (mejorIdViaje != -1) {
                    Viaje viajeModificado =
                        this.viajesPorCamion[mejorIdCamion].get(mejorIdViaje);
                    double kmNuevosViaje = calcularKmNuevosViaje(
                        viajeModificado,
                        idPeticion,
                        mejorIdCamion
                    );
                    viajeModificado.addPeticion(idPeticion);
                    viajeModificado.setKilometros(kmNuevosViaje);
                } else {
                    // Crear nuevo viaje
                    Viaje viajeNuevo = new Viaje();
                    double kmNuevosViaje = calcularKmNuevosViaje(
                        viajeNuevo,
                        idPeticion,
                        mejorIdCamion
                    );
                    viajeNuevo.addPeticion(idPeticion);
                    viajeNuevo.setKilometros(kmNuevosViaje);
                    this.viajesPorCamion[mejorIdCamion].add(viajeNuevo);
                }
            } else {
                // Si no se encontró ninguna inserción válida, no se asigna
                peticionesNoAsignadas.add(idPeticion);
            }
        }
    }

    /**
     * Calcula los km totales de un camión.
     */
    private double calcularKmCamion(int idCamion) {
        double totalKm = 0;
        for (Viaje v : this.viajesPorCamion[idCamion]) {
            totalKm += v.getKilometros();
        }
        return totalKm;
    }

    /**
     * Mou una petició d'un viatge a un altre.
     */
    public void moverPeticion(
        int cOrigen,
        int vOrigen,
        int pIdxOrigen,
        int cDestino,
        int vDestino
    ) {
        // Localitzar i extreure la petició
        Viaje viajeOrigen = viajesPorCamion[cOrigen].get(vOrigen);
        int idPeticion = viajeOrigen.getPeticionesServidas().get(pIdxOrigen);
        viajeOrigen.removePeticionByIdx(pIdxOrigen);

        // Afegir la petició al destí (creant el viatge si no existeix)
        while (viajesPorCamion[cDestino].size() <= vDestino) {
            viajesPorCamion[cDestino].add(new Viaje());
        }
        Viaje viajeDestino = viajesPorCamion[cDestino].get(vDestino);
        viajeDestino.addPeticion(idPeticion);

        //  Recalcular KMs i netejar viatges que s'hagin quedat buits
        recalcularKmViaje(cOrigen, vOrigen);
        recalcularKmViaje(cDestino, vDestino);
        limpiarViajesVacios(cOrigen);
    }

    /**
     * Assigna una petició no servida a un viatge concret.
     */
    public void asignarPeticion(
        int pNoAsignadaIdx,
        int cDestino,
        int vDestino
    ) {
        int idPeticion = peticionesNoAsignadas.remove(pNoAsignadaIdx);

        while (viajesPorCamion[cDestino].size() <= vDestino) {
            viajesPorCamion[cDestino].add(new Viaje());
        }
        Viaje viajeDestino = viajesPorCamion[cDestino].get(vDestino);
        viajeDestino.addPeticion(idPeticion);

        recalcularKmViaje(cDestino, vDestino);
    }

    /**
     * Comprova si l'estat actual és vàlid (cap camió supera els 640 km).
     */
    public boolean esValido() {
        for (int i = 0; i < getNumCamiones(); ++i) {
            if (calcularKmCamion(i) > 640.0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Retorna el nombre total de camions (centres de distribució).
     * @return El nombre de camions.
     */
    public int getNumCamiones() {
        return centros.size();
    }

    /**
     * Retorna la llista d'identificadors de les peticions que no han estat assignades
     * a cap viatge en la solució actual.
     * @return Una llista d'enters amb els IDs de les peticions no assignades.
     */
    public List<Integer> getPeticionesNoAsignadas() {
        return peticionesNoAsignadas;
    }

    /**
     * Retorna l'array de llistes que representa els viatges assignats a cada camió.
     * @return L'estructura de dades dels viatges.
     */
    public List<Viaje>[] getViajesPorCamion() {
        return viajesPorCamion;
    }

    /**
     * Simula el cálculo de km de un viaje al añadir una nueva petición.
     */
    private double calcularKmNuevosViaje(
        Viaje viaje,
        int idPeticion,
        int idCamion
    ) {
        int nPeticiones = viaje.getPeticionesServidas().size();

        // Coordenadas del centro (origen y destino)
        int centroX = centros.get(idCamion).getCoordX();
        int centroY = centros.get(idCamion).getCoordY();

        // Coordenadas de la petición a añadir
        int idGasolineraNueva = todasLasPeticiones.get(idPeticion)[0];
        int gasX_Nueva = gasolineras.get(idGasolineraNueva).getCoordX();
        int gasY_Nueva = gasolineras.get(idGasolineraNueva).getCoordY();

        if (nPeticiones == 0) {
            // Caso 1: el viaje está vacío. Ruta C -> G_Nueva -> C
            return (
                Utils.calcularDistancia(
                    centroX,
                    centroY,
                    gasX_Nueva,
                    gasY_Nueva
                ) *
                2
            );
        } else {
            // nPeticiones == 1
            // Caso 2: el viaje ya tiene una petición. Coordenadas de la existente.
            int idPeticionExistente = viaje.getPeticionesServidas().get(0);
            int idGasolineraExistente = todasLasPeticiones.get(
                idPeticionExistente
            )[0];
            int gasX_Existente = gasolineras
                .get(idGasolineraExistente)
                .getCoordX();
            int gasY_Existente = gasolineras
                .get(idGasolineraExistente)
                .getCoordY();

            // Ruta C -> G_Existente -> G_Nueva -> C
            int dist1 = Utils.calcularDistancia(
                centroX,
                centroY,
                gasX_Existente,
                gasY_Existente
            );
            int dist2 = Utils.calcularDistancia(
                gasX_Existente,
                gasY_Existente,
                gasX_Nueva,
                gasY_Nueva
            );
            int dist3 = Utils.calcularDistancia(
                gasX_Nueva,
                gasY_Nueva,
                centroX,
                centroY
            );
            return dist1 + dist2 + dist3;
        }
    }

    /**
     * Recalcula els kilòmetres d'un viatge específic basant-se en les seves peticions.
     */
    public void recalcularKmViaje(int idCamion, int idViaje) {
        Viaje viaje = viajesPorCamion[idCamion].get(idViaje);
        List<Integer> peticiones = viaje.getPeticionesServidas();

        // Coordenades del centre de distribució
        int centroX = centros.get(idCamion).getCoordX();
        int centroY = centros.get(idCamion).getCoordY();

        double kmTotales = 0;

        if (peticiones.size() == 1) {
            // Ruta: Centre -> Gasolinera1 -> Centre
            int idGas1 = todasLasPeticiones.get(peticiones.get(0))[0];
            int gas1X = gasolineras.get(idGas1).getCoordX();
            int gas1Y = gasolineras.get(idGas1).getCoordY();
            kmTotales =
                Utils.calcularDistancia(centroX, centroY, gas1X, gas1Y) * 2;
        } else if (peticiones.size() == 2) {
            // Ruta: Centre -> Gasolinera1 -> Gasolinera2 -> Centre
            int idGas1 = todasLasPeticiones.get(peticiones.get(0))[0];
            int gas1X = gasolineras.get(idGas1).getCoordX();
            int gas1Y = gasolineras.get(idGas1).getCoordY();

            int idGas2 = todasLasPeticiones.get(peticiones.get(1))[0];
            int gas2X = gasolineras.get(idGas2).getCoordX();
            int gas2Y = gasolineras.get(idGas2).getCoordY();

            kmTotales =
                Utils.calcularDistancia(centroX, centroY, gas1X, gas1Y) +
                Utils.calcularDistancia(gas1X, gas1Y, gas2X, gas2Y) +
                Utils.calcularDistancia(gas2X, gas2Y, centroX, centroY);
        }

        viaje.setKilometros(kmTotales);
    }

    /**
     * Calcula el valor heurístic (cost) de la solució actual.
     * Un valor més baix és millor. La fórmula és:
     * Cost = (CostTransport + CostOportunitat) - BeneficiObtingut
     */
    public double calcularValorHeuristico() {
        double costeTransporte = 0.0;
        double beneficioObtenido = 0.0;

        // Calcular Cost de Transport i Benefici Obtingut (Peticions Servides)
        for (int i = 0; i < viajesPorCamion.length; ++i) {
            for (Viaje v : viajesPorCamion[i]) {
                // Suma els kilòmetres de cada viatge
                costeTransporte += v.getKilometros();

                // Calcula el benefici per cada petició servida
                for (Integer idPeticion : v.getPeticionesServidas()) {
                    int dias = todasLasPeticiones.get(idPeticion)[1];
                    // El benefici es basa en el valor del dipòsit (1000) i un percentatge que depèn dels dies d'espera
                    int diasPendiente = todasLasPeticiones.get(idPeticion)[1];
                    double porcentajePrecio;
                    if (diasPendiente == 0) porcentajePrecio = 102.0/100.0;   
                    else{

                    porcentajePrecio =
                        (102.0 - Math.pow(2.0, diasPendiente)) / 100.0;
                    } 
                    beneficioObtenido +=
                        (1000.0 * porcentajePrecio);
                }
            }
        }
        costeTransporte *= 2.0;

        // Calcular Cost d'Oportunitat (Peticions No Ateses)
        // Assumim que la pèrdua per no atendre una petició avui és la reducció
        // de benefici que tindrem demà (un 2% menys), que són 20 per dipòsit
        double costeOportunidad = peticionesNoAsignadas.size() * 20.0;

        //  Càlcul Final (invertim el benefici per minimitzar)
        return (costeTransporte + costeOportunidad) - beneficioObtenido;
    }

    /**
     * Operador: Intercanvia dues peticions entre dos viatges.
     * @param c1 Camio de la primera petició
     * @param v1 Viatge de la primera petició
     * @param p1_idx Índex de la primera petició dins del seu viatge
     * @param c2 Camio de la segona petició
     * @param v2 Viatge de la segona petició
     * @param p2_idx Índex de la segona petició dins del seu viatge
     */
    public void intercambiarPeticiones(
        int c1,
        int v1,
        int p1_idx,
        int c2,
        int v2,
        int p2_idx
    ) {
        Viaje viaje1 = this.viajesPorCamion[c1].get(v1);
        Viaje viaje2 = this.viajesPorCamion[c2].get(v2);

        // Obtenim els IDs de les peticions abans de modificar les llistes
        int idPeticion1 = viaje1.getPeticionesServidas().get(p1_idx);
        int idPeticion2 = viaje2.getPeticionesServidas().get(p2_idx);

        // Fem l'intercanvi utilitzant set() per substituir l'element a l'índex
        viaje1.getPeticionesServidas().set(p1_idx, idPeticion2);
        viaje2.getPeticionesServidas().set(p2_idx, idPeticion1);

        // Recalculem els kilòmetres d'ambdós viatges, ja que han canviat
        recalcularKmViaje(c1, v1);

        // Només recalculem el segon si no és el mateix viatge
        if (c1 != c2 || v1 != v2) {
            recalcularKmViaje(c2, v2);
        }
    }

    /**
     * Elimina els viatges que s'han quedat buits d'un camió.
     */
    public void limpiarViajesVacios(int idCamion) {
        // Iterem a la inversa per evitar problemes d'índex en eliminar
        for (int i = viajesPorCamion[idCamion].size() - 1; i >= 0; i--) {
            if (
                viajesPorCamion[idCamion].get(i)
                    .getPeticionesServidas()
                    .isEmpty()
            ) {
                viajesPorCamion[idCamion].remove(i);
            }
        }
    }

    /**
     * Calcula el beneficio neto final de la solución, siguiendo la fórmula del enunciado:
     * (Ganancias por peticiones servidas) - (Coste total por kilómetros recorridos).
     * Este método es diferente a la función heurística, ya que no incluye el coste
     * de oportunidad de las peticiones no asignadas.
     *
     * @return El beneficio neto como un valor double.
     */
    public double calcularBeneficioNeto() {
        double beneficioBruto = 0.0;
        double costeTransporteTotal = 0.0;
        final double COSTE_POR_KM = 2.0; // Según el enunciado [cite: 94]
        final double VALOR_DEPOSITO = 1000.0; // Según el enunciado [cite: 92]

        // Recorrer todos los viajes de todos los camiones
        for (int i = 0; i < viajesPorCamion.length; ++i) {
            for (Viaje v : viajesPorCamion[i]) {
                // Acumular el coste por los kilómetros de este viaje
                costeTransporteTotal += v.getKilometros();

                //  Calcular el beneficio por cada petición servida en el viaje
                for (Integer idPeticion : v.getPeticionesServidas()) {
                    // Obtenemos los días que la petición lleva pendiente
                    int diasPendiente = todasLasPeticiones.get(idPeticion)[1];

                    // Aplicamos la fórmula del beneficio para esa petición [cite: 78, 79]
                    double porcentajePrecio;
                    if (diasPendiente == 0) porcentajePrecio = 102.0/100;   
                    else{

                    porcentajePrecio =
                        (102.0 - Math.pow(2.0, diasPendiente)) / 100.0;
                    } 

                    
                    beneficioBruto += VALOR_DEPOSITO * porcentajePrecio;
                }
            }
        }

        //  Multiplicar los km totales por el coste por km
        costeTransporteTotal *= COSTE_POR_KM;

        //  Retornar el resultado final
        return beneficioBruto - costeTransporteTotal;
    }

    public void imprimirResumenSolucion() {
        System.out.println("\n RESUMEN DE LA SOLUCIÓN FINAL ");
        for (int i = 0; i < this.getNumCamiones(); i++) {
            double totalKmCamion = 0;
            int totalPeticiones = 0;
            for (Viaje v : viajesPorCamion[i]) {
                totalKmCamion += v.getKilometros();
                totalPeticiones += v.getPeticionesServidas().size();
            }

            System.out.printf(
                "Camión %d: %d viajes, %d peticiones, %.2f km totales.\n",
                i,
                viajesPorCamion[i].size(),
                totalPeticiones,
                totalKmCamion
            );

            // Alerta si se viola una restricción
            if (totalKmCamion > 640.0) {
                System.out.println("  --> ¡ERROR! Supera los 640 km.");
            }
            if (viajesPorCamion[i].size() > 5) {
                System.out.println("  --> ¡ERROR! Supera los 5 viajes.");
            }
        }
        System.out.println("----");
    }

    public void imprimirResumenSolucionI() {
        System.out.println("\n RESUMEN DE LA SOLUCIÓN INICIAL ");
        for (int i = 0; i < this.getNumCamiones(); i++) {
            double totalKmCamion = 0;
            for (Viaje v : viajesPorCamion[i]) {
                totalKmCamion += v.getKilometros();
            }

            System.out.printf(
                "Camión %d: %d viajes, %.2f km totales.\n",
                i,
                viajesPorCamion[i].size(),
                totalKmCamion
            );

            // Alerta si se viola una restricción
            if (totalKmCamion > 640.0) {
                System.out.println("  --> ¡ERROR! Supera los 640 km.");
            }
            if (viajesPorCamion[i].size() > 5) {
                System.out.println("  --> ¡ERROR! Supera los 5 viajes.");
            }
        }
        System.out.println("----");
    }
}
