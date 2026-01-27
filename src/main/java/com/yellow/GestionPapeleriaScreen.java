package com.yellow;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.io.FileWriter;
import java.io.IOException;

public class GestionPapeleriaScreen extends JFrame {

    private SessionFactory sessionFactory;
    private JFrame ventanaAnterior; // Referencia a la ventana anterior (PantallaPrincipal)

    private JTable tablaProductos;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField campoNombreProducto;
    private JTextField campoStock;
    private JTextField campoDescripcionMedida;
    private JCheckBox checkEnStock;
    private JTextField campoBuscador;

    private JButton btnAgregar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnLimpiarCampos;
    private JButton btnGenerarListaSinStock;
    private JButton btnRegresarPrincipal; // Cambiado de btnAtras para mayor claridad

    public GestionPapeleriaScreen(JFrame ventanaAnterior, SessionFactory sessionFactory) {
        this.ventanaAnterior = ventanaAnterior;
        this.sessionFactory = sessionFactory;
        initComponents();
        cargarProductosTabla();
    }

    private void initComponents() {
        setTitle("Gestión de Papelería");
        setSize(1000, 750); // Ajustada la altura para asegurar visibilidad del botón de regresar
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(255, 255, 220)); // Amarillo claro

        // --- Panel Superior (Título y Buscador) ---
        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.setBackground(new Color(255, 255, 220));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JLabel tituloLabel = new JLabel("GESTIÓN DE ARTÍCULOS DE PAPELERÍA", SwingConstants.CENTER);
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 26));
        tituloLabel.setForeground(new Color(60, 60, 60));
        panelSuperior.add(tituloLabel, BorderLayout.NORTH);

        JPanel panelBuscador = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBuscador.setBackground(new Color(255, 255, 220));
        JLabel labelBuscador = new JLabel("Buscar por Nombre:");
        labelBuscador.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campoBuscador = new JTextField(25);
        campoBuscador.setPreferredSize(new Dimension(200, 30));
        campoBuscador.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { aplicarFiltro(); }
            @Override
            public void removeUpdate(DocumentEvent e) { aplicarFiltro(); }
            @Override
            public void changedUpdate(DocumentEvent e) { aplicarFiltro(); }
        });
        panelBuscador.add(labelBuscador);
        panelBuscador.add(campoBuscador);
        panelSuperior.add(panelBuscador, BorderLayout.CENTER);

        add(panelSuperior, BorderLayout.NORTH);

        // --- Panel Central (Tabla) ---
        String[] columnNames = {"ID", "Nombre Producto", "Stock", "Descripción/Medida", "En Stock"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Las celdas no son editables directamente en la tabla
            }
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 4) return Boolean.class; // La columna "En Stock" es un booleano para el JCheckBox
                return super.getColumnClass(column);
            }
        };
        tablaProductos = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        tablaProductos.setRowSorter(sorter);
        
        // Ordenación alfabética por nombre de producto (columna 1) por defecto
        sorter.setComparator(1, Comparator.comparing(Object::toString));
        sorter.toggleSortOrder(1); // Ordena inicialmente de forma ascendente

        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        add(scrollPane, BorderLayout.CENTER);

        // Listener para la selección de fila en la tabla
        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tablaProductos.getSelectedRow();
                if (selectedRow != -1) {
                    // Convertir la fila seleccionada de la vista al modelo
                    int modelRow = tablaProductos.convertRowIndexToModel(selectedRow);
                    campoNombreProducto.setText(tableModel.getValueAt(modelRow, 1).toString());
                    campoStock.setText(tableModel.getValueAt(modelRow, 2).toString());
                    campoDescripcionMedida.setText(tableModel.getValueAt(modelRow, 3).toString());
                    checkEnStock.setSelected((Boolean) tableModel.getValueAt(modelRow, 4));
                } else {
                    limpiarCampos();
                }
            }
        });

        // --- Panel Inferior (Formulario y Botones de Acción) ---
        JPanel panelInferior = new JPanel(new BorderLayout(10, 10));
        panelInferior.setBackground(new Color(255, 255, 220));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        // Panel de campos de entrada
        JPanel panelCampos = new JPanel(new GridBagLayout());
        panelCampos.setBackground(new Color(255, 255, 220));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Nombre Producto
        gbc.gridx = 0; gbc.gridy = 0; panelCampos.add(new JLabel("Nombre Producto:"), gbc);
        gbc.gridx = 1; campoNombreProducto = new JTextField(20); panelCampos.add(campoNombreProducto, gbc);

        // Stock
        gbc.gridx = 0; gbc.gridy = 1; panelCampos.add(new JLabel("Stock:"), gbc);
        gbc.gridx = 1; campoStock = new JTextField(10); panelCampos.add(campoStock, gbc);

        // Descripción/Medida
        gbc.gridx = 2; gbc.gridy = 0; panelCampos.add(new JLabel("Descripción/Medida:"), gbc);
        gbc.gridx = 3; campoDescripcionMedida = new JTextField(20); panelCampos.add(campoDescripcionMedida, gbc);

        // En Stock (Checkbox)
        gbc.gridx = 2; gbc.gridy = 1; panelCampos.add(new JLabel("¿En Stock?"), gbc);
        gbc.gridx = 3; checkEnStock = new JCheckBox();
        checkEnStock.setBackground(new Color(255, 255, 220));
        panelCampos.add(checkEnStock, gbc);

        panelInferior.add(panelCampos, BorderLayout.NORTH);

        // Panel de botones de acción
        // Se usa GridLayout para organizar los botones en 2 filas y 3 columnas
        JPanel panelBotones = new JPanel(new GridLayout(2, 3, 15, 10)); // 2 filas, 3 columnas, con espaciado
        panelBotones.setBackground(new Color(255, 255, 220));

        btnAgregar = createStyledButton("AGREGAR", new Color(85, 107, 47)); // Verde
        btnActualizar = createStyledButton("ACTUALIZAR", new Color(70, 130, 180)); // Azul
        btnEliminar = createStyledButton("ELIMINAR", new Color(178, 34, 34)); // Rojo
        btnLimpiarCampos = createStyledButton("LIMPIAR CAMPOS", Color.GRAY); // Gris
        btnGenerarListaSinStock = createStyledButton("LISTA SIN STOCK", new Color(255, 140, 0)); // Naranja
        
        // BOTÓN DE REGRESAR
        btnRegresarPrincipal = createStyledButton("REGRESAR", Color.DARK_GRAY); 
        btnRegresarPrincipal.addActionListener(e -> irAtras()); 

        panelBotones.add(btnAgregar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiarCampos);
        panelBotones.add(btnGenerarListaSinStock);
        panelBotones.add(btnRegresarPrincipal); 

        panelInferior.add(panelBotones, BorderLayout.SOUTH);

        add(panelInferior, BorderLayout.SOUTH);

        // Acciones de los botones
        btnAgregar.addActionListener(e -> agregarProducto());
        btnActualizar.addActionListener(e -> actualizarProducto());
        btnEliminar.addActionListener(e -> eliminarProducto());
        btnLimpiarCampos.addActionListener(e -> limpiarCampos());
        btnGenerarListaSinStock.addActionListener(e -> generarListaSinStock());
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(150, 40));
        button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        button.setFocusPainted(false);
        return button;
    }

    private void cargarProductosTabla() {
        tableModel.setRowCount(0); // Limpiar tabla
        try (Session session = sessionFactory.openSession()) {
            List<Papeleria> productos = session.createQuery("FROM Papeleria ORDER BY nombreProducto ASC", Papeleria.class).list();
            for (Papeleria p : productos) {
                tableModel.addRow(new Object[]{p.getId(), p.getNombreProducto(), p.getStock(), p.getDescripcionMedida(), p.isEnStock()});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar productos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void agregarProducto() {
        String nombre = campoNombreProducto.getText().trim();
        String stockStr = campoStock.getText().trim();
        String descripcion = campoDescripcionMedida.getText().trim();
        boolean enStock = checkEnStock.isSelected();

        if (nombre.isEmpty() || stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del producto y el stock son obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                JOptionPane.showMessageDialog(this, "El stock no puede ser negativo.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Session session = null;
            Transaction transaction = null;
            try {
                session = sessionFactory.openSession();
                transaction = session.beginTransaction();

                // Verificar si ya existe un producto con el mismo nombre
                Papeleria existente = session.createQuery("FROM Papeleria WHERE nombreProducto = :nombre", Papeleria.class)
                                            .setParameter("nombre", nombre)
                                            .uniqueResult();
                if (existente != null) {
                    JOptionPane.showMessageDialog(this, "Ya existe un producto con ese nombre. Por favor, actualice el existente.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Papeleria nuevoProducto = new Papeleria(nombre, stock, descripcion, enStock);
                session.save(nuevoProducto);
                transaction.commit();
                JOptionPane.showMessageDialog(this, "Producto agregado exitosamente.");
                cargarProductosTabla();
                limpiarCampos();
            } catch (Exception ex) {
                if (transaction != null) {
                    transaction.rollback();
                }
                JOptionPane.showMessageDialog(this, "Error al agregar producto: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                if (session != null) {
                    session.close();
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El stock debe ser un número entero válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarProducto() {
        int selectedRowView = tablaProductos.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto para actualizar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedModelRow = tablaProductos.convertRowIndexToModel(selectedRowView);
        Integer idProducto = (Integer) tableModel.getValueAt(selectedModelRow, 0);
        String nombre = campoNombreProducto.getText().trim();
        String stockStr = campoStock.getText().trim();
        String descripcion = campoDescripcionMedida.getText().trim();
        boolean enStock = checkEnStock.isSelected();

        if (nombre.isEmpty() || stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del producto y el stock son obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                JOptionPane.showMessageDialog(this, "El stock no puede ser negativo.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Session session = null;
            Transaction transaction = null;
            try {
                session = sessionFactory.openSession();
                transaction = session.beginTransaction();

                // Verificar si existe otro producto con el mismo nombre (excepto el que estamos editando)
                Papeleria existenteConOtroId = session.createQuery("FROM Papeleria WHERE nombreProducto = :nombre AND id != :id", Papeleria.class)
                                                     .setParameter("nombre", nombre)
                                                     .setParameter("id", idProducto)
                                                     .uniqueResult();
                if (existenteConOtroId != null) {
                    JOptionPane.showMessageDialog(this, "Ya existe otro producto con este nombre.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Papeleria productoAActualizar = session.get(Papeleria.class, idProducto);
                if (productoAActualizar != null) {
                    productoAActualizar.setNombreProducto(nombre);
                    productoAActualizar.setStock(stock);
                    productoAActualizar.setDescripcionMedida(descripcion);
                    productoAActualizar.setEnStock(enStock);
                    session.merge(productoAActualizar);
                    transaction.commit();
                    JOptionPane.showMessageDialog(this, "Producto actualizado exitosamente.");
                    cargarProductosTabla();
                    limpiarCampos();
                } else {
                    JOptionPane.showMessageDialog(this, "Producto no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                if (transaction != null) {
                    transaction.rollback();
                }
                JOptionPane.showMessageDialog(this, "Error al actualizar producto: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                if (session != null) {
                    session.close();
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El stock debe ser un número entero válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarProducto() {
        int selectedRowView = tablaProductos.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedModelRow = tablaProductos.convertRowIndexToModel(selectedRowView);
        Integer idProducto = (Integer) tableModel.getValueAt(selectedModelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea eliminar el producto seleccionado?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Session session = null;
            Transaction transaction = null;
            try {
                session = sessionFactory.openSession();
                transaction = session.beginTransaction();
                Papeleria productoAEliminar = session.get(Papeleria.class, idProducto);
                if (productoAEliminar != null) {
                    session.delete(productoAEliminar);
                    transaction.commit();
                    JOptionPane.showMessageDialog(this, "Producto eliminado exitosamente.");
                    cargarProductosTabla();
                    limpiarCampos();
                } else {
                    JOptionPane.showMessageDialog(this, "Producto no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                if (transaction != null) {
                    transaction.rollback();
                }
                JOptionPane.showMessageDialog(this, "Error al eliminar producto: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                if (session != null) {
                    session.close();
                }
            }
        }
    }

    private void limpiarCampos() {
        campoNombreProducto.setText("");
        campoStock.setText("");
        campoDescripcionMedida.setText("");
        checkEnStock.setSelected(false);
        tablaProductos.clearSelection();
    }

    private void aplicarFiltro() {
        String textoBusqueda = campoBuscador.getText().trim();
        if (textoBusqueda.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + textoBusqueda, 1)); // Filtra por la columna "Nombre Producto" (índice 1) ignorando mayúsculas/minúsculas
        }
    }

    private void generarListaSinStock() {
        List<String> productosSinStock = tableModel.getDataVector().stream()
                .filter(row -> !(Boolean) row.get(4)) // Filtra donde 'En Stock' es falso
                .map(row -> row.get(1) + " (Stock: " + row.get(2) + ", " + row.get(3) + ")") // Formato: Nombre (Stock: X, Desc)
                .collect(Collectors.toList());

        if (productosSinStock.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay productos sin stock en la lista.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Crear el contenido del archivo
        StringBuilder contenidoArchivo = new StringBuilder("Lista de Productos Sin Stock (Fecha: " + java.time.LocalDate.now() + "):\n\n");
        productosSinStock.forEach(p -> contenidoArchivo.append("- ").append(p).append("\n"));

        // Guardar en un archivo de texto
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Lista de Productos Sin Stock");
        fileChooser.setSelectedFile(new java.io.File("productos_sin_stock.txt")); // Nombre de archivo por defecto
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.write(contenidoArchivo.toString());
                JOptionPane.showMessageDialog(this, "Lista de productos sin stock guardada en:\n" + fileToSave.getAbsolutePath(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void irAtras() {
        if (ventanaAnterior != null) {
            ventanaAnterior.setVisible(true); // Muestra la ventana principal
        }
        dispose(); // Cierra esta ventana
    }
}