package com.yellow;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

// MODIFICADO: Ahora extiende JPanel
public class GestionCategoriasScreen extends JPanel {

    private SessionFactory sessionFactory;
    // ELIMINADO: private JFrame ventanaAnterior; // Ya no es JFrame, ahora es IngresoReceta
    private IngresoReceta ventanaPadre; // AÑADIDO: Referencia a la ventana padre (IngresoReceta)

    // Componentes para la gestión de categorías
    private JTable categoriaTable;
    private DefaultTableModel categoriaTableModel;
    private JTextField categoriaIdField;
    private JTextField categoriaNombreField;
    private JButton btnAgregarCategoria;
    private JButton btnEditarCategoria;
    private JButton btnEliminarCategoria;
    private JButton btnVerRecetasCategoria;

    // MODIFICADO: Constructor ahora recibe IngresoReceta como padre
    public GestionCategoriasScreen(IngresoReceta ventanaPadre, SessionFactory sessionFactory) {
        this.ventanaPadre = ventanaPadre; // Guardar la referencia
        this.sessionFactory = sessionFactory;
        initComponents();
        cargarCategoriasTabla();
    }

    private void initComponents() {
        // ELIMINADO: setTitle("Gestión de Categorías - Yellow");
        // ELIMINADO: setSize(600, 500);
        // ELIMINADO: setLocationRelativeTo(null);
        // ELIMINADO: setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // MODIFICADO: Configuración del panel en sí
        setLayout(new BorderLayout());
        setBackground(new Color(255, 255, 220));
        setBorder(new EmptyBorder(10, 10, 10, 10)); // Mantener el borde del panel

        JLabel titleLabel = new JLabel("GESTIÓN DE CATEGORÍAS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 60, 60));
        add(titleLabel, BorderLayout.NORTH); // Añadido al JPanel (este)

        String[] columnNames = {"ID", "Nombre de Categoría"};
        categoriaTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        categoriaTable = new JTable(categoriaTableModel);
        JScrollPane scrollPane = new JScrollPane(categoriaTable);
        add(scrollPane, BorderLayout.CENTER); // Añadido al JPanel (este)

        categoriaTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = categoriaTable.getSelectedRow();
                if (selectedRow != -1) {
                    categoriaIdField.setText(categoriaTableModel.getValueAt(selectedRow, 0).toString());
                    categoriaNombreField.setText(categoriaTableModel.getValueAt(selectedRow, 1).toString());
                    btnVerRecetasCategoria.setEnabled(true);
                } else {
                    limpiarCamposCategoria();
                    btnVerRecetasCategoria.setEnabled(false);
                }
            }
        });

        JPanel inputAndButtonsPanel = new JPanel(new BorderLayout(5, 5));
        inputAndButtonsPanel.setBackground(new Color(255, 255, 220));
        inputAndButtonsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        inputPanel.setBackground(new Color(255, 255, 220));
        inputPanel.add(new JLabel("Nombre de Categoría:"));
        categoriaNombreField = new JTextField(20);
        inputPanel.add(categoriaNombreField);
        categoriaIdField = new JTextField(5);
        categoriaIdField.setEditable(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
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

        btnVerRecetasCategoria = new JButton("Ver Recetas de Categoría");
        btnVerRecetasCategoria.setBackground(new Color(255, 140, 0));
        btnVerRecetasCategoria.setForeground(Color.WHITE);
        btnVerRecetasCategoria.setFont(new Font("Arial", Font.BOLD, 12));
        btnVerRecetasCategoria.setEnabled(false);
        buttonPanel.add(btnVerRecetasCategoria);

        inputAndButtonsPanel.add(inputPanel, BorderLayout.NORTH);
        inputAndButtonsPanel.add(buttonPanel, BorderLayout.CENTER);

        JPanel bottomControlsPanel = new JPanel();
        bottomControlsPanel.setLayout(new BoxLayout(bottomControlsPanel, BoxLayout.Y_AXIS));
        bottomControlsPanel.setBackground(new Color(255, 255, 220));
        bottomControlsPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        bottomControlsPanel.add(inputAndButtonsPanel);
        bottomControlsPanel.add(Box.createVerticalStrut(10));

        JButton atrasButton = new JButton("ATRÁS");
        atrasButton.setBackground(Color.DARK_GRAY);
        atrasButton.setForeground(Color.WHITE);
        atrasButton.setFont(new Font("Arial", Font.BOLD, 14));
        // MODIFICADO: Llama al método de la ventana padre para cambiar de panel
        atrasButton.addActionListener(e -> ventanaPadre.mostrarPanel("principal"));

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setBackground(new Color(255, 255, 220));
        southPanel.add(atrasButton);
        bottomControlsPanel.add(southPanel);

        add(bottomControlsPanel, BorderLayout.SOUTH); // Añadido al JPanel (este)

        // Acciones de los botones de Categorías
        btnAgregarCategoria.addActionListener(e -> agregarCategoria());
        btnEditarCategoria.addActionListener(e -> editarCategoria());
        btnEliminarCategoria.addActionListener(e -> eliminarCategoria());
        btnVerRecetasCategoria.addActionListener(e -> verRecetasDeCategoria());
    }

    // ELIMINADO: irAtras() ya que ahora lo maneja el padre

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
        btnVerRecetasCategoria.setEnabled(false);
    }

    private void verRecetasDeCategoria() {
        int selectedRow = categoriaTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una categoría para ver sus recetas.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer idCategoria = (Integer) categoriaTableModel.getValueAt(selectedRow, 0);
        String nombreCategoria = (String) categoriaTableModel.getValueAt(selectedRow, 1);

        Categoria categoriaSeleccionada = new Categoria();
        categoriaSeleccionada.setIdCategoria(idCategoria);
        categoriaSeleccionada.setNombreCategoria(nombreCategoria);

        // AÑADIDO: Llama al método del padre para mostrar el panel de recetas con filtro
        ventanaPadre.mostrarPanel("verRecetas", categoriaSeleccionada);
    }
}