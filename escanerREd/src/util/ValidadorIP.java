package util;

public class ValidadorIP {
    
    public static boolean esIPValida(String ip) {
        try {
            String[] partes = ip.split("\\.");
            if (partes.length != 4) return false;
            
            for (String parte : partes) {
                int num = Integer.parseInt(parte);
                if (num < 0 || num > 255) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean esRangoValido(String ipInicio, String ipFin) {
        if (!esIPValida(ipInicio) || !esIPValida(ipFin)) return false;
        
        long numInicio = ipToLong(ipInicio);
        long numFin = ipToLong(ipFin);
        
        return numInicio <= numFin;
    }
    
    public static long ipToLong(String ip) {
        long resultado = 0;
        String[] octetos = ip.split("\\.");
        
        for (int i = 0; i < 4; i++) {
            resultado <<= 8;
            resultado |= Integer.parseInt(octetos[i]);
        }
        return resultado;
    }
    
    public static String longToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "." +
               ((ip >> 16) & 0xFF) + "." +
               ((ip >> 8) & 0xFF) + "." +
               (ip & 0xFF);
    }
}