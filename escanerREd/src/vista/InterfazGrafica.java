package vista;

import controlador.EscanerRed;
import modelo.Dispositivo;
import util.ValidadorIP;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.io.*;

public class InterfazGrafica extends JFrame {
    private JTextField campoIPInicio, campoIPFin;
    private JButton botonEscanear, botonLimpiar, botonGuardar;
    private JProgressBar barraProgreso;
    private JSpinner spinnerTiempoEspera;
    private JTable tablaResultados, tablaNetstat;
    private DefaultTableModel modeloTabla, modeloNetstat;
    private List<Dispositivo> dispositivosEncontrados;

    public InterfazGrafica() {
        setTitle("Escáner de Red - ET 36");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        configurarInterfaz();
    }

    private void configurarInterfaz() {
        // Panel principal con pestañas
        JTabbedPane panelPestanas = new JTabbedPane();
        
        // Pestaña 1: Escáner de Red
        panelPestanas.addTab("Escáner de Red", crearPanelEscanner());
        
        // Pestaña 2: Netstat
        panelPestanas.addTab("Netstat", crearPanelNetstat());
        
        add(panelPestanas);
    }

    private JPanel crearPanelEscanner() {
        JPanel panelEscanner = new JPanel(new BorderLayout(10, 10));
        
        JPanel panelSuperior = new JPanel(new GridLayout(2, 2, 10, 10));
        
        panelSuperior.add(new JLabel("IP Inicio:"));
        campoIPInicio = new JTextField("192.168.1.1");
        panelSuperior.add(campoIPInicio);

        panelSuperior.add(new JLabel("IP Fin:"));
        campoIPFin = new JTextField("192.168.1.10");
        panelSuperior.add(campoIPFin);

        JPanel panelConfig = new JPanel(new GridLayout(1, 2, 10, 10));
        panelConfig.add(new JLabel("Tiempo de espera (ms):"));
        spinnerTiempoEspera = new JSpinner(new SpinnerNumberModel(1000, 100, 5000, 100));
        panelConfig.add(spinnerTiempoEspera);

        JPanel panelBotones = new JPanel(new FlowLayout());
        botonEscanear = new JButton("Escanear");
        botonLimpiar = new JButton("Limpiar");
        botonGuardar = new JButton("Guardar Resultados");
        
        botonEscanear.addActionListener(this::iniciarEscaneo);
        botonLimpiar.addActionListener(e -> limpiarCampos());
        botonGuardar.addActionListener(e -> guardarResultados());
        
        panelBotones.add(botonEscanear);
        panelBotones.add(botonLimpiar);
        panelBotones.add(botonGuardar);

        barraProgreso = new JProgressBar();
        barraProgreso.setStringPainted(true);

        String[] columnas = {"IP", "Nombre", "Estado", "Tiempo (ms)"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaResultados = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaResultados);

        JPanel panelControles = new JPanel(new BorderLayout(10, 10));
        panelControles.add(panelSuperior, BorderLayout.NORTH);
        panelControles.add(panelConfig, BorderLayout.CENTER);
        panelControles.add(panelBotones, BorderLayout.SOUTH);

        panelEscanner.add(panelControles, BorderLayout.NORTH);
        panelEscanner.add(barraProgreso, BorderLayout.CENTER);
        panelEscanner.add(scrollPane, BorderLayout.SOUTH);
        
        return panelEscanner;
    }

    private JPanel crearPanelNetstat() {
        JPanel panelNetstat = new JPanel(new BorderLayout(10, 10));
        
        // Solo la tabla sin botones
        String[] columnasNetstat = {"Protocolo", "Dirección Local", "Dirección Remota", "Estado"};
        modeloNetstat = new DefaultTableModel(columnasNetstat, 0);
        tablaNetstat = new JTable(modeloNetstat);
        JScrollPane scrollPaneNetstat = new JScrollPane(tablaNetstat);
        
        // Mensaje informativo
        JLabel etiquetaInfo = new JLabel("Funcionalidad Netstat - En desarrollo", JLabel.CENTER);
        etiquetaInfo.setForeground(Color.GRAY);
        panelNetstat.add(etiquetaInfo, BorderLayout.NORTH);
        panelNetstat.add(scrollPaneNetstat, BorderLayout.CENTER);
        
        return panelNetstat;
    }

    private void iniciarEscaneo(ActionEvent e) {
        String ipInicio = campoIPInicio.getText();
        String ipFin = campoIPFin.getText();
        int tiempoEspera = (int) spinnerTiempoEspera.getValue();

        if (!validarEntradas(ipInicio, ipFin)) return;

        new Thread(() -> {
            botonEscanear.setEnabled(false);
            limpiarTabla();
            
            dispositivosEncontrados = EscanerRed.escanearRed(ipInicio, ipFin, tiempoEspera, barraProgreso);
            
            SwingUtilities.invokeLater(() -> {
                mostrarResultados();
                botonEscanear.setEnabled(true);
            });
        }).start();
    }

    private boolean validarEntradas(String ipInicio, String ipFin) {
        if (!ValidadorIP.esIPValida(ipInicio)) {
            mostrarError("IP inicial no válida");
            return false;
        }

        if (!ValidadorIP.esIPValida(ipFin)) {
            mostrarError("IP final no válida");
            return false;
        }

        if (!ValidadorIP.esRangoValido(ipInicio, ipFin)) {
            mostrarError("La IP inicial debe ser menor que la final");
            return false;
        }

        return true;
    }

    private void mostrarResultados() {
        int equiposActivos = 0;
        
        for (Dispositivo dispositivo : dispositivosEncontrados) {
            Object[] fila = {
                dispositivo.getDireccionIP(),
                dispositivo.getNombreHost(),
                dispositivo.estaEnLinea() ? "Conectado" : "Desconectado",
                dispositivo.getTiempoRespuesta()
            };
            modeloTabla.addRow(fila);
            
            if (dispositivo.estaEnLinea()) equiposActivos++;
        }
        
        JOptionPane.showMessageDialog(this, 
            "Escaneo completado!\nEquipos activos: " + equiposActivos +
            "\nTotal escaneado: " + dispositivosEncontrados.size());
    }

    private void guardarResultados() {
        if (dispositivosEncontrados == null || dispositivosEncontrados.isEmpty()) {
            mostrarError("No hay resultados para guardar");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("IP, Nombre, Estado, Tiempo (ms)");
                for (Dispositivo dispositivo : dispositivosEncontrados) {
                    writer.printf("%s, %s, %s, %d%n",
                        dispositivo.getDireccionIP(),
                        dispositivo.getNombreHost(),
                        dispositivo.estaEnLinea() ? "Conectado" : "Desconectado",
                        dispositivo.getTiempoRespuesta());
                }
                JOptionPane.showMessageDialog(this, "Resultados guardados en: " + file.getAbsolutePath());
            } catch (IOException ex) {
                mostrarError("Error al guardar: " + ex.getMessage());
            }
        }
    }

    private void limpiarCampos() {
        campoIPInicio.setText("");
        campoIPFin.setText("");
        spinnerTiempoEspera.setValue(1000);
        barraProgreso.setValue(0);
        limpiarTabla();
    }

    private void limpiarTabla() {
        modeloTabla.setRowCount(0);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InterfazGrafica().setVisible(true));
    }
}
