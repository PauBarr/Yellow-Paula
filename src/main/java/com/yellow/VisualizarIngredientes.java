package com.yellow;

 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.SessionFactory;
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 import java.awt.Font;
 import java.awt.BorderLayout;

 public class VisualizarIngredientes extends JFrame {

     private JTable ingredientesTable;
     private DefaultTableModel tableModel;
     private JButton eliminarButton;
     private JButton atrasButton;

     private JFrame ventanaAnterior;
     private SessionFactory sessionFactory;

     public VisualizarIngredientes(JFrame ventanaAnterior, SessionFactory sessionFactory) {
         this.ventanaAnterior = ventanaAnterior;
         this.sessionFactory = sessionFactory;
         initComponents();
         cargarDatosTabla();
     }

     private void initComponents() {
         setTitle("Visualizar Ingredientes Existentes");
         setSize(800, 500); // Ajustar tamaño para nuevas columnas
         setLocationRelativeTo(null);
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         setLayout(new BorderLayout());

         JLabel tituloLabel = new JLabel("LISTA DE INGREDIENTES", SwingConstants.CENTER);
         tituloLabel.setFont(new Font("Arial", Font.BOLD, 20));
         add(tituloLabel, BorderLayout.NORTH);

         // Configurar el modelo de la tabla (columnas actualizadas)
         String[] columnNames = {"ID", "Artículo", "Unidad de Compra", "Cantidad de Compra", "Costo de Compra", "Costo Unitario (Calculado)"};
         tableModel = new DefaultTableModel(columnNames, 0) {
             @Override
             public boolean isCellEditable(int row, int column) {
                 return false;
             }
         };
         ingredientesTable = new JTable(tableModel);
         JScrollPane scrollPane = new JScrollPane(ingredientesTable);
         add(scrollPane, BorderLayout.CENTER);

         JPanel buttonPanel = new JPanel();
         eliminarButton = new JButton("ELIMINAR INGREDIENTE SELECCIONADO");
         atrasButton = new JButton("ATRÁS");

         buttonPanel.add(eliminarButton);
         buttonPanel.add(atrasButton);
         add(buttonPanel, BorderLayout.SOUTH);

         eliminarButton.addActionListener(e -> eliminarIngredienteSeleccionado());
         atrasButton.addActionListener(e -> irAtras());
     }

     private void cargarDatosTabla() {
         tableModel.setRowCount(0);
         try (Session session = sessionFactory.openSession()) {
             List<Ingrediente> ingredientes = session.createQuery("FROM Ingrediente", Ingrediente.class).list();
             for (Ingrediente ing : ingredientes) {
                 Object[] rowData = {
                     ing.getId(),
                     ing.getNombre(),
                     ing.getTipoPesoLt(), // Unidad de Compra
                     ing.getCantidadDeCompra(), // Cantidad del paquete/unidad
                     ing.getCostoDeCompra(), // Costo del paquete/unidad
                     String.format("$%.2f", ing.getCostoUnitarioCalculado()) // Costo Unitario CALCULADO
                 };
                 tableModel.addRow(rowData);
             }
         } catch (Exception e) {
             JOptionPane.showMessageDialog(this, "Error al cargar ingredientes en la tabla: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
             e.printStackTrace();
         }
     }

     private void eliminarIngredienteSeleccionado() {
         int selectedRow = ingredientesTable.getSelectedRow();
         if (selectedRow == -1) {
             JOptionPane.showMessageDialog(this, "Por favor, seleccione un ingrediente para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
             return;
         }

         int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea eliminar el ingrediente seleccionado?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
         if (confirm == JOptionPane.YES_OPTION) {
             // El ID de la tabla se obtiene como Integer, asegúrate que sea el tipo correcto (int)
             Integer ingredienteId = (Integer) tableModel.getValueAt(selectedRow, 0);

             Session session = null;
             Transaction transaction = null;
             try {
                 session = sessionFactory.openSession();
                 transaction = session.beginTransaction();

                 Ingrediente ingrediente = session.get(Ingrediente.class, ingredienteId);
                 if (ingrediente != null) {
                     session.delete(ingrediente);
                     transaction.commit();
                     JOptionPane.showMessageDialog(this, "Ingrediente eliminado exitosamente.");
                     cargarDatosTabla();
                 } else {
                     JOptionPane.showMessageDialog(this, "Ingrediente no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
                 }
             } catch (Exception ex) {
                 if (transaction != null) {
                     transaction.rollback();
                 }
                 JOptionPane.showMessageDialog(this, "Error al eliminar el ingrediente: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                 ex.printStackTrace();
             } finally {
                 if (session != null) {
                     session.close();
                 }
             }
         }
     }

     private void irAtras() {
         if (ventanaAnterior != null) {
             ventanaAnterior.setVisible(true);
         }
         dispose();
     }
 }