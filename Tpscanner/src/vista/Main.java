
package vista;
import vista.VentanaEscaneoRed;
import javax.swing.*;

/**
 * Punto de entrada principal - Versión simplificada y robusta
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("🚀 Iniciando Escáner de Red ET 36...");
        
        // Usar invokeLater para garantizar ejecución en EDT
        SwingUtilities.invokeLater(() -> {
            try {
                // No configuramos look and feel - usar el por defecto
                VentanaEscaneoRed ventana = new VentanaEscaneoRed();
                ventana.setVisible(true);
                System.out.println("✅ Aplicación iniciada correctamente");
                
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error al iniciar: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}