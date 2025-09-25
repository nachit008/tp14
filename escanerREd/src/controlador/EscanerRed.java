package controlador;

import modelo.Dispositivo;
import util.ValidadorIP;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class EscanerRed {
    
    public static List<Dispositivo> escanearRed(String ipInicio, String ipFin, int timeout, 
                                              javax.swing.JProgressBar barraProgreso) {
        List<Dispositivo> dispositivos = new ArrayList<>();
        
        long inicio = ValidadorIP.ipToLong(ipInicio);
        long fin = ValidadorIP.ipToLong(ipFin);
        long totalIPs = fin - inicio + 1;
        
        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<Future<Dispositivo>> futures = new ArrayList<>();
        
        for (long i = inicio; i <= fin; i++) {
            String ipActual = ValidadorIP.longToIp(i);
            futures.add(executor.submit(() -> verificarDispositivo(ipActual, timeout)));
        }
        
        try {
            for (int i = 0; i < futures.size(); i++) {
                Dispositivo dispositivo = futures.get(i).get();
                dispositivos.add(dispositivo);
                
                // Actualizar barra de progreso
                int progreso = (int) ((i + 1) * 100 / totalIPs);
                if (barraProgreso != null) {
                    barraProgreso.setValue(progreso);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
        
        return dispositivos;
    }
    
    
    private static Dispositivo verificarDispositivo(String ip, int timeout) {
        try {
            long startTime = System.currentTimeMillis();
            InetAddress address = InetAddress.getByName(ip);
            boolean alcanzable = address.isReachable(timeout);
            long endTime = System.currentTimeMillis();
            
            String nombreHost = alcanzable ? address.getHostName() : "No disponible";
            long tiempoRespuesta = alcanzable ? (endTime - startTime) : 0;
            
            return new Dispositivo(ip, nombreHost, alcanzable, tiempoRespuesta);
        } catch (Exception e) {
            return new Dispositivo(ip, "Error: " + e.getMessage(), false, 0);
        }
    }
}