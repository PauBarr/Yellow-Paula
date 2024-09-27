package com.yellow;


import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class PantallaPrincipal extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel panel;
	/**
	 * Create the frame.
	 */
	public PantallaPrincipal() {
		
		// Configuración del JFrame
        setTitle("Pantalla Principal");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Crear panel principal
       panel = new JPanel(new GridLayout(3, 1, 10, 10));

        // Botón para Nuevo Proyecto
        JButton btnNuevoProyecto = new JButton("Nuevo Proyecto");
        btnNuevoProyecto.addActionListener(e -> {
            // Abrir la ventana de Ingreso de Recetas
            IngresoReceta ingresoReceta = new IngresoReceta();
            ingresoReceta.setVisible(true);
        });

        // Botón para Ingreso de Ingredientes
        JButton btnIngresoIngredientes = new JButton("Ingreso de Ingredientes");
        btnIngresoIngredientes.addActionListener(e -> {
            IngresoIngrediente ingresoIngrediente = new IngresoIngrediente();
            ingresoIngrediente.setVisible(true);
        });

        // Botón para Notas
        JButton btnNotas = new JButton("Notas");
        btnNotas.addActionListener(e -> {
            // Lógica para abrir la ventana de Notas (a implementar)
            VentanaNotas ventanaNotas= new VentanaNotas();
            ventanaNotas.setVisible(true);
        });

        // Agregar botones al panel
        panel.add(btnNuevoProyecto);
        panel.add(btnIngresoIngredientes);
        panel.add(btnNotas);

        // Agregar el panel al JFrame
        add(panel);
	}

}
