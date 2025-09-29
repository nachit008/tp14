package vista;

import controlador.EscanerRed;
import controlador.ConfiguracionEscaneo;
import modelo.Dispositivo;
import util.ValidadorIP;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ventana principal del escáner de red con interfaz profesional
 */
public class VentanaEscaneoRed extends JFrame {
    private final ConfiguracionEscaneo configuracion;
    private EscanerRed escaner;
    
    // Componentes de UI
    private JTextField campoIpInicio, campoIpFin;
    private JSpinner spinnerTimeout;
    private JButton botonIniciar, botonDetener, botonLimpiar;
    private JProgressBar barraProgreso;
    private JLabel etiquetaEstadisticas;
    private JTable tablaResultados;
    private DefaultTableModel modeloTabla;
    
    // Métricas
    private final AtomicInteger dispositivosActivos;
    private long tiempoInicioEscaneo;

    public VentanaEscaneoRed() {
        this.configuracion = new ConfiguracionEscaneo();
        this.dispositivosActivos = new AtomicInteger(0);
        inicializarVentana();
        cargarConfiguracion();
    }

    private void inicializarVentana() {
        setTitle("Escáner de Red Profesional - ET 36");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Configurar layout principal
        setLayout(new BorderLayout(10, 10));
        
        // Agregar componentes
        add(crearPanelSuperior(), BorderLayout.NORTH);
        add(crearPanelCentral(), BorderLayout.CENTER);
        add(crearPanelInferior(), BorderLayout.SOUTH);
        
        // Configurar tabla de resultados
        configurarTabla();
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Configuración de Escaneo"));
        
        // Fila 1: Campos de IP
        JPanel filaIPs = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filaIPs.add(new JLabel("IP Inicio:"));
        campoIpInicio = new JTextField(12);
        filaIPs.add(campoIpInicio);
        
        filaIPs.add(new JLabel("IP Fin:"));
        campoIpFin = new JTextField(12);
        filaIPs.add(campoIpFin);
        
        filaIPs.add(new JLabel("Timeout (ms):"));
        spinnerTimeout = new JSpinner(new SpinnerNumberModel(1000, 100, 10000, 100));
        filaIPs.add(spinnerTimeout);
        
        // Fila 2: Botones
        JPanel filaBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botonIniciar = new JButton("Iniciar Escaneo");
        botonDetener = new JButton("Detener");
        botonLimpiar = new JButton("Limpiar");
        JButton botonNetstat = new JButton("Abrir NetStat");
        
        // Estilos de botones
        botonIniciar.setBackground(new Color(34, 139, 34));
        botonIniciar.setForeground(Color.WHITE);
        botonDetener.setBackground(new Color(220, 53, 69));
        botonDetener.setForeground(Color.WHITE);
        botonDetener.setEnabled(false);
        botonNetstat.setBackground(new Color(0, 123, 255));
        botonNetstat.setForeground(Color.WHITE);
        
        filaBotones.add(botonIniciar);
        filaBotones.add(botonDetener);
        filaBotones.add(botonLimpiar);
        filaBotones.add(botonNetstat);
        
        // Action Listeners
        botonIniciar.addActionListener(this::iniciarEscaneo);
        botonDetener.addActionListener(e -> detenerEscaneo());
        botonLimpiar.addActionListener(e -> limpiarResultados());
        botonNetstat.addActionListener(e -> abrirVentanaNetStat());
        
        panel.add(filaIPs);
        panel.add(filaBotones);
        
        return panel;
    }

