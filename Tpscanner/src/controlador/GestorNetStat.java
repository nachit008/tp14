package controlador;

import modelo.ConexionRed;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gestor profesional de NetStat con 3 funciones específicas:
 * 1. Conexiones activas establecidas
 * 2. Puertos en escucha locales
 * 3. Estadísticas de protocolos
 */
public class GestorNetStat {
    private static final Pattern TCP_UDP_PATTERN = Pattern.compile(
        "(TCP|UDP)\\s+([^\\s]+):(\\d+)\\s+([^\\s]+):(\\d+)\\s+(\\w+)"
    );
    private static final Pattern PID_PATTERN = Pattern.compile("\\[(\\d+)\\]");
    
    private final List<NetStatListener> listeners = new CopyOnWriteArrayList<>();
    
    public interface NetStatListener {
        void onNetStatIniciado(String funcion);
        void onConexionEncontrada(ConexionRed conexion);
        void onNetStatCompletado(String funcion, int totalConexiones);
        void onErrorNetStat(String error);
    }

    public void agregarListener(NetStatListener listener) {
        listeners.add(listener);
    }

    public void removerListener(NetStatListener listener) {
        listeners.remove(listener);
    }

    /**
     * FUNCIÓN 1: Obtener conexiones activas establecidas
     */
    public List<ConexionRed> obtenerConexionesActivas() {
        notifyIniciado("CONEXIONES_ACTIVAS");
        List<ConexionRed> conexiones = new ArrayList<>();
        
        try {
            String comando = isWindows() ? "netstat -anop tcp" : "netstat -tupan";
            Process process = Runtime.getRuntime().exec(comando);
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    ConexionRed conexion = parsearLineaNetstat(line);
                    if (conexion != null && 
                        (conexion.getEstado() == ConexionRed.EstadoConexion.ESTABLISHED ||
                         conexion.getEstado() == ConexionRed.EstadoConexion.CLOSE_WAIT)) {
                        conexiones.add(conexion);
                        notifyConexionEncontrada(conexion);
                    }
                }
            }
            
            process.waitFor();
        } catch (Exception e) {
            notifyError("Error obteniendo conexiones activas: " + e.getMessage());
        }
        
        notifyCompletado("CONEXIONES_ACTIVAS", conexiones.size());
        return conexiones;
    }

    /**
     * FUNCIÓN 2: Obtener puertos en escucha locales
     */
    public List<ConexionRed> obtenerPuertosEscucha() {
        notifyIniciado("PUERTOS_ESPERA");
        List<ConexionRed> puertos = new ArrayList<>();
        
        try {
            String comando = isWindows() ? "netstat -anop tcp" : "netstat -tupan";
            Process process = Runtime.getRuntime().exec(comando);
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    ConexionRed conexion = parsearLineaNetstat(line);
                    if (conexion != null && 
                        conexion.getEstado() == ConexionRed.EstadoConexion.LISTENING &&
                        conexion.esLocal()) {
                        puertos.add(conexion);
                        notifyConexionEncontrada(conexion);
                    }
                }
            }
            
            process.waitFor();
        } catch (Exception e) {
            notifyError("Error obteniendo puertos en escucha: " + e.getMessage());
        }
        
        notifyCompletado("PUERTOS_ESPERA", puertos.size());
        return puertos;
    }

    /**
     * FUNCIÓN 3: Obtener estadísticas de protocolos
     */
    public Map<String, Integer> obtenerEstadisticasProtocolos() {
        notifyIniciado("ESTADISTICAS_PROTOCOLOS");
        Map<String, Integer> estadisticas = new HashMap<>();
        
        try {
            String comando = isWindows() ? "netstat -s" : "netstat -s";
            Process process = Runtime.getRuntime().exec(comando);
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                
                String line;
                String protocoloActual = "";
                
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    
                    if (line.startsWith("TCP") || line.startsWith("UDP")) {
                        protocoloActual = line.split(" ")[0];
                    } else if (line.contains("segments") || line.contains("packets") || 
                              line.contains("connections") || line.contains("datagrams")) {
                        String[] partes = line.split("\\s+");
                        if (partes.length >= 2) {
                            try {
                                int valor = Integer.parseInt(partes[0]);
                                String clave = protocoloActual + "_" + String.join("_", 
                                    Arrays.copyOfRange(partes, 1, partes.length));
                                estadisticas.put(clave, valor);
                            } catch (NumberFormatException e) {
                                // Ignorar líneas que no contienen números
                            }
                        }
                    }
                }
            }
            
            process.waitFor();
        } catch (Exception e) {
            notifyError("Error obteniendo estadísticas: " + e.getMessage());
        }
        
        notifyCompletado("ESTADISTICAS_PROTOCOLOS", estadisticas.size());
        return estadisticas;
    }

    private ConexionRed parsearLineaNetstat(String linea) {
        try {
            Matcher matcher = TCP_UDP_PATTERN.matcher(linea);
            if (matcher.find()) {
                ConexionRed.TipoProtocolo protocolo = ConexionRed.TipoProtocolo.valueOf(matcher.group(1));
                String dirLocal = matcher.group(2);
                int puertoLocal = Integer.parseInt(matcher.group(3));
                String dirRemota = matcher.group(4);
                int puertoRemoto = Integer.parseInt(matcher.group(5));
                ConexionRed.EstadoConexion estado = parsearEstado(matcher.group(6));
                
                // Extraer PID si está disponible
                int pid = extraerPID(linea);
                String proceso = obtenerNombreProceso(pid);
                
                return new ConexionRed(protocolo, dirLocal, puertoLocal, 
                                      dirRemota, puertoRemoto, estado, pid, proceso);
            }
        } catch (Exception e) {
            // Ignorar líneas que no se pueden parsear
        }
        return null;
    }

    private ConexionRed.EstadoConexion parsearEstado(String estadoStr) {
        try {
            return ConexionRed.EstadoConexion.valueOf(estadoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ConexionRed.EstadoConexion.ESTABLISHED; // Valor por defecto
        }
    }

    private int extraerPID(String linea) {
        Matcher matcher = PID_PATTERN.matcher(linea);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    private String obtenerNombreProceso(int pid) {
        if (pid <= 0) return "N/A";
        
        try {
            String comando = isWindows() ? "tasklist /FI \"PID eq " + pid + "\"" : "ps -p " + pid + " -o comm=";
            Process process = Runtime.getRuntime().exec(comando);
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                
                String line;
                if (isWindows()) {
                    // Saltar líneas de encabezado en Windows
                    reader.readLine();
                    reader.readLine();
                    line = reader.readLine();
                    if (line != null) {
                        String[] partes = line.split("\\s+");
                        return partes.length > 0 ? partes[0] : "N/A";
                    }
                } else {
                    line = reader.readLine();
                    return line != null ? line.trim() : "N/A";
                }
            }
        } catch (Exception e) {
            return "N/A";
        }
        return "N/A";
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private void notifyIniciado(String funcion) {
        for (NetStatListener listener : listeners) {
            listener.onNetStatIniciado(funcion);
        }
    }

    private void notifyConexionEncontrada(ConexionRed conexion) {
        for (NetStatListener listener : listeners) {
            listener.onConexionEncontrada(conexion);
        }
    }

    private void notifyCompletado(String funcion, int total) {
        for (NetStatListener listener : listeners) {
            listener.onNetStatCompletado(funcion, total);
        }
    }

    private void notifyError(String error) {
        for (NetStatListener listener : listeners) {
            listener.onErrorNetStat(error);
        }
    }
}