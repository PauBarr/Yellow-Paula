package com.yellow;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelEvent;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class PantallaCostos extends JPanel {

    private SessionFactory sessionFactory;
    private JTable tablaIngredientes;
    private JComboBox<Ingrediente> comboBoxIngredientes;
    private JTextField campoCostoTotal, campoGastosExtra, campoPrecioFinal;
    private IngresoReceta ventanaPadre;
    private List<Categoria> categoriasSeleccionadas = new ArrayList<>();

    public PantallaCostos(IngresoReceta ventanaPadre, SessionFactory sessionFactory) {
        this.ventanaPadre = ventanaPadre;
        this.sessionFactory = sessionFactory;
        initComponents();
        cargarIngredientes();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(255, 255, 220));

        // TÍTULO
        JLabel titulo = new JLabel("NUEVA RECETA", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        titulo.setForeground(new Color(85, 107, 47));
        add(titulo, BorderLayout.NORTH);

        // TABLA DE INGREDIENTES
        String[] columnas = {"PRODUCTO", "TIPO COMPRA", "CANT. COMPRA", "COSTO COMPRA", "CANT. UTILIZADA", "UNIDAD UTILIZADA", "COSTO REAL"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return c == 0 || c == 4 || c == 5; }
        };
        
        tablaIngredientes = new JTable(modelo);
        comboBoxIngredientes = new JComboBox<>();
        // Esto permite seleccionar el producto en la primera columna
        tablaIngredientes.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(comboBoxIngredientes));
        
        // Selector de unidad (gramos, kilos, etc.)
        JComboBox<String> comboUnid = new JComboBox<>(new String[]{"gramos", "mililitros", "unidades", "litros", "kilogramos"});
        tablaIngredientes.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(comboUnid));

        add(new JScrollPane(tablaIngredientes), BorderLayout.CENTER);

        // PANEL INFERIOR (CAMPOS Y BOTONES)
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(new Color(255, 255, 220));

        // Campos de texto para los totales
        JPanel panelCampos = new JPanel(new GridLayout(3, 2, 5, 5));
        panelCampos.setBackground(new Color(255, 255, 220));
        campoCostoTotal = new JTextField("$0,00"); campoCostoTotal.setEditable(false);
        campoGastosExtra = new JTextField("0");
        campoPrecioFinal = new JTextField("$0,00"); campoPrecioFinal.setEditable(false);

        panelCampos.add(new JLabel("COSTO INGREDIENTES:")); panelCampos.add(campoCostoTotal);
        panelCampos.add(new JLabel("GASTOS EXTRA:")); panelCampos.add(campoGastosExtra);
        panelCampos.add(new JLabel("PRECIO FINAL:")); panelCampos.add(campoPrecioFinal);

        // RESTAURACIÓN DE BOTONERA
        JPanel panelBotones = new JPanel(new GridLayout(2, 2, 10, 10));
        panelBotones.setBackground(new Color(255, 255, 220));

        JButton btnFila = new JButton("AGREGAR FILA");
        btnFila.addActionListener(e -> agregarFila());

        JButton btnGuardar = new JButton("GUARDAR RECETA");
        btnGuardar.addActionListener(e -> guardarReceta());

        JButton btnImprimir = new JButton("IMPRIMIR PLANILLA");
        
        JButton btnRegresar = new JButton("REGRESAR");
        btnRegresar.addActionListener(e -> ventanaPadre.mostrarPanel("principal"));

        panelBotones.add(btnFila);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnImprimir);
        panelBotones.add(btnRegresar);

        panelInferior.add(panelCampos, BorderLayout.NORTH);
        panelInferior.add(panelBotones, BorderLayout.SOUTH);
        add(panelInferior, BorderLayout.SOUTH);

        // Lógica de cálculo automático
        modelo.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int f = e.getFirstRow();
                if (e.getColumn() == 0 || e.getColumn() == 4 || e.getColumn() == 5) {
                    actualizarCostoReal(f);
                    recalcularTotales();
                }
            }
        });

        campoGastosExtra.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { recalcularTotales(); }
            public void removeUpdate(DocumentEvent e) { recalcularTotales(); }
            public void changedUpdate(DocumentEvent e) { recalcularTotales(); }
        });
        
        agregarFila(); // Inicia con una fila vacía
    }

    private void actualizarCostoReal(int fila) {
        try {
            Ingrediente ing = (Ingrediente) tablaIngredientes.getValueAt(fila, 0);
            if (ing == null) return;
            
            // Setea automáticamente datos de compra para que los veas
            tablaIngredientes.setValueAt(ing.getTipoPesoLt(), fila, 1);
            tablaIngredientes.setValueAt(ing.getCantidadDeCompra(), fila, 2);
            tablaIngredientes.setValueAt(ing.getCostoDeCompra(), fila, 3);

            double cantUtil = parsearNumero(tablaIngredientes.getValueAt(fila, 4).toString());
            String unidComp = ing.getTipoPesoLt().toLowerCase();

            // CORRECCIÓN AZÚCAR: División por 1000 si comprás por kilo
            double factor = (unidComp.contains("kilo") || unidComp.contains("litro")) ? 0.001 : 1.0;

            double costo = (ing.getCostoDeCompra() / ing.getCantidadDeCompra()) * cantUtil * factor;
            tablaIngredientes.setValueAt(String.format("$%.0f", costo), fila, 6);
        } catch (Exception e) {}
    }

    private void recalcularTotales() {
        double totalIng = 0;
        for (int i = 0; i < tablaIngredientes.getRowCount(); i++) {
            Object v = tablaIngredientes.getValueAt(i, 6);
            if (v != null) totalIng += parsearNumero(v.toString());
        }
        campoCostoTotal.setText(String.format("$%.2f", totalIng));
        double extra = parsearNumero(campoGastosExtra.getText());
        campoPrecioFinal.setText(String.format("$%.2f", totalIng + extra));
    }

    private double parsearNumero(String s) {
        if (s == null || s.isEmpty()) return 0;
        return Double.parseDouble(s.replaceAll("[^\\d.]", ""));
    }

    private void cargarIngredientes() {
        try (Session s = sessionFactory.openSession()) {
            List<Ingrediente> l = s.createQuery("FROM Ingrediente", Ingrediente.class).list();
            for (Ingrediente i : l) comboBoxIngredientes.addItem(i);
        }
    }

    private void agregarFila() {
        ((DefaultTableModel) tablaIngredientes.getModel()).addRow(new Object[]{null, "", 0.0, 0.0, 0.0, "", "$0.00"});
    }

    private void guardarReceta() {
        String n = JOptionPane.showInputDialog("Nombre receta:");
        if (n == null || n.trim().isEmpty()) return;
        Receta r = new Receta(n, "");
        r.setGastosExtra(parsearNumero(campoGastosExtra.getText()));
        r.recalcularCostoTotal();
        try (Session s = sessionFactory.openSession()) {
            Transaction tx = s.beginTransaction();
            s.save(r); tx.commit();
            JOptionPane.showMessageDialog(this, "¡Receta Guardada!");
        }
    }
}