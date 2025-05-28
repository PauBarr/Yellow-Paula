package com.yellow;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class VisualizarRecetas extends JFrame {

    private JTable recetasTable;
    private DefaultTableModel tableModel;
    private JButton eliminarButton;
    private JButton actualizarCostosButton;
    private JButton verDetalleButton;
    private JButton atrasButton;

    private JFrame ventanaAnterior;
    private SessionFactory sessionFactory;

    public VisualizarRecetas(JFrame ventanaAnterior, SessionFactory sessionFactory) {
        this.ventanaAnterior = ventanaAnterior;
        this.sessionFactory = sessionFactory;
        initComponents();
        cargarDatosTabla();
    }

    private void initComponents() {
        setTitle("Visualizar y Gestionar Recetas");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel tituloLabel = new JLabel("LISTA DE RECETAS", SwingConstants.CENTER);
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(tituloLabel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Nombre Receta", "Descripción", "Costo Total", "Fecha Creación"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recetasTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(recetasTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        eliminarButton = new JButton("ELIMINAR RECETA SELECCIONADA");
        actualizarCostosButton = new JButton("ACTUALIZAR COSTOS DE RECETA");
        verDetalleButton = new JButton("VER DETALLE DE RECETA");
        atrasButton = new JButton("ATRÁS");

        buttonPanel.add(eliminarButton);
        buttonPanel.add(actualizarCostosButton);
        buttonPanel.add(verDetalleButton);
        buttonPanel.add(atrasButton);
        add(buttonPanel, BorderLayout.SOUTH);

        eliminarButton.addActionListener(e -> eliminarRecetaSeleccionada());
        actualizarCostosButton.addActionListener(e -> actualizarCostoRecetaSeleccionada());
        verDetalleButton.addActionListener(e -> verDetalleReceta());
        atrasButton.addActionListener(e -> irAtras());
    }

    private void cargarDatosTabla() {
        tableModel.setRowCount(0);
        try (Session session = sessionFactory.openSession()) {
            List<Receta> recetas = session.createQuery("FROM Receta", Receta.class).list();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            for (Receta receta : recetas) {
                Object[] rowData = {
                    receta.getId(),
                    receta.getNombre(),
                    receta.getDescripcion(),
                    String.format("$%.2f", receta.getCostoTotal()),
                    sdf.format(receta.getFechaCreacion())
                };
                tableModel.addRow(rowData);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar recetas en la tabla: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void eliminarRecetaSeleccionada() {
        int selectedRow = recetasTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una receta para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea eliminar la receta seleccionada?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Integer recetaId = (Integer) tableModel.getValueAt(selectedRow, 0);

            Session session = null;
            Transaction transaction = null;
            try {
                session = sessionFactory.openSession();
                transaction = session.beginTransaction();

                Receta receta = session.get(Receta.class, recetaId);
                if (receta != null) {
                    session.delete(receta);
                    transaction.commit();
                    JOptionPane.showMessageDialog(this, "Receta eliminada exitosamente.");
                    cargarDatosTabla();
                } else {
                    JOptionPane.showMessageDialog(this, "Receta no encontrada.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                if (transaction != null) {
                    transaction.rollback();
                }
                JOptionPane.showMessageDialog(this, "Error al eliminar la receta: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                if (session != null) {
                    session.close();
                }
            }
        }
    }

    private void actualizarCostoRecetaSeleccionada() {
        int selectedRow = recetasTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una receta para actualizar su costo.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer recetaId = (Integer) tableModel.getValueAt(selectedRow, 0);

        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            Receta receta = session.get(Receta.class, recetaId);
            if (receta != null) {
                // Es crucial recargar el estado de los ingredientes desde la base de datos
                // para asegurar que se usen los costos de compra más actuales.
                for (RecetaIngrediente ri : receta.getRecetaIngredientes()) {
                    session.refresh(ri.getIngrediente()); // Recarga el ingrediente desde la BD
                }

                receta.recalcularCostoTotal(); // Llama al método para recalcular el costo
                session.merge(receta); // Actualiza la receta en la base de datos
                transaction.commit();
                JOptionPane.showMessageDialog(this, "Costo de la receta actualizado exitosamente.");
                cargarDatosTabla();
            } else {
                JOptionPane.showMessageDialog(this, "Receta no encontrada.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            JOptionPane.showMessageDialog(this, "Error al actualizar el costo de la receta: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void verDetalleReceta() {
        int selectedRow = recetasTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una receta para ver su detalle.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer recetaId = (Integer) tableModel.getValueAt(selectedRow, 0);

        try (Session session = sessionFactory.openSession()) {
            Receta receta = session.get(Receta.class, recetaId);
            if (receta != null) {
                StringBuilder detalle = new StringBuilder();
                detalle.append("Nombre: ").append(receta.getNombre()).append("\n");
                detalle.append("Descripción: ").append(receta.getDescripcion()).append("\n");
                detalle.append("Costo Total: ").append(String.format("$%.2f", receta.getCostoTotal())).append("\n");
                detalle.append("Fecha Creación: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(receta.getFechaCreacion())).append("\n\n");
                detalle.append("Ingredientes:\n");

                if (receta.getRecetaIngredientes() != null && !receta.getRecetaIngredientes().isEmpty()) {
                    for (RecetaIngrediente ri : receta.getRecetaIngredientes()) {
                        detalle.append("  - ").append(ri.getIngrediente().getNombre())
                               .append(": ").append(ri.getCantidadUtilizada())
                               .append(" ").append(ri.getUnidadUtilizada()) // Mostrar la unidad utilizada
                               .append(" (Costo de Compra: $").append(String.format("%.2f", ri.getIngrediente().getCostoDeCompra())) // Costo de compra
                               .append(" por ").append(ri.getIngrediente().getCantidadDeCompra()).append(" ").append(ri.getIngrediente().getTipoPesoLt()) // Cantidad de compra y su tipo
                               .append(", Costo Real en Receta: $").append(String.format("%.2f", ri.getCostoReal()))
                               .append(")\n");
                    }
                } else {
                    detalle.append("  No hay ingredientes asociados a esta receta.\n");
                }

                JTextArea detailArea = new JTextArea(detalle.toString());
                detailArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(detailArea);
                scrollPane.setPreferredSize(new Dimension(450, 350)); // Ajustar tamaño

                JOptionPane.showMessageDialog(this, scrollPane, "Detalle de Receta: " + receta.getNombre(), JOptionPane.INFORMATION_MESSAGE);

            } else {
                JOptionPane.showMessageDialog(this, "Receta no encontrada.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al obtener el detalle de la receta: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void irAtras() {
        if (ventanaAnterior != null) {
            ventanaAnterior.setVisible(true);
        }
        dispose();
    }
}