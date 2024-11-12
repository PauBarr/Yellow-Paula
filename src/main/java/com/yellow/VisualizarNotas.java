package com.yellow;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class VisualizarNotas extends JFrame {
	
	private JTextArea textArea;

	public VisualizarNotas(VentanaNotas ventanaNotas) { // Recibe la instancia de VentanaNotas
		setTitle("Visualizar Notas");
		setSize(400, 300);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

		textArea = new JTextArea();
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		add(scrollPane, BorderLayout.CENTER);

		// Cargar las notas desde el archivo
		cargarNotas();

		// Crear un panel para los botones
		JPanel panelBotones = new JPanel();
		panelBotones.setLayout(new FlowLayout());

		// Botón para eliminar la nota seleccionada
		JButton btnEliminarNota = new JButton("Eliminar Nota");
		btnEliminarNota.addActionListener(e -> eliminarNota());
		panelBotones.add(btnEliminarNota);

		// Botón para regresar a VentanaNotas
		JButton btnRegresar = new JButton("Regresar");
		btnRegresar.addActionListener(e -> {
			ventanaNotas.setVisible(true); // Muestra VentanaNotas
			dispose(); // Cierra VisualizarNotas
		});
		panelBotones.add(btnRegresar);

		// Agregar el panel de botones al borde inferior
		add(panelBotones, BorderLayout.SOUTH);
	}

	private void cargarNotas() {
		File file = new File("nota.txt");
		if (!file.exists()) {
			JOptionPane.showMessageDialog(this, "El archivo nota.txt no existe.");
			return;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String linea;
			StringBuilder contenido = new StringBuilder();
			while ((linea = reader.readLine()) != null) {
				contenido.append(linea).append("\n");
			}
			textArea.setText(contenido.toString());
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "Error al leer el archivo de notas.");
			ex.printStackTrace();
		}
	}

	private void eliminarNota() {
		// Obtener el texto seleccionado en el JTextArea
		String textoSeleccionado = textArea.getSelectedText();
		if (textoSeleccionado == null) {
			JOptionPane.showMessageDialog(this, "Por favor, selecciona una nota para eliminar.");
			return;
		}

		// Leer todas las notas en una lista
		String[] notas = textArea.getText().split("\n");
		StringBuilder nuevasNotas = new StringBuilder();

		// Reescribir las notas, excluyendo la seleccionada
		for (String nota : notas) {
			if (!nota.equals(textoSeleccionado)) {
				nuevasNotas.append(nota).append("\n");
			}
		}

		// Guardar las nuevas notas en el archivo
		try (FileWriter writer = new FileWriter("nota.txt")) {
			writer.write(nuevasNotas.toString());
			JOptionPane.showMessageDialog(this, "Nota eliminada con éxito.");
			cargarNotas(); // Recargar las notas
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "Error al guardar las notas.");
			ex.printStackTrace();
		}
	}
}
