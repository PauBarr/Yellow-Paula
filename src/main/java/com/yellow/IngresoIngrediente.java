package com.yellow;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.awt.Font;

public class IngresoIngrediente extends JFrame {

    private JTextField articuloField;
    private JRadioButton unidadRadio; // Representará la unidad en la que se compró
    private JRadioButton litrosRadio;
    private JRadioButton kilogramosRadio;
    private JRadioButton especialRadio; // Podría ser "Otros" o algo más genérico
    private JTextField cantidadDeCompraField; // Nuevo: Cantidad total del paquete/unidad
    private JTextField costoDeCompraField; // Nuevo: Costo total del paquete/unidad

    private JButton ingresarButton;
    private JButton salirButton;
    private JButton limpiarButton;
    private JButton verIngredientesButton;

    private JComboBox<Ingrediente> selectorIngrediente;
    private Ingrediente ingredienteActual;

    private JFrame ventanaAnterior;
    private SessionFactory sessionFactory;
    private ButtonGroup unidadGroup;

    public IngresoIngrediente(JFrame ventanaAnterior, SessionFactory sessionFactory) {
        this.ventanaAnterior = ventanaAnterior;
        this.sessionFactory = sessionFactory;
        initComponents();
        cargarIngredientesEnSelector();
    }

    private void initComponents() {
        setTitle("Ingreso/Actualización de Ingredientes");
        setSize(450, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        JLabel tituloLabel = new JLabel("GESTIÓN DE INGREDIENTES");
        tituloLabel.setBounds(20, 10, 400, 30);
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(tituloLabel);

        JLabel selectorLabel = new JLabel("Seleccionar Ingrediente:");
        selectorLabel.setBounds(20, 50, 150, 25);
        add(selectorLabel);
        selectorIngrediente = new JComboBox<>();
        selectorIngrediente.setBounds(180, 50, 200, 25);
        selectorIngrediente.addActionListener(e -> cargarDatosIngredienteSeleccionado());
        add(selectorIngrediente);

        JLabel articuloLabel = new JLabel("Artículo (Nombre):");
        articuloLabel.setBounds(20, 90, 150, 25);
        articuloField = new JTextField();
        articuloField.setBounds(180, 90, 200, 25);

        JLabel unidadLabel = new JLabel("Unidad de Compra:"); // Texto actualizado
        unidadLabel.setBounds(20, 130, 150, 25); // Ampliado el espacio para el label

        unidadRadio = new JRadioButton("Unidad");
        unidadRadio.setBounds(40, 160, 100, 25);
        litrosRadio = new JRadioButton("Litros");
        litrosRadio.setBounds(40, 190, 100, 25);
        kilogramosRadio = new JRadioButton("Kilogramos");
        kilogramosRadio.setBounds(40, 220, 100, 25);
        especialRadio = new JRadioButton("Especial/Otros"); // Texto actualizado
        especialRadio.setBounds(40, 250, 120, 25); // Ampliado el espacio

        unidadGroup = new ButtonGroup();
        unidadGroup.add(unidadRadio);
        unidadGroup.add(litrosRadio);
        unidadGroup.add(kilogramosRadio);
        unidadGroup.add(especialRadio);

        // NUEVO: Cantidad de compra (ej. 1000 para 1kg, 1 para 1 banana)
        JLabel cantidadDeCompraLabel = new JLabel("Cantidad de Compra:");
        cantidadDeCompraLabel.setBounds(180, 160, 150, 25);
        cantidadDeCompraField = new JTextField();
        cantidadDeCompraField.setBounds(330, 160, 80, 25); // Ajustado el ancho

        // NUEVO: Costo de compra (ej. 5600 por el paquete, 200 por la banana)
        JLabel costoDeCompraLabel = new JLabel("Costo Total de Compra:");
        costoDeCompraLabel.setBounds(180, 200, 150, 25);
        costoDeCompraField = new JTextField();
        costoDeCompraField.setBounds(330, 200, 80, 25); // Ajustado el ancho

        ingresarButton = new JButton("GUARDAR / ACTUALIZAR");
        ingresarButton.setBounds(20, 300, 200, 35);

        limpiarButton = new JButton("NUEVO / LIMPIAR");
        limpiarButton.setBounds(230, 300, 180, 35);

        salirButton = new JButton("SALIR");
        salirButton.setBounds(20, 350, 150, 35);

        verIngredientesButton = new JButton("VER INGREDIENTES");
        verIngredientesButton.setBounds(180, 350, 230, 35);

        ingresarButton.addActionListener(e -> guardarOActualizarIngrediente());
        limpiarButton.addActionListener(e -> limpiarCampos());
        salirButton.addActionListener(e -> salir());
        verIngredientesButton.addActionListener(e -> verIngredientes());

        add(articuloLabel);
        add(articuloField);
        add(unidadLabel);
        add(unidadRadio);
        add(litrosRadio);
        add(kilogramosRadio);
        add(especialRadio);
        add(cantidadDeCompraLabel); // Agregado
        add(cantidadDeCompraField); // Agregado
        add(costoDeCompraLabel); // Agregado
        add(costoDeCompraField); // Agregado
        add(ingresarButton);
        add(limpiarButton);
        add(salirButton);
        add(verIngredientesButton);
    }

    private void cargarIngredientesEnSelector() {
        selectorIngrediente.removeAllItems();
        selectorIngrediente.addItem(null);
        try (Session session = sessionFactory.openSession()) {
            List<Ingrediente> ingredientes = session.createQuery("FROM Ingrediente", Ingrediente.class).list();
            for (Ingrediente ing : ingredientes) {
                selectorIngrediente.addItem(ing);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar ingredientes en el selector: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void cargarDatosIngredienteSeleccionado() {
        ingredienteActual = (Ingrediente) selectorIngrediente.getSelectedItem();
        if (ingredienteActual != null) {
            articuloField.setText(ingredienteActual.getNombre());
            // Aquí pueden ser null si los datos viejos no tienen valor para estas nuevas columnas
            // Hay que usar String.valueOf() que maneja nulls o asegurar que los getters devuelvan 0.0
            cantidadDeCompraField.setText(String.valueOf(ingredienteActual.getCantidadDeCompra())); //
            costoDeCompraField.setText(String.valueOf(ingredienteActual.getCostoDeCompra())); //

            unidadGroup.clearSelection();
            String tipo = ingredienteActual.getTipoPesoLt();
            if ("Unidad".equals(tipo)) {
                unidadRadio.setSelected(true);
            } else if ("Litros".equals(tipo)) {
                litrosRadio.setSelected(true);
            } else if ("Kilogramos".equals(tipo)) {
                kilogramosRadio.setSelected(true);
            } else if ("Especial/Otros".equals(tipo) || "Especial".equals(tipo)) {
                especialRadio.setSelected(true);
            }
        } else {
            limpiarCampos();
        }
    }

    private void guardarOActualizarIngrediente() {
        String producto = articuloField.getText();

        System.out.println("Valor de 'producto' desde el campo: '" + producto + "'");
        System.out.println("Es 'producto' vacío? " + producto.isEmpty());
        System.out.println("Es 'producto' solo espacios? " + producto.trim().isEmpty());

        String tipo = getTipoSeleccionado(); // ¡CORREGIDO: Ahora esta línea NO está duplicada!
        String costoDeCompraText = costoDeCompraField.getText();
        String cantidadDeCompraText = cantidadDeCompraField.getText();

        if (producto.isEmpty() || tipo == null || costoDeCompraText.isEmpty() || cantidadDeCompraText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos (Artículo, Unidad, Cantidad de Compra, Costo de Compra) son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double costoDeCompra = parsearNumero(costoDeCompraText);
            double cantidadDeCompra = parsearNumero(cantidadDeCompraText);

            Session session = null;
            Transaction transaction = null;
            try {
                session = sessionFactory.openSession();
                transaction = session.beginTransaction();

                System.out.println("DEBUG: Intentando guardar/actualizar ingrediente: " + (ingredienteActual == null ? "NUEVO" : "EXISTENTE"));
                System.out.println("DEBUG: Nombre: " + producto + ", Tipo: " + tipo + ", Cantidad: " + cantidadDeCompra + ", Costo: " + costoDeCompra);

                if (ingredienteActual == null) {
                    Ingrediente nuevoIngrediente = new Ingrediente(producto, tipo, cantidadDeCompra, costoDeCompra);
                    session.save(nuevoIngrediente);
                    System.out.println("DEBUG: Se llamó a session.save() para el nuevo ingrediente.");
                    JOptionPane.showMessageDialog(this, "Nuevo ingrediente ingresado exitosamente.");
                } else {
                    ingredienteActual.setNombre(producto);
                    ingredienteActual.setTipoPesoLt(tipo);
                    ingredienteActual.setCantidadDeCompra(cantidadDeCompra);
                    ingredienteActual.setCostoDeCompra(costoDeCompra);
                    session.merge(ingredienteActual); // ¡CORREGIDO: esta línea NO está duplicada!
                    System.out.println("DEBUG: Se llamó a session.merge() para el ingrediente existente.");
                    JOptionPane.showMessageDialog(this, "Ingrediente actualizado exitosamente.");
                }
                transaction.commit();
                System.out.println("DEBUG: La transacción se ha hecho commit.");

                cargarIngredientesEnSelector();
                limpiarCampos();

            } catch (Exception ex) {
                if (transaction != null) {
                    transaction.rollback();
                    System.err.println("DEBUG: La transacción se ha hecho rollback.");
                }
                JOptionPane.showMessageDialog(this, "Error al guardar/actualizar en la base de datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                if (session != null) {
                    session.close();
                    System.out.println("DEBUG: La sesión de Hibernate se ha cerrado.");
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad de Compra y Costo de Compra deben ser valores numéricos válidos.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarCampos() {
        articuloField.setText("");
        cantidadDeCompraField.setText("");
        costoDeCompraField.setText("");
        unidadGroup.clearSelection();
        selectorIngrediente.setSelectedItem(null);
        ingredienteActual = null;
    }

    private String getTipoSeleccionado() {
        if (unidadRadio.isSelected()) {
            return "Unidad";
        } else if (litrosRadio.isSelected()) {
            return "Litros";
        } else if (kilogramosRadio.isSelected()) {
            return "Kilogramos";
        } else if (especialRadio.isSelected()) {
            return "Especial/Otros";
        }
        return null;
    }

    // Método robusto para parsear números (copiado de PantallaCostos)
    private double parsearNumero(String text) {
        if (text == null || text.trim().isEmpty() || text.trim().equals("-")) {
            return 0.0;
        }
        String cleanedText = text.trim().replaceAll("[^\\d\\.,-]", "");

        if (cleanedText.contains(".") && cleanedText.contains(",")) {
            cleanedText = cleanedText.replace(".", "");
            cleanedText = cleanedText.replace(",", ".");
        } else if (cleanedText.contains(",")) {
            cleanedText = cleanedText.replace(",", ".");
        }

        try {
            return Double.parseDouble(cleanedText);
        } catch (NumberFormatException e) {
            System.err.println("Error al parsear el número '" + text + "' después de limpiar. Se devolverá 0.0. Limpiado a: '" + cleanedText + "'");
            return 0.0;
        }
    }

    private void verIngredientes() {
        this.setVisible(false);
        VisualizarIngredientes visualizarIngredientes = new VisualizarIngredientes(this, sessionFactory);
        visualizarIngredientes.setVisible(true);
    }

    private void salir() {
        if (ventanaAnterior != null) {
            ventanaAnterior.setVisible(true);
        }
        dispose();
    }
}