    private JScrollPane crearPanelCentral() {
        // Modelo de tabla
        String[] columnas = {"IP", "Nombre Host", "Estado", "Tiempo Respuesta (ms)", "Timestamp"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaResultados = new JTable(modeloTabla);
        tablaResultados.setAutoCreateRowSorter(true);
        tablaResultados.setFillsViewportHeight(true);
        
        // Renderizado personalizado para estados
        tablaResultados.getColumnModel().getColumn(2).setCellRenderer(new EstadoCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(tablaResultados);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Resultados del Escaneo"));
        
        return scrollPane;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Barra de progreso
        barraProgreso = new JProgressBar(0, 100);
        barraProgreso.setStringPainted(true);
        barraProgreso.setForeground(new Color(0, 123, 255));
        
        // Etiqueta de estadísticas
        etiquetaEstadisticas = new JLabel("Listo para escanear");
        etiquetaEstadisticas.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        
        panel.add(barraProgreso, BorderLayout.CENTER);
        panel.add(etiquetaEstadisticas, BorderLayout.SOUTH);
        
        return panel;
    }

    private void configurarTabla() {
        // Configurar anchos de columnas
        tablaResultados.getColumnModel().getColumn(0).setPreferredWidth(120); // IP
        tablaResultados.getColumnModel().getColumn(1).setPreferredWidth(200); // Host
        tablaResultados.getColumnModel().getColumn(2).setPreferredWidth(80);  // Estado
        tablaResultados.getColumnModel().getColumn(3).setPreferredWidth(120); // Tiempo
        tablaResultados.getColumnModel().getColumn(4).setPreferredWidth(150); // Timestamp
    }

    private void iniciarEscaneo(ActionEvent e) {
        String ipInicio = campoIpInicio.getText().trim();
        String ipFin = campoIpFin.getText().trim();
        int timeout = (Integer) spinnerTimeout.getValue();
        
        // Validar IPs
        if (!ValidadorIP.esIPValida(ipInicio) || !ValidadorIP.esIPValida(ipFin)) {
            JOptionPane.showMessageDialog(this, 
                "Por favor ingrese direcciones IP válidas", 
                "Error de Validación", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Guardar configuración
        guardarConfiguracion();
        
        // Preparar UI
        botonIniciar.setEnabled(false);
        botonDetener.setEnabled(true);
        dispositivosActivos.set(0);
        tiempoInicioEscaneo = System.currentTimeMillis();
        etiquetaEstadisticas.setText("Escaneo en progreso...");
        
        // Crear y configurar escáner
        escaner = new EscanerRed(ipInicio, ipFin, timeout);
        escaner.agregarListener(new EscanerRed.EscanerRedListener() {
            @Override
            public void onEscaneoIniciado(int totalIps) {
                SwingUtilities.invokeLater(() -> {
                    barraProgreso.setValue(0);
                    etiquetaEstadisticas.setText(String.format(
                        "Escaneando %d direcciones IP...", totalIps));
                });
            }

            @Override
            public void onHostDescubierto(Dispositivo dispositivo) {
                SwingUtilities.invokeLater(() -> {
                    dispositivosActivos.incrementAndGet();
                    agregarFilaTabla(dispositivo);
                    actualizarEstadisticas();
                });
            }

            @Override
            public void onProgreso(int completados, int total, double porcentaje) {
                SwingUtilities.invokeLater(() -> {
                    barraProgreso.setValue((int) porcentaje);
                    etiquetaEstadisticas.setText(String.format(
                        "Progreso: %d/%d (%.1f%%) - Activos: %d", 
                        completados, total, porcentaje, dispositivosActivos.get()));
                });
            }

            @Override
            public void onErrorEscaneo(String ip, String error) {
                System.err.println("Error escaneando " + ip + ": " + error);
            }

            @Override
            public void onEscaneoCompletado(int totalActivos, long duracionMs) {
                SwingUtilities.invokeLater(() -> {
                    finalizarEscaneo(totalActivos, duracionMs);
                });
            }

            @Override
            public void onEscaneoCancelado() {
                SwingUtilities.invokeLater(() -> {
                    etiquetaEstadisticas.setText("Escaneo cancelado por el usuario");
                    botonIniciar.setEnabled(true);
                    botonDetener.setEnabled(false);
                });
            }
        });
        
        // Ejecutar en hilo separado
        new Thread(() -> escaner.iniciarEscaneo(), "Escaneo-Thread").start();
    }

    private void detenerEscaneo() {
        if (escaner != null && escaner.isEscaneoEnCurso()) {
            escaner.cancelarEscaneo();
        }
    }

    private void limpiarResultados() {
        modeloTabla.setRowCount(0);
        dispositivosActivos.set(0);
        barraProgreso.setValue(0);
        etiquetaEstadisticas.setText("Resultados limpiados");
    }

    private void abrirVentanaNetStat() {
        SwingUtilities.invokeLater(() -> {
            VentanaNetStat ventanaNetStat = new VentanaNetStat();
            ventanaNetStat.setVisible(true);
        });
    }

    private void agregarFilaTabla(Dispositivo dispositivo) {
        Object[] fila = {
            dispositivo.getDireccionIP(),
            dispositivo.getNombreHost(),
            dispositivo.estaEnLinea() ? "ACTIVO" : "INACTIVO",
            dispositivo.getTiempoRespuesta(),
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
        };
        modeloTabla.addRow(fila);
    }

    private void actualizarEstadisticas() {
        // Actualizado en onProgreso
    }

    private void finalizarEscaneo(int totalActivos, long duracionMs) {
        botonIniciar.setEnabled(true);
        botonDetener.setEnabled(false);
        
        double duracionSegundos = duracionMs / 1000.0;
        etiquetaEstadisticas.setText(String.format(
            "Escaneo completado - %d dispositivos activos encontrados en %.2f segundos",
            totalActivos, duracionSegundos));
        
        barraProgreso.setValue(100);
        
        JOptionPane.showMessageDialog(this,
            String.format("Escaneo completado!\n\nDispositivos activos: %d\nDuración: %.2f segundos",
                totalActivos, duracionSegundos),
            "Escaneo Finalizado",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void cargarConfiguracion() {
        String ipBase = configuracion.cargarIpBase();
        campoIpInicio.setText(ipBase + "1");
        campoIpFin.setText(ipBase + "254");
        spinnerTimeout.setValue(configuracion.cargarTiempoEspera());
    }

    private void guardarConfiguracion() {
        String ipInicio = campoIpInicio.getText().trim();
        String ipBase = ipInicio.substring(0, ipInicio.lastIndexOf('.') + 1);
        
        configuracion.guardarIpBase(ipBase);
        configuracion.guardarTiempoEspera((Integer) spinnerTimeout.getValue());
    }

    // Renderer personalizado para columna de estado
    private static class EstadoCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null) {
                String estado = value.toString();
                if ("ACTIVO".equals(estado)) {
                    c.setBackground(new Color(212, 237, 218)); // Verde claro
                    c.setForeground(new Color(21, 87, 36));    // Verde oscuro
                } else {
                    c.setBackground(new Color(248, 215, 218)); // Rojo claro
                    c.setForeground(new Color(114, 28, 36));   // Rojo oscuro
                }
            }
            
            if (isSelected) {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            }
            
            setHorizontalAlignment(CENTER);
            return c;
        }
    }
}