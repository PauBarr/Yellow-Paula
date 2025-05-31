package com.yellow;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// MODIFICADO: Ahora extiende JPanel
public class VisualizarRecetas extends JPanel {

    private JTable recetasTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton eliminarButton;
    private JButton actualizarCostosButton;
    private JButton verDetalleButton;
    private JButton atrasButton;

    private JComboBox<Categoria> filtroCategoriaComboBox;
    private JTextField buscadorTextField;

    // AÑADIDO: Referencia a la ventana padre (IngresoReceta)
    private IngresoReceta ventanaPadre;
    private SessionFactory sessionFactory;

    private Categoria categoriaInicialFiltro;

    // MODIFICADO: Nuevo constructor para aceptar IngresoReceta como padre
    public VisualizarRecetas(IngresoReceta ventanaPadre, SessionFactory sessionFactory, Categoria categoriaInicialFiltro) {
        this.ventanaPadre = ventanaPadre; // Guardar la referencia
        this.sessionFactory = sessionFactory;
        this.categoriaInicialFiltro = categoriaInicialFiltro;
        initComponents();
        cargarCategoriasEnFiltro();
        cargarDatosTabla();
        if (this.categoriaInicialFiltro != null) {
            // Seleccionar la categoría en el ComboBox y aplicar el filtro
            // Primero, asegúrate de que el item exista en el ComboBox
            for (int i = 0; i < filtroCategoriaComboBox.getItemCount(); i++) {
                Categoria item = filtroCategoriaComboBox.getItemAt(i);
                if (item != null && item.getIdCategoria() == this.categoriaInicialFiltro.getIdCategoria()) {
                    filtroCategoriaComboBox.setSelectedItem(item);
                    break;
                }
            }
            aplicarFiltro();
        }
    }

    // Constructor original (para cuando no hay filtro inicial), llama al nuevo constructor
    public VisualizarRecetas(IngresoReceta ventanaPadre, SessionFactory sessionFactory) {
        this(ventanaPadre, sessionFactory, null);
    }

    private void initComponents() {
        // ELIMINADO: setTitle("Visualizar y Gestionar Recetas");
        // ELIMINADO: setSize(1000, 700);
        // ELIMINADO: setLocationRelativeTo(null);
        // ELIMINADO: setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // MODIFICADO: Configuración del panel en sí
        setLayout(new BorderLayout());
        setBackground(new Color(255, 255, 220));

        JLabel tituloLabel = new JLabel("LISTA DE RECETAS", SwingConstants.CENTER);
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(tituloLabel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Nombre Receta", "Descripción", "Costo Total", "Fecha Creación", "Categorías"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recetasTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        recetasTable.setRowSorter(sorter);
        JScrollPane scrollPane = new JScrollPane(recetasTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel filtroPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filtroPanel.setBackground(new Color(255, 255, 220));

        filtroPanel.add(new JLabel("Filtrar por Categoría:"));
        filtroCategoriaComboBox = new JComboBox<>();
        filtroCategoriaComboBox.setPreferredSize(new Dimension(150, 25));
        filtroCategoriaComboBox.addActionListener(e -> aplicarFiltro());
        filtroPanel.add(filtroCategoriaComboBox);

        filtroPanel.add(new JLabel("Buscar por Nombre:"));
        buscadorTextField = new JTextField(20);
        buscadorTextField.setPreferredSize(new Dimension(150, 25));
        buscadorTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { aplicarFiltro(); }
            @Override
            public void removeUpdate(DocumentEvent e) { aplicarFiltro(); }
            @Override
            public void changedUpdate(DocumentEvent e) { aplicarFiltro(); }
        });
        filtroPanel.add(buscadorTextField);

        add(filtroPanel, BorderLayout.PAGE_START);

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
        // MODIFICADO: Llama al método de la ventana padre para cambiar de panel
        atrasButton.addActionListener(e -> ventanaPadre.mostrarPanel("principal"));
    }

