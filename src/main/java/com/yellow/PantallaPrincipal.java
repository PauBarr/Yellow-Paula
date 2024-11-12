package com.yellow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.hibernate.SessionFactory;

public class PantallaPrincipal extends JFrame {

    private JPanel panel;
    private SessionFactory sessionFactory;

    public PantallaPrincipal(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;  // Guardar la referencia al SessionFactory

        // Configuración del JFrame
        setTitle("Pantalla Principal");
        setExtendedState(JFrame.MAXIMIZED_BOTH);  // Maximiza la ventana
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Crear panel principal con GridBagLayout para centrar botones
        panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 255, 200));  // Fondo amarillo claro

        // Configurar restricciones para centrar los botones
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = GridBagConstraints.RELATIVE;  // Colocar cada botón al lado del otro
        gbc.gridy = 0;  // Todos en la misma fila
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.CENTER;  // Centrar los botones
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;  // No expandir los botones

        // Botón para Nuevo Proyecto
        JButton btnNuevoProyecto = new JButton("Nuevo Proyecto");
        btnNuevoProyecto.setPreferredSize(new Dimension(150, 40));  // Tamaño preferido del botón
        btnNuevoProyecto.addActionListener(e -> {
            // Abrir la ventana de Ingreso de Recetas
            IngresoReceta ingresoReceta = new IngresoReceta(sessionFactory);  // Pasar SessionFactory
            ingresoReceta.setVisible(true);
            dispose();  // Cierra la ventana actual
        });

        // Botón para Ingreso de Ingredientes
        JButton btnIngresoIngredientes = new JButton("Ingreso de Ingredientes");
        btnIngresoIngredientes.setPreferredSize(new Dimension(150, 40));  // Tamaño preferido del botón
        btnIngresoIngredientes.addActionListener(e -> {
            IngresoIngrediente ingresoIngrediente = new IngresoIngrediente(this, sessionFactory);  // Pasar SessionFactory
            ingresoIngrediente.setVisible(true);
            dispose();  // Cierra la ventana actual
        });

        // Botón para Notas
        JButton btnNotas = new JButton("Notas");
        btnNotas.setPreferredSize(new Dimension(150, 40));  // Tamaño preferido del botón
        btnNotas.addActionListener(e -> {
            VentanaNotas ventanaNotas = new VentanaNotas();
            ventanaNotas.setVisible(true);
            dispose();  // Cierra la ventana actual
        });

        // Agregar botones al panel con las restricciones de GridBagConstraints
        panel.add(btnNuevoProyecto, gbc);
        panel.add(btnIngresoIngredientes, gbc);
        panel.add(btnNotas, gbc);

        // Agregar el panel al JFrame
        add(panel);
    }
}