package com.yellow;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class PantallaConfiguracion extends JFrame {

    private SessionFactory sessionFactory;
    private JFrame ventanaAnterior;

    // Componentes para la gestión de categorías
    private JTable categoriaTable;
    private DefaultTableModel categoriaTableModel;
    private JTextField categoriaIdField; // Para edición/eliminación (opcional, o se usa la tabla)
    private JTextField categoriaNombreField;
    private JButton btnAgregarCategoria;
    private JButton btnEditarCategoria;
    private JButton btnEliminarCategoria;

    public PantallaConfiguracion(JFrame ventanaAnterior, SessionFactory sessionFactory) {
        this.ventanaAnterior = ventanaAnterior;
        this.sessionFactory = sessionFactory;
        initComponents();
        cargarCategoriasTabla(); // Cargar datos al iniciar
    }

    private void initComponents() {
        setTitle("Configuración - Yellow");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(255, 255, 220));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("CONFIGURACIÓN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 60, 60));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // --- Panel de Contenido Central (Aquí irán las pestañas o secciones) ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(255, 255, 220)); // Pestañas con color amarillo claro

        // --- Pestaña de Gestión de Categorías ---
        JPanel categoriaPanel = new JPanel(new BorderLayout(10, 10));
        categoriaPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        categoriaPanel.setBackground(Color.WHITE); // Fondo blanco para la pestaña

        // Tabla de Categorías
        String[] columnNames = {"ID", "Nombre de Categoría"};
        categoriaTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No permitir edición directa en la tabla
            }
        };
        categoriaTable = new JTable(categoriaTableModel);
        JScrollPane scrollPane = new JScrollPane(categoriaTable);
        categoriaPanel.add(scrollPane, BorderLayout.CENTER);

        // Listener para seleccionar fila en la tabla
        categoriaTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && categoriaTable.getSelectedRow() != -1) {
                int selectedRow = categoriaTable.getSelectedRow();
                categoriaIdField.setText(categoriaTableModel.getValueAt(selectedRow, 0).toString());
                categoriaNombreField.setText(categoriaTableModel.getValueAt(selectedRow, 1).toString());
            }
        });


        // Panel para ingreso y botones de Categorías
        JPanel inputButtonPanel = new JPanel(new GridBagLayout());
        inputButtonPanel.setBackground(Color.WHITE); // Mismo fondo que la pestaña
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel nombreLabel = new JLabel("Nombre:");
        gbc.gridx = 0; gbc.gridy = 0; inputButtonPanel.add(nombreLabel, gbc);

        categoriaNombreField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 0; inputButtonPanel.add(categoriaNombreField, gbc);

        // Campo ID para edición/eliminación (oculto o no editable si la selección es desde la tabla)
        categoriaIdField = new JTextField(5);
        categoriaIdField.setEditable(false); // No se edita manualmente
        // gb.gridx = 2; gb.gridy = 0; inputButtonPanel.add(categoriaIdField, gbc); // Si decides mostrarlo

        btnAgregarCategoria = new JButton("Agregar");
        btnAgregarCategoria.setBackground(new Color(85, 107, 47)); // Verde oliva
        btnAgregarCategoria.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 1; inputButtonPanel.add(btnAgregarCategoria, gbc);

        btnEditarCategoria = new JButton("Editar");
        btnEditarCategoria.setBackground(new Color(70, 130, 180)); // Azul acero
        btnEditarCategoria.setForeground(Color.WHITE);
        gbc.gridx = 1; gbc.gridy = 1; inputButtonPanel.add(btnEditarCategoria, gbc);

        btnEliminarCategoria = new JButton("Eliminar");
        btnEliminarCategoria.setBackground(new Color(178, 34, 34)); // Rojo fuego
        btnEliminarCategoria.setForeground(Color.WHITE);
        gbc.gridx = 2; gbc.gridy = 1; inputButtonPanel.add(btnEliminarCategoria, gbc);

        categoriaPanel.add(inputButtonPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Categorías", categoriaPanel);


        // --- Pestañas Futuras (Unidades, Moneda, Respaldo, etc.) ---
        tabbedPane.addTab("Unidades de Medida", new JPanel()); // Placeholder
        tabbedPane.addTab("Moneda y Formato", new JPanel()); // Placeholder
        tabbedPane.addTab("Respaldo/Restauración", new JPanel()); // Placeholder
        tabbedPane.addTab("Personalización Visual", new JPanel()); // Placeholder
        tabbedPane.addTab("Acerca de", new JPanel()); // Placeholder


        mainPanel.add(tabbedPane, BorderLayout.CENTER); // Añadir el JTabbedPane al panel principal

        // Botón "Atrás"
        JButton atrasButton = new JButton("ATRÁS");
        atrasButton.setBackground(Color.DARK_GRAY);
        atrasButton.setForeground(Color.WHITE);
        atrasButton.setFont(new Font("Arial", Font.BOLD, 14));
        atrasButton.addActionListener(e -> irAtras());

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setBackground(new Color(255, 255, 220));
        southPanel.add(atrasButton);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // --- Acciones de los botones de Categorías ---
        btnAgregarCategoria.addActionListener(e -> agregarCategoria());
        btnEditarCategoria.addActionListener(e -> editarCategoria());
        btnEliminarCategoria.addActionListener(e -> eliminarCategoria());
    }

    private void irAtras() {
        if (ventanaAnterior != null) {
            ventanaAnterior.setVisible(true);
        }
        dispose();
    }

    // --- Métodos CRUD para Categorías ---

    private void cargarCategoriasTabla() {
        categoriaTableModel.setRowCount(0); // Limpiar tabla
        try (Session session = sessionFactory.openSession()) {
            List<Categoria> categorias = session.createQuery("FROM Categoria", Categoria.class).list();
            for (Categoria cat : categorias) {
                categoriaTableModel.addRow(new Object[]{cat.getIdCategoria(), cat.getNombreCategoria()});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar categorías: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void agregarCategoria() {
        String nombre = categoriaNombreField.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre de la categoría no puede estar vacío.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            // Verificar si ya existe una categoría con ese nombre
            long count = (long) session.createQuery("SELECT count(*) FROM Categoria WHERE nombreCategoria = :nombre")
                                      .setParameter("nombre", nombre)
                                      .uniqueResult();
            if (count > 0) {
                JOptionPane.showMessageDialog(this, "Ya existe una categoría con ese nombre.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Categoria nuevaCategoria = new Categoria();
            nuevaCategoria.setNombreCategoria(nombre);
            session.save(nuevaCategoria);
            transaction.commit();
            JOptionPane.showMessageDialog(this, "Categoría agregada exitosamente.");
            limpiarCamposCategoria();
            cargarCategoriasTabla();
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            JOptionPane.showMessageDialog(this, "Error al agregar categoría: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void editarCategoria() {
        int selectedRow = categoriaTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una categoría para editar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer idCategoria = (Integer) categoriaTableModel.getValueAt(selectedRow, 0);
        String nuevoNombre = categoriaNombreField.getText().trim();

        if (nuevoNombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre de la categoría no puede estar vacío.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            Categoria categoriaAEditar = session.get(Categoria.class, idCategoria);
            if (categoriaAEditar != null) {
                // Verificar si el nuevo nombre ya existe en otra categoría
                long count = (long) session.createQuery("SELECT count(*) FROM Categoria WHERE nombreCategoria = :nombre AND idCategoria != :id")
                                          .setParameter("nombre", nuevoNombre)
                                          .setParameter("id", idCategoria)
                                          .uniqueResult();
                if (count > 0) {
                    JOptionPane.showMessageDialog(this, "Ya existe otra categoría con ese nombre.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                categoriaAEditar.setNombreCategoria(nuevoNombre);
                session.merge(categoriaAEditar); // Usar merge para actualizar
                transaction.commit();
                JOptionPane.showMessageDialog(this, "Categoría actualizada exitosamente.");
                limpiarCamposCategoria();
                cargarCategoriasTabla();
            } else {
                JOptionPane.showMessageDialog(this, "Categoría no encontrada.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            JOptionPane.showMessageDialog(this, "Error al editar categoría: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void eliminarCategoria() {
        int selectedRow = categoriaTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una categoría para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer idCategoria = (Integer) categoriaTableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea eliminar esta categoría? Esto podría afectar recetas.", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Session session = null;
            Transaction transaction = null;
            try {
                session = sessionFactory.openSession();
                transaction = session.beginTransaction();

                Categoria categoriaAEliminar = session.get(Categoria.class, idCategoria);
                if (categoriaAEliminar != null) {
                    session.delete(categoriaAEliminar);
                    transaction.commit();
                    JOptionPane.showMessageDialog(this, "Categoría eliminada exitosamente.");
                    limpiarCamposCategoria();
                    cargarCategoriasTabla();
                } else {
                    JOptionPane.showMessageDialog(this, "Categoría no encontrada.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                if (transaction != null) {
                    transaction.rollback();
                }
                JOptionPane.showMessageDialog(this, "Error al eliminar categoría: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                if (session != null) {
                    session.close();
                }
            }
        }
    }

    private void limpiarCamposCategoria() {
        categoriaIdField.setText("");
        categoriaNombreField.setText("");
        categoriaTable.clearSelection();
    }
}