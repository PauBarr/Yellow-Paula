package com.yellow;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class GestionCategoriasScreen extends JFrame {

    private SessionFactory sessionFactory;
    private JFrame ventanaAnterior;

    // Componentes para la gestión de categorías
    private JTable categoriaTable;
    private DefaultTableModel categoriaTableModel;
    private JTextField categoriaIdField;
    private JTextField categoriaNombreField;
    private JButton btnAgregarCategoria;
    private JButton btnEditarCategoria;
    private JButton btnEliminarCategoria;

    public GestionCategoriasScreen(JFrame ventanaAnterior, SessionFactory sessionFactory) {
        this.ventanaAnterior = ventanaAnterior;
        this.sessionFactory = sessionFactory;
        initComponents();
        cargarCategoriasTabla();
    }

    private void initComponents() {
        setTitle("Gestión de Categorías - Yellow");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(255, 255, 220));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("GESTIÓN DE CATEGORÍAS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 60, 60));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Nombre de Categoría"};
        categoriaTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        categoriaTable = new JTable(categoriaTableModel);
        JScrollPane scrollPane = new JScrollPane(categoriaTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        categoriaTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && categoriaTable.getSelectedRow() != -1) {
                int selectedRow = categoriaTable.getSelectedRow();
                categoriaIdField.setText(categoriaTableModel.getValueAt(selectedRow, 0).toString());
                categoriaNombreField.setText(categoriaTableModel.getValueAt(selectedRow, 1).toString());
            }
        });

        // =====================================================================
        // INICIO DE LA CORRECCIÓN: Asegurar que los componentes se añadan al panel
        // =====================================================================
        JPanel inputAndButtonsPanel = new JPanel(new BorderLayout(5, 5)); // Panel para los campos de texto y botones
        inputAndButtonsPanel.setBackground(new Color(255, 255, 220));
        inputAndButtonsPanel.setBorder(new EmptyBorder(10, 0, 0, 0)); // Pequeño borde superior

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5)); // Panel para el campo de nombre
        inputPanel.setBackground(new Color(255, 255, 220));
        inputPanel.add(new JLabel("Nombre de Categoría:"));
        categoriaNombreField = new JTextField(20);
        inputPanel.add(categoriaNombreField);
        // El categoriaIdField es solo para la lógica interna, no lo mostramos al usuario para que no lo edite
        categoriaIdField = new JTextField(5); // Inicializado aquí, aunque no se añade visiblemente
        categoriaIdField.setEditable(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Panel para los botones CRUD
        buttonPanel.setBackground(new Color(255, 255, 220));

        btnAgregarCategoria = new JButton("Agregar");
        btnAgregarCategoria.setBackground(new Color(85, 107, 47));
        btnAgregarCategoria.setForeground(Color.WHITE);
        buttonPanel.add(btnAgregarCategoria);

        btnEditarCategoria = new JButton("Editar");
        btnEditarCategoria.setBackground(new Color(70, 130, 180));
        btnEditarCategoria.setForeground(Color.WHITE);
        buttonPanel.add(btnEditarCategoria);

        btnEliminarCategoria = new JButton("Eliminar");
        btnEliminarCategoria.setBackground(new Color(178, 34, 34));
        btnEliminarCategoria.setForeground(Color.WHITE);
        buttonPanel.add(btnEliminarCategoria);

        inputAndButtonsPanel.add(inputPanel, BorderLayout.NORTH);
        inputAndButtonsPanel.add(buttonPanel, BorderLayout.CENTER);

        mainPanel.add(inputAndButtonsPanel, BorderLayout.SOUTH); // Añadir el nuevo panel al SUR del mainPanel

        // =====================================================================
        // FIN DE LA CORRECCIÓN
        // =====================================================================


        // Botón "Atrás" (se mantiene al final, fuera del inputAndButtonsPanel)
        JButton atrasButton = new JButton("ATRÁS");
        atrasButton.setBackground(Color.DARK_GRAY);
        atrasButton.setForeground(Color.WHITE);
        atrasButton.setFont(new Font("Arial", Font.BOLD, 14));
        atrasButton.addActionListener(e -> irAtras());

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setBackground(new Color(255, 255, 220));
        southPanel.add(atrasButton);
        mainPanel.add(southPanel, BorderLayout.SOUTH); // El botón de Atrás se añade al SOUTH del mainPanel, lo cual está bien.
                                                      // Reorganizamos para que inputAndButtonsPanel esté en BorderLayout.SOUTH del mainPanel,
                                                      // y luego southPanel (con el botón Atrás) se añade después de inputAndButtonsPanel.
                                                      // Para que el botón ATRÁS esté debajo de los botones de CRUD:

        // Nueva estructura para la parte inferior:
        JPanel bottomControlsPanel = new JPanel();
        bottomControlsPanel.setLayout(new BoxLayout(bottomControlsPanel, BoxLayout.Y_AXIS));
        bottomControlsPanel.setBackground(new Color(255, 255, 220));
        bottomControlsPanel.setBorder(new EmptyBorder(0, 10, 10, 10)); // Más espacio

        bottomControlsPanel.add(inputAndButtonsPanel); // Primero los campos y botones CRUD
        bottomControlsPanel.add(Box.createVerticalStrut(10)); // Espacio vertical
        bottomControlsPanel.add(southPanel); // Luego el botón ATRÁS

        mainPanel.add(bottomControlsPanel, BorderLayout.SOUTH); // Añadir el panel combinado al SUR del mainPanel


        add(mainPanel);

        // Acciones de los botones de Categorías
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

    // --- Métodos CRUD para Categorías (estos ya estaban correctos) ---

    private void cargarCategoriasTabla() {
        categoriaTableModel.setRowCount(0);
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
                long count = (long) session.createQuery("SELECT count(*) FROM Categoria WHERE nombreCategoria = :nombre AND idCategoria != :id")
                                          .setParameter("nombre", nuevoNombre)
                                          .setParameter("id", idCategoria)
                                          .uniqueResult();
                if (count > 0) {
                    JOptionPane.showMessageDialog(this, "Ya existe otra categoría con ese nombre.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                categoriaAEditar.setNombreCategoria(nuevoNombre);
                session.merge(categoriaAEditar);
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
                    // Antes de eliminar la categoría, debemos asegurarnos de que no esté asociada a ninguna receta.
                    // Si está asociada, la eliminación fallará o causará un error de integridad referencial.
                    // Podemos removerla de las recetas asociadas primero, o simplemente informar al usuario.
                    // Por ahora, el error de la DB ya lo manejará.

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
                // Mensaje más útil si la eliminación falla por FK
                if (ex.getMessage() != null && ex.getMessage().contains("Cannot delete or update a parent row: a foreign key constraint fails")) {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar la categoría. Probablemente esté asociada a una o más recetas.", "Error de Eliminación", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Error al eliminar categoría: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
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