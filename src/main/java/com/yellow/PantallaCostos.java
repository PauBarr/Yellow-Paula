package com.yellow;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class PantallaCostos extends JFrame {

    private static final long serialVersionUID = 1L;
    private SessionFactory sessionFactory;
    private JTable tablaIngredientes;
    private JComboBox<Ingrediente> comboBoxIngredientes;
    private JLabel labelCostoTotal, labelGastosExtra, labelRentabilidad, labelPrecioFinal;
    private JTextField txtCostoTotal, txtGastosExtra, txtRentabilidad, txtPrecioFinal;
    private JButton btnImprimirPlanilla;
    
    private Map<String, Ingrediente> ingredientesMap;

    public PantallaCostos(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        ingredientesMap = new HashMap<>();
        initComponents();
        cargarIngredientes();
    }

    private void initComponents() {
        setTitle("Pantalla de Costos - Pepas");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panelPrincipal = new JPanel(new BorderLayout());

        JPanel panelSuperior = new JPanel();
        JLabel labelTitulo = new JLabel("PEPAS");
        labelTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        labelTitulo.setForeground(new Color(85, 107, 47));
        panelSuperior.add(labelTitulo);

        String[] columnNames = {"PRODUCTO", "TIPO PESO/LT", "PESO/LT R", "COSTO UNIT.", "CANT. UTILIZADA", "COSTO REAL"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 10);
        tablaIngredientes = new JTable(model);

        comboBoxIngredientes = new JComboBox<>();
        tablaIngredientes.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(comboBoxIngredientes));

        comboBoxIngredientes.addActionListener(e -> {
            int row = tablaIngredientes.getSelectedRow();
            if (row >= 0) {
                actualizarCamposIngrediente(row);
                actualizarCostoReal(row);
                actualizarCostoTotal();
            }
        });

        model.addTableModelListener(e -> {
            if (e.getColumn() == 4) {  // Columna "Cantidad Utilizada"
                int row = e.getFirstRow();
                actualizarCostoReal(row);
                actualizarCostoTotal();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaIngredientes);

        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));

        JPanel panelCostos = new JPanel();
        panelCostos.setLayout(new GridLayout(2, 4, 10, 10));

        labelCostoTotal = new JLabel("COSTO TOTAL POR PRODUCTO  $");
        txtCostoTotal = new JTextField("-");
        labelGastosExtra = new JLabel("GASTOS EXTRA  $");
        txtGastosExtra = new JTextField("0");  // Inicializa en 0
        labelRentabilidad = new JLabel("RENTABILIDAD  %");
        txtRentabilidad = new JTextField("0");  // Inicializa en 0
        labelPrecioFinal = new JLabel("PRECIO FINAL  $");
        labelPrecioFinal.setForeground(Color.RED);
        txtPrecioFinal = new JTextField("-");

        txtCostoTotal.setEditable(false);
        txtPrecioFinal.setEditable(false);

        panelCostos.add(labelCostoTotal);
        panelCostos.add(txtCostoTotal);
        panelCostos.add(labelGastosExtra);
        panelCostos.add(txtGastosExtra);
        panelCostos.add(labelRentabilidad);
        panelCostos.add(txtRentabilidad);
        panelCostos.add(labelPrecioFinal);
        panelCostos.add(txtPrecioFinal);

        btnImprimirPlanilla = new JButton("IMPRIMIR PLANILLA");
        btnImprimirPlanilla.setAlignmentX(CENTER_ALIGNMENT);

        panelInferior.add(panelCostos);
        panelInferior.add(Box.createVerticalStrut(10));
        panelInferior.add(btnImprimirPlanilla);

        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        add(panelPrincipal);

        // Listeners para actualizar el precio final al cambiar rentabilidad o gastos extra
        agregarDocumentListener(txtGastosExtra);
        agregarDocumentListener(txtRentabilidad);
    }
    
    // Método para agregar DocumentListener a los campos de texto
    private void agregarDocumentListener(JTextField textField) {
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarPrecioFinal();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarPrecioFinal();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarPrecioFinal();
            }
        });
    }
    
    private void cargarIngredientes() {
        try (Session session = sessionFactory.openSession()) {
            List<Ingrediente> ingredientes = session.createQuery("FROM Ingrediente", Ingrediente.class).list();
            for (Ingrediente ingrediente : ingredientes) {
                comboBoxIngredientes.addItem(ingrediente);
                ingredientesMap.put(ingrediente.getNombre(), ingrediente);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los ingredientes", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void actualizarCamposIngrediente(int row) {
        Ingrediente ingrediente = (Ingrediente) comboBoxIngredientes.getSelectedItem();
        if (ingrediente != null && row >= 0) {
            tablaIngredientes.setValueAt(ingrediente.getTipoPesoLt(), row, 1);
            tablaIngredientes.setValueAt(ingrediente.getPesoLtR(), row, 2);
            tablaIngredientes.setValueAt(ingrediente.getCostoUnitario(), row, 3);
        }
    }

    private void actualizarCostoReal(int row) {
        try {
            double cantidad = Double.parseDouble(tablaIngredientes.getValueAt(row, 4).toString());
            double costoUnitario = Double.parseDouble(tablaIngredientes.getValueAt(row, 3).toString());
            double costoReal = cantidad * costoUnitario;

            tablaIngredientes.setValueAt(String.format("$%.2f", costoReal), row, 5);
        } catch (Exception e) {
            tablaIngredientes.setValueAt("-", row, 5);
        }
    }

    // Método para calcular el costo total y actualizar el campo de precio final
    private void actualizarCostoTotal() {
        double total = 0.0;
        NumberFormat format = NumberFormat.getInstance(Locale.getDefault());

        for (int i = 0; i < tablaIngredientes.getRowCount(); i++) {
            Object costoRealObj = tablaIngredientes.getValueAt(i, 5);
            if (costoRealObj != null && !costoRealObj.toString().equals("-")) {
                try {
                    Number number = format.parse(costoRealObj.toString().replace("$", "").trim());
                    total += number.doubleValue();
                } catch (ParseException e) {
                    tablaIngredientes.setValueAt("-", i, 5);
                }
            }
        }
        txtCostoTotal.setText(String.format("$%.2f", total));
        actualizarPrecioFinal();  // Calcula el precio final con el nuevo costo total
    }

    // Método para calcular y actualizar el precio final en función del costo total, rentabilidad y gastos extra
    private void actualizarPrecioFinal() {
        try {
            double costoTotal = Double.parseDouble(txtCostoTotal.getText().replace("$", "").trim());
            double gastosExtra = Double.parseDouble(txtGastosExtra.getText().trim());
            double rentabilidad = Double.parseDouble(txtRentabilidad.getText().trim()) / 100;

            double precioFinal = costoTotal + gastosExtra + (costoTotal * rentabilidad);
            txtPrecioFinal.setText(String.format("$%.2f", precioFinal));
        } catch (NumberFormatException e) {
            txtPrecioFinal.setText("-");
        }
    }
}
