package com.yellow;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import javax.swing.*;

public class IngresoIngrediente extends JFrame {

    private JTextField articuloField;
    private JRadioButton unidadRadio;
    private JRadioButton litrosRadio;
    private JRadioButton kilogramosRadio;
    private JRadioButton especialRadio;
    private JTextField cantidadField;
    private JTextField precioField;
    private JButton ingresarButton;
    private JButton salirButton;
    private JFrame ventanaAnterior;
    private SessionFactory sessionFactory;
    private ButtonGroup unidadGroup;

    public IngresoIngrediente(JFrame ventanaAnterior, SessionFactory sessionFactory) {
        this.ventanaAnterior = ventanaAnterior;
        this.sessionFactory = sessionFactory;
        initComponents();
    }

    private void initComponents() {
        setTitle("Ingreso de Ingredientes");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        // Etiquetas y campos de entrada
        JLabel articuloLabel = new JLabel("Artículo:");
        articuloLabel.setBounds(20, 20, 100, 25);
        articuloField = new JTextField();
        articuloField.setBounds(120, 20, 200, 25);

        JLabel unidadLabel = new JLabel("Unidad:");
        unidadLabel.setBounds(20, 60, 100, 25);

        unidadRadio = new JRadioButton("Unidad");
        unidadRadio.setBounds(40, 90, 100, 25);
        litrosRadio = new JRadioButton("Litros");
        litrosRadio.setBounds(40, 120, 100, 25);
        kilogramosRadio = new JRadioButton("Kilogramos");
        kilogramosRadio.setBounds(40, 150, 100, 25);
        especialRadio = new JRadioButton("Especial");
        especialRadio.setBounds(40, 180, 100, 25);

        unidadGroup = new ButtonGroup();
        unidadGroup.add(unidadRadio);
        unidadGroup.add(litrosRadio);
        unidadGroup.add(kilogramosRadio);
        unidadGroup.add(especialRadio);

        JLabel cantidadLabel = new JLabel("Cantidad:");
        cantidadLabel.setBounds(150, 90, 100, 25);
        cantidadField = new JTextField();
        cantidadField.setBounds(220, 90, 100, 25);

        JLabel precioLabel = new JLabel("Precio:");
        precioLabel.setBounds(150, 130, 100, 25);
        precioField = new JTextField();
        precioField.setBounds(220, 130, 100, 25);

        // Botones
        ingresarButton = new JButton("INGRESAR");
        ingresarButton.setBounds(120, 200, 100, 30);
        salirButton = new JButton("SALIR");
        salirButton.setBounds(230, 200, 100, 30);

        // Acciones de los botones
        ingresarButton.addActionListener(e -> guardarIngrediente());
        salirButton.addActionListener(e -> salir());

        // Añadir componentes al JFrame
        add(articuloLabel);
        add(articuloField);
        add(unidadLabel);
        add(unidadRadio);
        add(litrosRadio);
        add(kilogramosRadio);
        add(especialRadio);
        add(cantidadLabel);
        add(cantidadField);
        add(precioLabel);
        add(precioField);
        add(ingresarButton);
        add(salirButton);
    }

    // Método para guardar el ingrediente en la base de datos
    private void guardarIngrediente() {
        System.out.println("Botón INGRESAR presionado"); // Mensaje para verificar la ejecución
        String producto = articuloField.getText();
        String tipo = getTipoSeleccionado();
        String precioText = precioField.getText();
        String cantidadText = cantidadField.getText();

        if (producto.isEmpty() || tipo == null || precioText.isEmpty() || cantidadText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double precio = Double.parseDouble(precioText);
            double cantidad = Double.parseDouble(cantidadText);

            Ingrediente ingrediente = new Ingrediente(producto, tipo, cantidad, precio, cantidad * precio);

            try (Session session = sessionFactory.openSession()) {
                Transaction transaction = session.beginTransaction();
                session.save(ingrediente);
                transaction.commit();
                JOptionPane.showMessageDialog(this, "Ingrediente ingresado exitosamente.");
                limpiarCampos();  // Limpiar los campos después de guardar
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Precio y cantidad deben ser valores numéricos.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al guardar en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para limpiar los campos después de guardar un ingrediente
    @SuppressWarnings("null")
	private void limpiarCampos() {
        articuloField.setText("");
        cantidadField.setText("");
        precioField.setText("");
		unidadGroup.clearSelection(); // Deseleccionar todos los radio buttons
    }

    // Método para obtener el tipo seleccionado
    private String getTipoSeleccionado() {
        if (unidadRadio.isSelected()) {
            return "Unidad";
        } else if (litrosRadio.isSelected()) {
            return "Litros";
        } else if (kilogramosRadio.isSelected()) {
            return "Kilogramos";
        } else if (especialRadio.isSelected()) {
            return "Especial";
        }
        return null;
    }

    // Método para salir y volver a la ventana anterior
    private void salir() {
        if (ventanaAnterior != null) {
            ventanaAnterior.setVisible(true);
        }
        dispose();
    }
}
