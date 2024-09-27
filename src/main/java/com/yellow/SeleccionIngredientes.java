package com.yellow;

import org.hibernate.Session;
import org.hibernate.Transaction;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class SeleccionIngredientes extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTable table;
	private DefaultTableModel model;

	public SeleccionIngredientes() {
		setTitle("Seleccionar Ingredientes");
		setSize(600, 400);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		// Crear tabla con modelo de datos
		model = new DefaultTableModel(
				new Object[] { "Ingrediente", "Precio por unidad", "Cantidad a usar", "Costo total" }, 0);
		table = new JTable(model);

		// Cargar ingredientes desde la base de datos
		cargarIngredientes();

		// Botón para calcular el costo total
		JButton btnCalcular = new JButton("Calcular Totales");
		btnCalcular.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < table.getRowCount(); i++) {
					double precio = (double) table.getValueAt(i, 1); // Precio por unidad
					int cantidad = Integer.parseInt(table.getValueAt(i, 2).toString()); // Cantidad a usar
					double costoTotal = precio * cantidad; // Calcular costo total
					table.setValueAt(costoTotal, i, 3); // Colocar costo total en la tabla
				}
			}
		});

		// Botón para guardar la receta
		JButton btnGuardar = new JButton("Guardar Receta");
		btnGuardar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				guardarReceta();
			}
		});

		// Botón para regresar a la pantalla principal
		JButton btnRegresar = new JButton("Regresar");
		btnRegresar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				regresarAPantallaPrincipal();
			}
		});

		// Agregar un ícono de flecha al botón "Regresar"
		btnRegresar.setIcon(new ImageIcon("ruta/al/icono_flecha.png")); // Ruta al ícono de la flecha

		// Agregar tabla y botones al panel
		JPanel panelBotones = new JPanel();
		panelBotones.add(btnCalcular);
		panelBotones.add(btnGuardar);
		panelBotones.add(btnRegresar);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		panel.add(panelBotones, BorderLayout.SOUTH);

		add(panel);
	}

	// Método para cargar ingredientes desde la base de datos
	private void cargarIngredientes() {
		Session session = Main.getSessionFactory().openSession();
		List<Ingrediente> ingredientes = session.createQuery("FROM Ingrediente", Ingrediente.class).list();

		// Agregar ingredientes a la tabla
		for (Ingrediente ingrediente : ingredientes) {
			model.addRow(new Object[] { ingrediente.getNombre(), ingrediente.getCostoPorUnidad(), 0, 0 });
		}

		session.close();
	}

	// Método para guardar la receta
	@SuppressWarnings("deprecation")
	private void guardarReceta() {
		Session session = Main.getSessionFactory().openSession(); // Usar SessionFactory de Main
		Transaction tx = session.beginTransaction();

		try {
			for (int i = 0; i < table.getRowCount(); i++) {
				String nombreIngrediente = table.getValueAt(i, 0).toString();
				int cantidad = Integer.parseInt(table.getValueAt(i, 2).toString());
				double costoTotal = Double.parseDouble(table.getValueAt(i, 3).toString());

				// Lógica para guardar la receta en la base de datos
				RecetaIngrediente recetaIngrediente = new RecetaIngrediente(null, null, costoTotal);
				((RecetaIngrediente) recetaIngrediente).setNombre1(nombreIngrediente);
				recetaIngrediente.setCantidadUtilizada(cantidad);
				((RecetaIngrediente) recetaIngrediente).setCostoTotal(costoTotal);

				session.save(recetaIngrediente); // Guardar en la base de datos
			}

			tx.commit();
			JOptionPane.showMessageDialog(null, "Receta guardada con éxito");
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error al guardar la receta");
		} finally {
			session.close();
		}
	}

	// Método para regresar a la pantalla principal
	private void regresarAPantallaPrincipal() {
		this.dispose(); // Cerrar la ventana actual
		PantallaPrincipal pantallaPrincipal = new PantallaPrincipal(); // Crear la nueva ventana
		pantallaPrincipal.setVisible(true); // Mostrar la pantalla principal
	}

	/**
	 * Create the frame.
	 * 
	 * @return
	 */
	public void SeleccionIngredientes() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		JComponent contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(getContentPane());
	}

}
