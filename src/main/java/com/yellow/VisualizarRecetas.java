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
        cargarDatosTabla(); // Carga las recetas al iniciar la ventana
    }

    private void initComponents() {
        setTitle("Visualizar y Gestionar Recetas");
        setSize(800, 600); // Tamaño de la ventana
        setLocationRelativeTo(null); // Centra la ventana
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cierra solo esta ventana
        setLayout(new BorderLayout()); // Usa un diseño BorderLayout

        // Título de la pantalla
        JLabel tituloLabel = new JLabel("LISTA DE RECETAS", SwingConstants.CENTER); // Centra el texto
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 20)); // Fuente y tamaño
        add(tituloLabel, BorderLayout.NORTH); // Lo coloca arriba

        // Configurar el modelo de la tabla
        String[] columnNames = {"ID", "Nombre Receta", "Descripción", "Costo Total", "Fecha Creación"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Las celdas de la tabla no serán editables
            }
        };
        recetasTable = new JTable(tableModel); // Crea la tabla
        JScrollPane scrollPane = new JScrollPane(recetasTable); // Agrega scroll si hay muchas recetas
        add(scrollPane, BorderLayout.CENTER); // Coloca la tabla en el centro

        // Panel para los botones
        JPanel buttonPanel = new JPanel();
        eliminarButton = new JButton("ELIMINAR RECETA SELECCIONADA");
        actualizarCostosButton = new JButton("ACTUALIZAR COSTOS DE RECETA");
        verDetalleButton = new JButton("VER DETALLE DE RECETA");
        atrasButton = new JButton("ATRÁS");

        buttonPanel.add(eliminarButton);
        buttonPanel.add(actualizarCostosButton);
        buttonPanel.add(verDetalleButton);
        buttonPanel.add(atrasButton);
        add(buttonPanel, BorderLayout.SOUTH); // Coloca los botones abajo

        // Acciones de los botones
        eliminarButton.addActionListener(e -> eliminarRecetaSeleccionada());
        actualizarCostosButton.addActionListener(e -> actualizarCostoRecetaSeleccionada());
        verDetalleButton.addActionListener(e -> verDetalleReceta());
        atrasButton.addActionListener(e -> irAtras());
    }

    // Método para cargar los datos de las recetas desde la base de datos a la tabla
    private void cargarDatosTabla() {
        tableModel.setRowCount(0); // Vacía la tabla antes de cargar
        try (Session session = sessionFactory.openSession()) { // Abre una sesión de Hibernate
            // Consulta todas las recetas de la base de datos
            List<Receta> recetas = session.createQuery("FROM Receta", Receta.class).list();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm"); // Formato de fecha
            for (Receta receta : recetas) { // Recorre cada receta
                Object[] rowData = { // Crea una fila de datos para la tabla
                    receta.getId(),
                    receta.getNombre(),
                    receta.getDescripcion(),
                    String.format("$%.2f", receta.getCostoTotal()), // Formato de moneda
                    sdf.format(receta.getFechaCreacion()) // Formato de fecha
                };
                tableModel.addRow(rowData); // Añade la fila a la tabla
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar recetas en la tabla: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Método para eliminar la receta seleccionada
    private void eliminarRecetaSeleccionada() {
        int selectedRow = recetasTable.getSelectedRow(); // Obtiene la fila seleccionada
        if (selectedRow == -1) { // Si no hay fila seleccionada
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una receta para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Pide confirmación al usuario
        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea eliminar la receta seleccionada?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) { // Si el usuario confirma
            Integer recetaId = (Integer) tableModel.getValueAt(selectedRow, 0); // Obtiene el ID de la receta

            Session session = null;
            Transaction transaction = null;
            try {
                session = sessionFactory.openSession(); // Abre una nueva sesión
                transaction = session.beginTransaction(); // Inicia una transacción

                Receta receta = session.get(Receta.class, recetaId); // Busca la receta por ID
                if (receta != null) {
                    session.delete(receta); // Elimina la receta
                    transaction.commit(); // Confirma la transacción
                    JOptionPane.showMessageDialog(this, "Receta eliminada exitosamente.");
                    cargarDatosTabla(); // Recarga la tabla
                } else {
                    JOptionPane.showMessageDialog(this, "Receta no encontrada.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                if (transaction != null) {
                    transaction.rollback(); // Si hay error, revierte la transacción
                }
                JOptionPane.showMessageDialog(this, "Error al eliminar la receta: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                if (session != null) {
                    session.close(); // Cierra la sesión
                }
            }
        }
    }

    // Método para actualizar el costo de la receta seleccionada
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
                // para asegurar que se usen los costos unitarios más actuales.
                for (RecetaIngrediente ri : receta.getRecetaIngredientes()) {
                    session.refresh(ri.getIngrediente()); // Recarga el ingrediente desde la BD
                }

                receta.recalcularCostoTotal(); // Llama al nuevo método para recalcular el costo
                session.merge(receta); // Actualiza la receta en la base de datos
                transaction.commit();
                JOptionPane.showMessageDialog(this, "Costo de la receta actualizado exitosamente.");
                cargarDatosTabla(); // Recarga la tabla para mostrar el costo actualizado
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

    // Método para ver el detalle de una receta
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
                               .append(" (Costo Unitario: $").append(String.format("%.2f", ri.getIngrediente().getCostoUnitario()))
                               .append(", Costo Real en Receta: $").append(String.format("%.2f", ri.getCostoReal()))
                               .append(")\n");
                    }
                } else {
                    detalle.append("  No hay ingredientes asociados a esta receta.\n");
                }

                JTextArea detailArea = new JTextArea(detalle.toString());
                detailArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(detailArea);
                scrollPane.setPreferredSize(new Dimension(400, 300)); // Ajustar tamaño

                JOptionPane.showMessageDialog(this, scrollPane, "Detalle de Receta: " + receta.getNombre(), JOptionPane.INFORMATION_MESSAGE);

            } else {
                JOptionPane.showMessageDialog(this, "Receta no encontrada.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al obtener el detalle de la receta: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Método para regresar a la ventana anterior
    private void irAtras() {
        if (ventanaAnterior != null) {
            ventanaAnterior.setVisible(true);
        }
        dispose();
    }
}