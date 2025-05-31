// src/main/java/com/yellow/PantallaConfiguracion.java
package com.yellow;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class PantallaConfiguracion extends JFrame {

    private SessionFactory sessionFactory;
    private JFrame ventanaAnterior;

    // Se mantiene solo el constructor original, ya que no habrá pestañas que seleccionar
    public PantallaConfiguracion(JFrame ventanaAnterior, SessionFactory sessionFactory) {
        this.ventanaAnterior = ventanaAnterior;
        this.sessionFactory = sessionFactory;
        initComponents();
    }

    private void initComponents() {
        // MODIFICADO: Nuevo título para la ventana
        setTitle("Acerca de Yellow");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(255, 255, 220));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // MODIFICADO: Nuevo título para la parte superior del panel
        JLabel titleLabel = new JLabel("ACERCA DE YELLOW", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 60, 60));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // INICIO: Contenido de la información "Acerca de" como panel principal
        JPanel aboutContentPanel = new JPanel(); // Usamos un nuevo nombre para el panel de contenido
        aboutContentPanel.setLayout(new BoxLayout(aboutContentPanel, BoxLayout.Y_AXIS));
        aboutContentPanel.setBackground(Color.WHITE);
        aboutContentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel appNameLabel = new JLabel("<html><b>Nombre de la Aplicación:</b> Yellow</html>");
        appNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        appNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel versionLabel = new JLabel("<html><b>Versión:</b> 0.0.1-SNAPSHOT</html>");
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel developerLabel = new JLabel("<html><b>Desarrollado por:</b> Paula</html>");
        developerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        developerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea descriptionArea = new JTextArea(
            "Yellow es una aplicación de gestión de recetas diseñada para facilitar la administración " +
            "de ingredientes y calcular automáticamente los costos de cada receta, además de ofrecer " +
            "funcionalidades para organizar y personalizar notas, entre otras opciones."
        );
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setEditable(false);
        descriptionArea.setBackground(Color.WHITE);
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 13));
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        descriptionArea.setAlignmentX(Component.CENTER_ALIGNMENT);


        JLabel technologiesTitle = new JLabel("<html><b>Tecnologías Utilizadas:</b></html>");
        technologiesTitle.setFont(new Font("Arial", Font.BOLD, 14));
        technologiesTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea technologiesArea = new JTextArea(
            "- Java Swing: Para crear la interfaz gráfica.\n" +
            "- Hibernate: Para la persistencia y manejo de datos en MySQL.\n" +
            "- MySQL: Base de datos para almacenar ingredientes, recetas y otros datos.\n" +
            "- Apache PDFBox: Para la exportación y compartición de recetas en formato PDF.\n" +
            "- Principios SOLID: Para una arquitectura más mantenible y escalable."
        );
        technologiesArea.setWrapStyleWord(true);
        technologiesArea.setLineWrap(true);
        technologiesArea.setEditable(false);
        technologiesArea.setBackground(Color.WHITE);
        technologiesArea.setFont(new Font("Arial", Font.PLAIN, 13));
        technologiesArea.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        technologiesArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        aboutContentPanel.add(appNameLabel);
        aboutContentPanel.add(Box.createVerticalStrut(5));
        aboutContentPanel.add(versionLabel);
        aboutContentPanel.add(Box.createVerticalStrut(5));
        aboutContentPanel.add(developerLabel);
        aboutContentPanel.add(Box.createVerticalStrut(15));
        aboutContentPanel.add(descriptionArea);
        aboutContentPanel.add(Box.createVerticalStrut(15));
        aboutContentPanel.add(technologiesTitle);
        aboutContentPanel.add(Box.createVerticalStrut(5));
        aboutContentPanel.add(technologiesArea);

        mainPanel.add(aboutContentPanel, BorderLayout.CENTER); // Añadir el panel de contenido al centro
        // FIN: Contenido de la información "Acerca de"

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