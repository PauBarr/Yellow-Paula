package com.yellow;

import org.hibernate.SessionFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IngresoReceta extends JFrame {

    
    private SessionFactory sessionFactory;

    public IngresoReceta(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;  // Guardar el SessionFactory

        setTitle("Gestión de Recetas");
        setSize(400, 300);  // Tamaño de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // Centrar la ventana

        // Crear botones
        JButton btnNuevaReceta = new JButton("Nueva receta");
        JButton btnActualizarReceta = new JButton("Actualizar receta");
        JButton btnRecetas = new JButton("Recetas");

        // Botón de regreso con un ícono de flecha
        JButton btnRegresar = new JButton();
        btnRegresar.setIcon(new ImageIcon("ruta/a/tu/icono_flecha.png"));  // Reemplaza con la ruta real del ícono

        // Asignar acciones a los botones
        btnNuevaReceta.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nuevaReceta();
            }
        });

        btnActualizarReceta.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actualizarReceta();
            }
        });

        btnRecetas.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mostrarRecetas();
            }
        });

        btnRegresar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                regresarAPantallaPrincipal();
            }
        });

        // Crear el panel y agregar los botones
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1, 10, 10));  // GridLayout con 4 filas, 1 columna y espaciado
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));  // Bordes para darle espacio al contenido

        // Añadir los botones al panel
        panel.add(btnNuevaReceta);
        panel.add(btnActualizarReceta);
        panel.add(btnRecetas);
        panel.add(btnRegresar);

        // Añadir el panel a la ventana
        add(panel);

        setVisible(true);
    }

   
    private void nuevaReceta() {
        this.dispose();
        PantallaCostos ventana = new PantallaCostos(sessionFactory);  // Pasar SessionFactory a PantallaCostos
        ventana.setVisible(true);
    }

    // Acción para el botón "Actualizar receta"
    private void actualizarReceta() {
        JOptionPane.showMessageDialog(this, "Receta actualizada");
    }

    // Acción para el botón "Recetas"
    private void mostrarRecetas() {
        JOptionPane.showMessageDialog(this, "Mostrando recetas");
    }

    // Acción para el botón "Regresar"
    private void regresarAPantallaPrincipal() {
        this.dispose();  // Cerrar la ventana actual
        PantallaPrincipal pantallaPrincipal = new PantallaPrincipal(sessionFactory);  // Pasar SessionFactory a PantallaPrincipal
        pantallaPrincipal.setVisible(true);  // Mostrar la pantalla principal
    }
}