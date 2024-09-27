package com.yellow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

	public class IngresoIngrediente extends JFrame {

	    // Componentes
	    private JLabel labelNombre, labelCantidad, labelCosto;
	    private JTextField fieldNombre, fieldCantidad, fieldCosto;
	    private JButton buttonGuardar, buttonCancelar;

	    public IngresoIngrediente() {
	        // Configuración del JFrame
	        setTitle("Ingreso de Ingredientes");
	        setSize(400, 300);
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        setLocationRelativeTo(null);
	        setLayout(new GridLayout(4, 2, 10, 10)); // Layout de rejilla con 4 filas y 2 columnas

	        // Inicialización de componentes
	        labelNombre = new JLabel("Nombre del Ingrediente:");
	        fieldNombre = new JTextField();
	        
	        labelCantidad = new JLabel("Cantidad Disponible:");
	        fieldCantidad = new JTextField();
	        
	        labelCosto = new JLabel("Costo por Unidad:");
	        fieldCosto = new JTextField();
	        
	        buttonGuardar = new JButton("Guardar");
	        buttonCancelar = new JButton("Cancelar");

	        // Añadir los componentes al JFrame
	        add(labelNombre);
	        add(fieldNombre);
	        
	        add(labelCantidad);
	        add(fieldCantidad);
	        
	        add(labelCosto);
	        add(fieldCosto);
	        
	        add(buttonGuardar);
	        add(buttonCancelar);

	        // Eventos de botones
	        buttonGuardar.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	                guardarIngrediente();
	            }
	        });

	        buttonCancelar.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	                limpiarCampos();
	            }
	        });
	    }

	    // Método para guardar el ingrediente
	    private void guardarIngrediente() {
	        String nombre = fieldNombre.getText();
	        String cantidadStr = fieldCantidad.getText();
	        String costoStr = fieldCosto.getText();

	        // Validaciones básicas
	        if (nombre.isEmpty() || cantidadStr.isEmpty() || costoStr.isEmpty()) {
	            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos.", "Error", JOptionPane.ERROR_MESSAGE);
	            return;
	        }

	        try {
	            double cantidad = Double.parseDouble(cantidadStr);
	            double costo = Double.parseDouble(costoStr);
	            
	            // Aquí puedes agregar el código para guardar el ingrediente en la base de datos
	            JOptionPane.showMessageDialog(this, "Ingrediente guardado exitosamente.");
	            limpiarCampos();
	        } catch (NumberFormatException ex) {
	            JOptionPane.showMessageDialog(this, "Cantidad y costo deben ser numéricos.", "Error", JOptionPane.ERROR_MESSAGE);
	        }
	    }

	    // Método para limpiar los campos
	    private void limpiarCampos() {
	        fieldNombre.setText("");
	        fieldCantidad.setText("");
	        fieldCosto.setText("");
	    }

	    public static void main(String[] args) {
	        // Ejecutar la aplicación
	        SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                new IngresoIngrediente().setVisible(true);
	            }
	        });
	    }
	}


