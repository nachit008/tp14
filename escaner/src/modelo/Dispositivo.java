package modelo;

public class Dispositivo {
    private String direccionIP;
    private String nombreHost;
    private boolean enLinea;
    private long tiempoRespuesta; // en milisegundos

    // Constructor
    public Dispositivo(String direccionIP, String nombreHost, boolean enLinea, long tiempoRespuesta) {
        this.direccionIP = direccionIP;
        this.nombreHost = nombreHost;
        this.enLinea = enLinea;
        this.tiempoRespuesta = tiempoRespuesta;
    }

    // Getters (métodos para obtener datos)
    public String getDireccionIP() { return direccionIP; }
    public String getNombreHost() { return nombreHost; }
    public boolean estaEnLinea() { return enLinea; }
    public long getTiempoRespuesta() { return tiempoRespuesta; }
}
