package vista;

import controlador.GestorNetStat;
import modelo.ConexionRed;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

/**
 * Ventana especializada para las 3 funciones de NetStat
 */
public class VentanaNetStat extends JFrame {
    private final GestorNetStat gestorNetStat;
    
    // Componentes de UI
    private JButton botonConexionesActivas, botonPuertosEscucha, botonEstadisticas;
    private JTabbedPane panelPestanias;
    private JTextArea areaTexto;
    private JTable tablaConexiones;
    private DefaultTableModel modeloTabla;
    private JProgressBar barraProgreso;
    private JLabel etiquetaEstado;

    public VentanaNetStat() {
        this.gestorNetStat = new GestorNetStat();
        inicializarVentana();
        configurarGestorEventos();
    }

    private void inicializarVentana() {
        setTitle("Analizador NetStat - ET 36");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout(10, 10));
        
        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(crearPanelCentral(), BorderLayout.CENTER);
        add(crearPanelInferior(), BorderLayout.SOUTH);
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Funciones NetStat"));
        
        // Crear botones con estilos distintivos
        botonConexionesActivas = crearBotonEstilizado(
            "Conexiones Activas", new Color(40, 167, 69));
        botonPuertosEscucha = crearBotonEstilizado(
            "Puertos en Escucha", new Color(0, 123, 255));
        botonEstadisticas = crearBotonEstilizado(
            "Estadísticas", new Color(108, 117, 125));
        
        // Tooltips informativos
        botonConexionesActivas.setToolTipText("Muestra todas las conexiones de red establecidas");
        botonPuertosEscucha.setToolTipText("Muestra puertos locales en espera de conexiones");
        botonEstadisticas.setToolTipText("Muestra estadísticas de protocolos de red");
        
        panel.add(botonConexionesActivas);
        panel.add(botonPuertosEscucha);
        panel.add(botonEstadisticas);
        