    private void cargarCategoriasEnFiltro() {
        filtroCategoriaComboBox.removeAllItems();
        filtroCategoriaComboBox.addItem(null);
        try (Session session = sessionFactory.openSession()) {
            List<Categoria> categorias = session.createQuery("FROM Categoria", Categoria.class).list();
            for (Categoria categoria : categorias) {
                filtroCategoriaComboBox.addItem(categoria);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar categorías en el filtro: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void cargarDatosTabla() {
        tableModel.setRowCount(0);
        try (Session session = sessionFactory.openSession()) {
            List<Receta> recetas = session.createQuery("SELECT DISTINCT r FROM Receta r LEFT JOIN FETCH r.categorias", Receta.class).list();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            for (Receta receta : recetas) {
                String categoriasStr = receta.getCategorias().stream()
                                            .map(Categoria::getNombreCategoria)
                                            .reduce((a, b) -> a + ", " + b)
                                            .orElse("Sin categoría");
                Object[] rowData = {
                    receta.getId(),
                    receta.getNombre(),
                    receta.getDescripcion(),
                    String.format("$%.2f", receta.getCostoTotal()),
                    sdf.format(receta.getFechaCreacion()),
                    categoriasStr
                };
                tableModel.addRow(rowData);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar recetas en la tabla: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void aplicarFiltro() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        Categoria categoriaSeleccionada = (Categoria) filtroCategoriaComboBox.getSelectedItem();
        if (categoriaSeleccionada != null) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    String categoriasCelda = (String) entry.getValue(5);
                    if (categoriasCelda == null || categoriasCelda.trim().isEmpty()) {
                        return false;
                    }
                    String[] nombresCategoriasEnCelda = categoriasCelda.split(",\\s*");
                    String nombreCategoriaFiltro = categoriaSeleccionada.getNombreCategoria();

                    for (String nombreCat : nombresCategoriasEnCelda) {
                        if (nombreCat.equalsIgnoreCase(nombreCategoriaFiltro)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        String textoBusqueda = buscadorTextField.getText().trim();
        if (!textoBusqueda.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + textoBusqueda, 1));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }


    private void eliminarRecetaSeleccionada() {
        int selectedRowView = recetasTable.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una receta para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedModelRow = recetasTable.convertRowIndexToModel(selectedRowView);
        Integer recetaId = (Integer) tableModel.getValueAt(selectedModelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea eliminar la receta seleccionada?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
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
                    aplicarFiltro();
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
        int selectedRowView = recetasTable.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una receta para actualizar su costo.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedModelRow = recetasTable.convertRowIndexToModel(selectedRowView);
        Integer recetaId = (Integer) tableModel.getValueAt(selectedModelRow, 0);

        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            Receta receta = session.get(Receta.class, recetaId);
            if (receta != null) {
                for (RecetaIngrediente ri : receta.getRecetaIngredientes()) {
                    session.refresh(ri.getIngrediente());
                }

                receta.recalcularCostoTotal();
                session.merge(receta);
                transaction.commit();
                JOptionPane.showMessageDialog(this, "Costo de la receta actualizado exitosamente.");
                cargarDatosTabla();
                aplicarFiltro();
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
        int selectedRowView = recetasTable.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una receta para ver su detalle.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedModelRow = recetasTable.convertRowIndexToModel(selectedRowView);
        Integer recetaId = (Integer) tableModel.getValueAt(selectedModelRow, 0);

        try (Session session = sessionFactory.openSession()) {
            Receta receta = session.createQuery(
                "SELECT r FROM Receta r " +
                "LEFT JOIN FETCH r.recetaIngredientes ri " +
                "LEFT JOIN FETCH ri.ingrediente " +
                "LEFT JOIN FETCH r.categorias " +
                "WHERE r.id = :id", Receta.class)
                .setParameter("id", recetaId)
                .uniqueResult();

            if (receta != null) {
                StringBuilder detalle = new StringBuilder();
                detalle.append("Nombre: ").append(receta.getNombre()).append("\n");
                detalle.append("Descripción: ").append(receta.getDescripcion()).append("\n");
                detalle.append("Costo Total: ").append(String.format("$%.2f", receta.getCostoTotal())).append("\n");
                detalle.append("Fecha Creación: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(receta.getFechaCreacion())).append("\n");
                detalle.append("Tiempo de Preparación: ").append(receta.getTiempoPreparacion()).append(" minutos").append("\n\n");

                detalle.append("Categorías:\n");
                if (receta.getCategorias() != null && !receta.getCategorias().isEmpty()) {
                    receta.getCategorias().forEach(cat -> detalle.append("  - ").append(cat.getNombreCategoria()).append("\n"));
                } else {
                    detalle.append("  No hay categorías asociadas.\n");
                }
                detalle.append("\n");

                detalle.append("Ingredientes:\n");

                if (receta.getRecetaIngredientes() != null && !receta.getRecetaIngredientes().isEmpty()) {
                    for (RecetaIngrediente ri : receta.getRecetaIngredientes()) {
                        detalle.append("  - ").append(ri.getIngrediente().getNombre())
                               .append(": ").append(ri.getCantidadUtilizada())
                               .append(" ").append(ri.getUnidadUtilizada())
                               .append(" (Costo de Compra: $").append(String.format("%.2f", ri.getIngrediente().getCostoDeCompra()))
                               .append(" por ").append(ri.getIngrediente().getCantidadDeCompra()).append(" ").append(ri.getIngrediente().getTipoPesoLt())
                               .append(", Costo Real en Receta: $").append(String.format("%.2f", ri.getCostoReal()))
                               .append(")\n");
                    }
                } else {
                    detalle.append("  No hay ingredientes asociados a esta receta.\n");
                }

                JTextArea detailArea = new JTextArea(detalle.toString());
                detailArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(detailArea);
                scrollPane.setPreferredSize(new Dimension(550, 450));

                JOptionPane.showMessageDialog(this, scrollPane, "Detalle de Receta: " + receta.getNombre(), JOptionPane.INFORMATION_MESSAGE);

            } else {
                JOptionPane.showMessageDialog(this, "Receta no encontrada.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al obtener el detalle de la receta: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // ELIMINADO: irAtras() ya que ahora lo maneja el padre
}