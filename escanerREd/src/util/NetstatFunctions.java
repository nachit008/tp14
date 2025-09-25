package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class NetstatFunctions {
    
    public static List<String> obtenerConexionesActivas() {
        List<String> conexiones = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("netstat -an");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() > 0) {
                    conexiones.add(line);
                }
            }
            reader.close();
        } catch (IOException e) {
            conexiones.add("Error al ejecutar netstat: " + e.getMessage());
        }
        return conexiones;
    }
    
    public static List<String> obtenerConexionesEstablecidas() {
        List<String> conexiones = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("netstat -n");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("ESTABLISHED")) {
                    conexiones.add(line.trim());
                }
            }
            reader.close();
        } catch (IOException e) {
            conexiones.add("Error al ejecutar netstat: " + e.getMessage());
        }
        return conexiones;
    }
    
    public static List<String> obtenerPuertosEscuchando() {
        List<String> puertos = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("netstat -an");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("LISTENING")) {
                    puertos.add(line.trim());
                }
            }
            reader.close();
        } catch (IOException e) {
            puertos.add("Error al ejecutar netstat: " + e.getMessage());
        }
        return puertos;
    }
}