package practica;


/**
 * Clase de utilidades con métodos de ayuda estáticos para el proyecto.
 */
public final class Utils {

    /**
     * Constructor privado para prevenir la instanciación de la clase.
     */
    private Utils() {
        // Esta clase no debe ser instanciada.
    }

    //calcular distancia manhattan
    public static int calcularDistancia(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
}