package vista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class InterfazGrafica extends JFrame {
    private JTextField campoIPInicio, campoIPFin;
    private JButton botonEscanear, botonLimpiar;
    private JProgressBar barraProgreso;
    private JSpinner spinnerTiempoEspera;

    public InterfazGrafica() {
        setTitle("Escáner de Red - ET 36");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        configurarInterfaz();
    }

    private void configurarInterfaz() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));

        // Campos para IPs
        panel.add(new JLabel("IP Inicio:"));
        campoIPInicio = new JTextField("192.168.1.1");
        panel.add(campoIPInicio);

        panel.add(new JLabel("IP Fin:"));
        campoIPFin = new JTextField("192.168.1.10");
        panel.add(campoIPFin);

        // Tiempo de espera (ping)
        panel.add(new JLabel("Tiempo de espera (ms):"));
        spinnerTiempoEspera = new JSpinner(new SpinnerNumberModel(1000, 100, 5000, 100));
        panel.add(spinnerTiempoEspera);

        // Botones
        botonEscanear = new JButton("Escanear");
        botonEscanear.addActionListener(this::iniciarEscaneo);
        panel.add(botonEscanear);

        botonLimpiar = new JButton("Limpiar");
        botonLimpiar.addActionListener(e -> limpiarCampos());
        panel.add(botonLimpiar);

        // Barra de progreso
        barraProgreso = new JProgressBar();
        barraProgreso.setStringPainted(true);
        panel.add(barraProgreso);

        add(panel, BorderLayout.CENTER);
    }

    private void iniciarEscaneo(ActionEvent e) {
        String ipInicio = campoIPInicio.getText();
        String ipFin = campoIPFin.getText();
        int tiempoEspera = (int) spinnerTiempoEspera.getValue();
        JOptionPane.showMessageDialog(this, "Escaneando desde " + ipInicio + " hasta " + ipFin);
        // (La lógica de escaneo se agregará en la siguiente entrega)
    }

    private void limpiarCampos() {
        campoIPInicio.setText("");
        campoIPFin.setText("");
        barraProgreso.setValue(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InterfazGrafica().setVisible(true));
    }
}