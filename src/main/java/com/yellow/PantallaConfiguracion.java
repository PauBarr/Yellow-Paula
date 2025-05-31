package com.yellow;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class PantallaConfiguracion extends JFrame {

    private SessionFactory sessionFactory;
    private JFrame ventanaAnterior;

    public PantallaConfiguracion(JFrame ventanaAnterior, SessionFactory sessionFactory) {
        this.ventanaAnterior = ventanaAnterior;
        this.sessionFactory = sessionFactory;
        initComponents();
    }

    private void initComponents() {
        setTitle("Configuración - Yellow");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(255, 255, 220));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("CONFIGURACIÓN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 60, 60));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(255, 255, 220));

        JPanel generalConfigPanel = new JPanel(new BorderLayout(10, 10));
        generalConfigPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        generalConfigPanel.setBackground(Color.WHITE);
        generalConfigPanel.add(new JLabel("Contenido de configuración general futura aquí...", SwingConstants.CENTER), BorderLayout.CENTER);
        tabbedPane.addTab("General", generalConfigPanel);

        tabbedPane.addTab("Unidades de Medida", new JPanel());
        tabbedPane.addTab("Moneda y Formato", new JPanel());
        tabbedPane.addTab("Respaldo/Restauración", new JPanel());
        tabbedPane.addTab("Personalización Visual", new JPanel());
        tabbedPane.addTab("Acerca de", new JPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        JButton atrasButton = new JButton("ATRÁS");
        atrasButton.setBackground(Color.DARK_GRAY);
        atrasButton.setForeground(Color.WHITE);
        atrasButton.setFont(new Font("Arial", Font.BOLD, 14));
        atrasButton.addActionListener(e -> irAtras());

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setBackground(new Color(255, 255, 220));
        southPanel.add(atrasButton);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void irAtras() {
        if (ventanaAnterior != null) {
            ventanaAnterior.setVisible(true);
        }
        dispose();
    }
}