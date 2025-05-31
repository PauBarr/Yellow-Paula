package com.yellow;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.util.List;
import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
// <<<<<<<<<<<<<<<<<<<<<<<<< IMPORTACIONES FALTANTES >>>>>>>>>>>>>>>>>>>>>>>>>
import java.awt.FlowLayout; // Importación agregada
import java.awt.GridLayout; // Importación agregada
// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

public class IngresoIngrediente extends JFrame {

    private JTextField articuloField;
    private JRadioButton unidadRadio;
    private JRadioButton litrosRadio;
    private JRadioButton kilogramosRadio;
    private JRadioButton especialRadio;
    private JTextField cantidadDeCompraField;
    private JTextField costoDeCompraField;

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
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(255, 255, 220));

        // --- Panel Superior para el Título ---
        JPanel panelSuperior = new JPanel();
        panelSuperior.setBackground(new Color(255, 255, 220));
        panelSuperior.setBorder(new EmptyBorder(20, 0, 10, 0));
        JLabel tituloLabel = new JLabel("GESTIÓN DE INGREDIENTES", SwingConstants.CENTER);
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 28));
        tituloLabel.setForeground(new Color(60, 60, 60));
        panelSuperior.add(tituloLabel);
        add(panelSuperior, BorderLayout.NORTH);

        // --- Panel Central para Formularios ---
        JPanel panelCentral = new JPanel(new GridBagLayout());
        panelCentral.setBackground(new Color(255, 255, 220));
        panelCentral.setBorder(new EmptyBorder(10, 50, 10, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Fila 0: Selector de Ingrediente
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelCentral.add(new JLabel("Seleccionar Ingrediente:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        selectorIngrediente = new JComboBox<>();
        selectorIngrediente.setPreferredSize(new Dimension(300, 30));
        selectorIngrediente.addActionListener(e -> cargarDatosIngredienteSeleccionado());
        selectorIngrediente.setBackground(Color.WHITE);
        panelCentral.add(selectorIngrediente, gbc);
        gbc.gridwidth = 1;

        // Fila 1: Artículo (Nombre)
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelCentral.add(new JLabel("Artículo (Nombre):"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        articuloField = new JTextField();
        articuloField.setPreferredSize(new Dimension(300, 28));
        panelCentral.add(articuloField, gbc);
        gbc.gridwidth = 1;

        // Fila 2: Unidad de Compra (Radio Buttons)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panelCentral.add(new JLabel("Unidad de Compra:"), gbc);

        JPanel radioPanel = new JPanel(new GridLayout(4, 1));
        radioPanel.setBackground(new Color(255, 255, 220));
        unidadRadio = new JRadioButton("Unidad");
        litrosRadio = new JRadioButton("Litros");
        kilogramosRadio = new JRadioButton("Kilogramos");
        especialRadio = new JRadioButton("Especial/Otros");

        unidadRadio.setBackground(new Color(255, 255, 220));
        litrosRadio.setBackground(new Color(255, 255, 220));
        kilogramosRadio.setBackground(new Color(255, 255, 220));
        especialRadio.setBackground(new Color(255, 255, 220));
        
        Font radioFont = new Font("Segoe UI", Font.PLAIN, 14);
        unidadRadio.setFont(radioFont);
        litrosRadio.setFont(radioFont);
        kilogramosRadio.setFont(radioFont);
        especialRadio.setFont(radioFont);

        radioPanel.add(unidadRadio);
        radioPanel.add(litrosRadio);
        radioPanel.add(kilogramosRadio);
        radioPanel.add(especialRadio);

        unidadGroup = new ButtonGroup();
        unidadGroup.add(unidadRadio);
        unidadGroup.add(litrosRadio);
        unidadGroup.add(kilogramosRadio);
        unidadGroup.add(especialRadio);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridheight = 4;
        panelCentral.add(radioPanel, gbc);
        gbc.gridheight = 1;

        // Fila 3: Cantidad de Compra (al lado de los radios)
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panelCentral.add(new JLabel("Cantidad de Compra:"), gbc);
        gbc.gridy = 3;
        cantidadDeCompraField = new JTextField();
        cantidadDeCompraField.setPreferredSize(new Dimension(100, 28));
        panelCentral.add(cantidadDeCompraField, gbc);

        // Fila 4: Costo Total de Compra (al lado de los radios)
        gbc.gridy = 4;
        panelCentral.add(new JLabel("Costo Total de Compra:"), gbc);
        gbc.gridy = 5;
        costoDeCompraField = new JTextField();
        costoDeCompraField.setPreferredSize(new Dimension(100, 28));
        panelCentral.add(costoDeCompraField, gbc);

        add(panelCentral, BorderLayout.CENTER);

        // --- Panel Inferior para Botones ---
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panelInferior.setBackground(new Color(255, 255, 220));
        panelInferior.setBorder(new EmptyBorder(10, 0, 10, 0));

        ingresarButton = createRoundedButton("GUARDAR / ACTUALIZAR", new Color(85, 107, 47));
        limpiarButton = createRoundedButton("NUEVO / LIMPIAR", Color.DARK_GRAY);
        verIngredientesButton = createRoundedButton("VER INGREDIENTES", new Color(70, 130, 180));
        salirButton = createRoundedButton("SALIR", new Color(178, 34, 34));

        panelInferior.add(ingresarButton);
        panelInferior.add(limpiarButton);
        panelInferior.add(verIngredientesButton);
        panelInferior.add(salirButton);

        add(panelInferior, BorderLayout.SOUTH);

        // Listener de acciones
        ingresarButton.addActionListener(e -> guardarOActualizarIngrediente());
        limpiarButton.addActionListener(e -> limpiarCampos());
        salirButton.addActionListener(e -> salir());
        verIngredientesButton.addActionListener(e -> verIngredientes());
    }

    private JButton createRoundedButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 15, 15));
                super.paintComponent(g);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 15, 15));
                g2.dispose();
            }
        };
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(180, 45));
        return button;
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
            cantidadDeCompraField.setText(String.valueOf(ingredienteActual.getCantidadDeCompra()));
            costoDeCompraField.setText(String.valueOf(ingredienteActual.getCostoDeCompra()));

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
        String producto = articuloField.getText().trim();

        String tipo = getTipoSeleccionado();
        String costoDeCompraText = costoDeCompraField.getText().trim();
        String cantidadDeCompraText = cantidadDeCompraField.getText().trim();

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

                if (ingredienteActual == null) {
                    Ingrediente existente = (Ingrediente) session.createQuery("FROM Ingrediente WHERE nombre = :nombre")
                                                                  .setParameter("nombre", producto)
                                                                  .uniqueResult();
                    if (existente != null) {
                        JOptionPane.showMessageDialog(this, "Ya existe un ingrediente con este nombre. Por favor, actualice el existente o use otro nombre.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                        transaction.rollback();
                        return;
                    }

                    Ingrediente nuevoIngrediente = new Ingrediente(producto, tipo, cantidadDeCompra, costoDeCompra);
                    session.save(nuevoIngrediente);
                    JOptionPane.showMessageDialog(this, "Nuevo ingrediente ingresado exitosamente.");
                } else {
                    Ingrediente existenteConOtroId = (Ingrediente) session.createQuery("FROM Ingrediente WHERE nombre = :nombre AND id != :id")
                                                                          .setParameter("nombre", producto)
                                                                          .setParameter("id", ingredienteActual.getId())
                                                                          .uniqueResult();
                    if (existenteConOtroId != null) {
                        JOptionPane.showMessageDialog(this, "Ya existe otro ingrediente con este nombre. Por favor, elija un nombre único.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                        transaction.rollback();
                        return;
                    }

                    ingredienteActual.setNombre(producto);
                    ingredienteActual.setTipoPesoLt(tipo);
                    ingredienteActual.setCantidadDeCompra(cantidadDeCompra);
                    ingredienteActual.setCostoDeCompra(costoDeCompra);
                    session.merge(ingredienteActual);
                    JOptionPane.showMessageDialog(this, "Ingrediente actualizado exitosamente.");
                }
                transaction.commit();

                cargarIngredientesEnSelector();
                limpiarCampos();

            } catch (Exception ex) {
                if (transaction != null) {
                    transaction.rollback();
                }
                JOptionPane.showMessageDialog(this, "Error al guardar/actualizar en la base de datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                if (session != null) {
                    session.close();
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