package com.yellow;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter; // Importar para el filtrado
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.event.DocumentEvent; // Importar para DocumentListener
import javax.swing.event.DocumentListener; // Importar para DocumentListener

public class VisualizarRecetas extends JFrame {

    private JTable recetasTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter; // Añadir el sorter para filtrar
    private JButton eliminarButton;
    private JButton actualizarCostosButton;
    private JButton verDetalleButton;
    private JButton atrasButton;

    private JComboBox<Categoria> filtroCategoriaComboBox; // Nuevo ComboBox para filtrar por categoría
    private JTextField buscadorTextField; // Nuevo campo de texto para buscar por nombre

    private JFrame ventanaAnterior;
    private SessionFactory sessionFactory;

    public VisualizarRecetas(JFrame ventanaAnterior, SessionFactory sessionFactory) {
        this.ventanaAnterior = ventanaAnterior;
        this.sessionFactory = sessionFactory;
        initComponents();
        cargarCategoriasEnFiltro(); // Cargar categorías al iniciar
        cargarDatosTabla(); // Cargar datos iniciales
    }

    private void initComponents() {
        setTitle("Visualizar y Gestionar Recetas");
        setSize(1000, 700); // Aumentar tamaño para nuevos controles
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel tituloLabel = new JLabel("LISTA DE RECETAS", SwingConstants.CENTER);
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(tituloLabel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Nombre Receta", "Descripción", "Costo Total", "Fecha Creación", "Categorías"}; // Añadida columna Categorías
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recetasTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel); // Inicializar el sorter
        recetasTable.setRowSorter(sorter); // Asignar el sorter a la tabla
        JScrollPane scrollPane = new JScrollPane(recetasTable);
        add(scrollPane, BorderLayout.CENTER);

        // Panel para filtros y búsqueda
        JPanel filtroPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filtroPanel.setBackground(new Color(255, 255, 220));

        filtroPanel.add(new JLabel("Filtrar por Categoría:"));
        filtroCategoriaComboBox = new JComboBox<>();
        filtroCategoriaComboBox.setPreferredSize(new Dimension(150, 25));
        filtroCategoriaComboBox.addActionListener(e -> aplicarFiltro()); // Aplicar filtro al cambiar categoría
        filtroPanel.add(filtroCategoriaComboBox);

        filtroPanel.add(new JLabel("Buscar por Nombre:"));
        buscadorTextField = new JTextField(20);
        buscadorTextField.setPreferredSize(new Dimension(150, 25));
        // Aplicar filtro dinámicamente mientras se escribe
        buscadorTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { aplicarFiltro(); }
            @Override
            public void removeUpdate(DocumentEvent e) { aplicarFiltro(); }
            @Override
            public void changedUpdate(DocumentEvent e) { aplicarFiltro(); }
        });
        filtroPanel.add(buscadorTextField);

        add(filtroPanel, BorderLayout.PAGE_START); // Colocar el panel de filtros arriba

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

    private void cargarCategoriasEnFiltro() {
        filtroCategoriaComboBox.removeAllItems();
        filtroCategoriaComboBox.addItem(null); // Opción para "Todas las categorías"
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
            // HQL para cargar las recetas y sus categorías de forma eficiente
            List<Receta> recetas = session.createQuery("SELECT DISTINCT r FROM Receta r LEFT JOIN FETCH r.categorias", Receta.class).list();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            for (Receta receta : recetas) {
                // Obtener los nombres de las categorías como una cadena separada por comas
                String categoriasStr = receta.getCategorias().stream()
                                            .map(Categoria::getNombreCategoria)
                                            .reduce((a, b) -> a + ", " + b)
                                            .orElse("Sin categoría"); // Si no tiene categorías

                Object[] rowData = {
                    receta.getId(),
                    receta.getNombre(),
                    receta.getDescripcion(),
                    String.format("$%.2f", receta.getCostoTotal()),
                    sdf.format(receta.getFechaCreacion()),
                    categoriasStr // Añadir la columna de categorías
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

        // Filtro por Categoría
        Categoria categoriaSeleccionada = (Categoria) filtroCategoriaComboBox.getSelectedItem();
        if (categoriaSeleccionada != null) {
            // Asumiendo que la columna "Categorías" es la última (índice 5)
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    String categoriasCelda = (String) entry.getValue(5); // Columna de Categorías
                    return categoriasCelda.contains(categoriaSeleccionada.getNombreCategoria());
                }
            });
        }

        // Filtro por Nombre de Receta (Buscador)
        String textoBusqueda = buscadorTextField.getText().trim();
        if (!textoBusqueda.isEmpty()) {
            // El filtro se aplica a la columna "Nombre Receta" (índice 1)
            filters.add(RowFilter.regexFilter("(?i)" + textoBusqueda, 1)); // (?i) para ignorar mayúsculas/minúsculas
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null); // No hay filtros, mostrar todo
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters)); // Aplicar todos los filtros combinados
        }
    }


    private void eliminarRecetaSeleccionada() {
        int selectedRowView = recetasTable.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una receta para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convertir el índice de la vista al índice del modelo subyacente
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
                    cargarDatosTabla(); // Recargar datos después de eliminar
                    aplicarFiltro(); // Volver a aplicar el filtro
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
                // Es crucial recargar el estado de los ingredientes desde la base de datos
                // para asegurar que se usen los costos de compra más actuales.
                // Usamos HQL con FETCH JOIN para cargar ingredientes y sus datos de compra de una vez.
                // Asegurarse de que los objetos RecetaIngrediente y Ingrediente también se recarguen
                // para que getCostoReal() de RecetaIngrediente use datos actualizados.
                // Dado que se usa session.refresh(ri.getIngrediente()), esto ya lo hace.
                // Se podría considerar cargar la receta con fetch joins para los RecetaIngrediente e Ingrediente
                // para evitar N+1 selects, pero session.refresh() funciona si la sesión está abierta.

                // Recarga cada ingrediente asociado para obtener el costo más reciente
                for (RecetaIngrediente ri : receta.getRecetaIngredientes()) {
                    session.refresh(ri.getIngrediente()); // Recarga el ingrediente desde la BD
                }

                receta.recalcularCostoTotal(); // Llama al método para recalcular el costo
                session.merge(receta); // Actualiza la receta en la base de datos
                transaction.commit();
                JOptionPane.showMessageDialog(this, "Costo de la receta actualizado exitosamente.");
                cargarDatosTabla(); // Recargar datos para mostrar el costo actualizado
                aplicarFiltro(); // Volver a aplicar el filtro
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
            // Usar HQL con FETCH JOIN para cargar la receta, sus ingredientes, y las categorías de una vez
            // Esto evita problemas de N+1 selects y LazyInitializationException
            Receta receta = session.createQuery(
                "SELECT r FROM Receta r " +
                "LEFT JOIN FETCH r.recetaIngredientes ri " +
                "LEFT JOIN FETCH ri.ingrediente " +
                "LEFT JOIN FETCH r.categorias " + // Añadir FETCH para categorías
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

                // Mostrar categorías
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
                scrollPane.setPreferredSize(new Dimension(550, 450)); // Ajustar tamaño

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