        return panel;
    }

    private JButton crearBotonEstilizado(String texto, Color colorFondo) {
        JButton boton = new JButton(texto);
        boton.setBackground(colorFondo);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(colorFondo.darker()),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        return boton;
    }

    private JTabbedPane crearPanelCentral() {
        panelPestanias = new JTabbedPane();
        
        // Pestaña 1: Tabla de conexiones
        JPanel pestaniaTabla = new JPanel(new BorderLayout());
        modeloTabla = new DefaultTableModel(new String[]{
            "Protocolo", "Dirección Local", "Puerto Local", 
            "Dirección Remota", "Puerto Remoto", "Estado", "PID", "Proceso"
        }, 0);
        
        tablaConexiones = new JTable(modeloTabla);
        tablaConexiones.setAutoCreateRowSorter(true);
        tablaConexiones.setFillsViewportHeight(true);
        
        // Configurar anchos de columnas
        tablaConexiones.getColumnModel().getColumn(0).setPreferredWidth(60);  // Protocolo
        tablaConexiones.getColumnModel().getColumn(1).setPreferredWidth(120); // Dir Local
        tablaConexiones.getColumnModel().getColumn(2).setPreferredWidth(80);  // Puerto Local
        tablaConexiones.getColumnModel().getColumn(3).setPreferredWidth(120); // Dir Remota
        tablaConexiones.getColumnModel().getColumn(4).setPreferredWidth(80);  // Puerto Remoto
        tablaConexiones.getColumnModel().getColumn(5).setPreferredWidth(100); // Estado
        tablaConexiones.getColumnModel().getColumn(6).setPreferredWidth(60);  // PID
        tablaConexiones.getColumnModel().getColumn(7).setPreferredWidth(150); // Proceso
        
        pestaniaTabla.add(new JScrollPane(tablaConexiones), BorderLayout.CENTER);
        panelPestanias.addTab("Conexiones", pestaniaTabla);
        
        // Pestaña 2: Área de texto para estadísticas
        JPanel pestaniaTexto = new JPanel(new BorderLayout());
        areaTexto = new JTextArea();
        areaTexto.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaTexto.setEditable(false);
        pestaniaTexto.add(new JScrollPane(areaTexto), BorderLayout.CENTER);
        panelPestanias.addTab("Estadísticas", pestaniaTexto);
        
        return panelPestanias;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout());
        
        barraProgreso = new JProgressBar();
        barraProgreso.setStringPainted(true);
        barraProgreso.setVisible(false);
        
        etiquetaEstado = new JLabel("Seleccione una función NetStat");
        etiquetaEstado.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        panel.add(barraProgreso, BorderLayout.CENTER);
        panel.add(etiquetaEstado, BorderLayout.SOUTH);
        
        return panel;
    }

    private void configurarGestorEventos() {
        // Configurar Action Listeners para los botones
        botonConexionesActivas.addActionListener(e -> ejecutarFuncionNetStat(1));
        botonPuertosEscucha.addActionListener(e -> ejecutarFuncionNetStat(2));
        botonEstadisticas.addActionListener(e -> ejecutarFuncionNetStat(3));
        
        // Configurar listener del gestor NetStat
        gestorNetStat.agregarListener(new GestorNetStat.NetStatListener() {
            @Override
            public void onNetStatIniciado(String funcion) {
                SwingUtilities.invokeLater(() -> {
                    barraProgreso.setVisible(true);
                    barraProgreso.setIndeterminate(true);
                    etiquetaEstado.setText("Ejecutando " + obtenerNombreFuncion(funcion) + "...");
                    habilitarBotones(false);
                });
            }

            @Override
            public void onConexionEncontrada(ConexionRed conexion) {
                // No necesitamos actualizar en tiempo real para evitar sobrecarga de UI
            }

            @Override
            public void onNetStatCompletado(String funcion, int totalConexiones) {
                SwingUtilities.invokeLater(() -> {
                    barraProgreso.setVisible(false);
                    barraProgreso.setIndeterminate(false);
                    etiquetaEstado.setText(String.format("%s completado - %d elementos encontrados",
                        obtenerNombreFuncion(funcion), totalConexiones));
                    habilitarBotones(true);
                });
            }

            @Override
            public void onErrorNetStat(String error) {
                SwingUtilities.invokeLater(() -> {
                    barraProgreso.setVisible(false);
                    etiquetaEstado.setText("Error: " + error);
                    habilitarBotones(true);
                    JOptionPane.showMessageDialog(VentanaNetStat.this,
                        error, "Error NetStat", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }

    private void ejecutarFuncionNetStat(int funcion) {
        new Thread(() -> {
            switch (funcion) {
                case 1 -> mostrarConexionesActivas();
                case 2 -> mostrarPuertosEscucha();
                case 3 -> mostrarEstadisticas();
            }
        }, "NetStat-Thread").start();
    }

    private void mostrarConexionesActivas() {
        List<ConexionRed> conexiones = gestorNetStat.obtenerConexionesActivas();
        SwingUtilities.invokeLater(() -> {
            modeloTabla.setRowCount(0);
            for (ConexionRed conexion : conexiones) {
                modeloTabla.addRow(new Object[]{
                    conexion.getProtocolo(),
                    conexion.getDireccionLocal(),
                    conexion.getPuertoLocal(),
                    conexion.getDireccionRemota(),
                    conexion.getPuertoRemoto(),
                    conexion.getEstado(),
                    conexion.getPid() > 0 ? conexion.getPid() : "N/A",
                    conexion.getNombreProceso()
                });
            }
            panelPestanias.setSelectedIndex(0); // Cambiar a pestaña de tabla
        });
    }

    private void mostrarPuertosEscucha() {
        List<ConexionRed> puertos = gestorNetStat.obtenerPuertosEscucha();
        SwingUtilities.invokeLater(() -> {
            modeloTabla.setRowCount(0);
            for (ConexionRed puerto : puertos) {
                modeloTabla.addRow(new Object[]{
                    puerto.getProtocolo(),
                    puerto.getDireccionLocal(),
                    puerto.getPuertoLocal(),
                    puerto.getDireccionRemota(),
                    puerto.getPuertoRemoto(),
                    puerto.getEstado(),
                    puerto.getPid() > 0 ? puerto.getPid() : "N/A",
                    puerto.getNombreProceso()
                });
            }
            panelPestanias.setSelectedIndex(0);
        });
    }

    private void mostrarEstadisticas() {
        Map<String, Integer> estadisticas = gestorNetStat.obtenerEstadisticasProtocolos();
        SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append("ESTADÍSTICAS DE PROTOCOLOS DE RED\n");
            sb.append("===================================\n\n");
            
            for (Map.Entry<String, Integer> entry : estadisticas.entrySet()) {
                sb.append(String.format("%-40s: %d\n", entry.getKey(), entry.getValue()));
            }
            
            areaTexto.setText(sb.toString());
            panelPestanias.setSelectedIndex(1); // Cambiar a pestaña de estadísticas
        });
    }

    private String obtenerNombreFuncion(String funcion) {
        return switch (funcion) {
            case "CONEXIONES_ACTIVAS" -> "Conexiones Activas";
            case "PUERTOS_ESPERA" -> "Puertos en Escucha";
            case "ESTADISTICAS_PROTOCOLOS" -> "Estadísticas de Protocolos";
            default -> "Función NetStat";
        };
    }

    private void habilitarBotones(boolean habilitar) {
        botonConexionesActivas.setEnabled(habilitar);
        botonPuertosEscucha.setEnabled(habilitar);
        botonEstadisticas.setEnabled(habilitar);
    }
}