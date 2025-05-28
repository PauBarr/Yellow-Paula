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
         cargarDatosTabla(); // Llama a este método para llenar la tabla al iniciar
     }

     private void initComponents() {
         setTitle("Visualizar Ingredientes Existentes");
         setSize(700, 500); // Tamaño de la ventana adecuado para una tabla
         setLocationRelativeTo(null); // Centra la ventana en la pantalla
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cierra solo esta ventana
         setLayout(new BorderLayout()); // Usa un diseño BorderLayout para organizar los componentes

         // Título de la pantalla
         JLabel tituloLabel = new JLabel("LISTA DE INGREDIENTES", SwingConstants.CENTER); // Centra el texto
         tituloLabel.setFont(new Font("Arial", Font.BOLD, 20)); // Fuente y tamaño del título
         add(tituloLabel, BorderLayout.NORTH); // Lo coloca en la parte superior de la ventana

         // Configurar el modelo de la tabla (columnas y datos)
         String[] columnNames = {"ID", "Artículo", "Unidad", "Cantidad (Peso/LtR)", "Costo Unitario"};
         tableModel = new DefaultTableModel(columnNames, 0) {
             @Override
             public boolean isCellEditable(int row, int column) {
                 return false; // Hace que ninguna celda de la tabla sea editable por el usuario
             }
         };
         ingredientesTable = new JTable(tableModel); // Crea la tabla con el modelo
         JScrollPane scrollPane = new JScrollPane(ingredientesTable); // Añade un scroll si hay muchos datos
         add(scrollPane, BorderLayout.CENTER); // Coloca la tabla en el centro de la ventana

         // Panel para los botones en la parte inferior
         JPanel buttonPanel = new JPanel();
         eliminarButton = new JButton("ELIMINAR INGREDIENTE SELECCIONADO");
         atrasButton = new JButton("ATRÁS");

         buttonPanel.add(eliminarButton);
         buttonPanel.add(atrasButton);
         add(buttonPanel, BorderLayout.SOUTH); // Coloca el panel de botones en la parte inferior

         // Acciones de los botones
         eliminarButton.addActionListener(e -> eliminarIngredienteSeleccionado());
         atrasButton.addActionListener(e -> irAtras());
     }

     // Método para cargar los datos de los ingredientes desde la base de datos a la tabla
     private void cargarDatosTabla() {
         tableModel.setRowCount(0); // Vacía la tabla completamente antes de volver a llenarla
         try (Session session = sessionFactory.openSession()) { // Abre una sesión de Hibernate
             // Consulta todos los ingredientes de la base de datos
             List<Ingrediente> ingredientes = session.createQuery("FROM Ingrediente", Ingrediente.class).list();
             for (Ingrediente ing : ingredientes) { // Recorre cada ingrediente de la lista
                 Object[] rowData = { // Crea una fila de datos para la tabla
                     ing.getId(),
                     ing.getNombre(),
                     ing.getTipoPesoLt(),
                     ing.getPesoLtR(),
                     ing.getCostoUnitario()
                 };
                 tableModel.addRow(rowData); // Añade la fila a la tabla
             }
         } catch (Exception e) {
             JOptionPane.showMessageDialog(this, "Error al cargar ingredientes en la tabla: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
             e.printStackTrace();
         }
     }

     // Método para eliminar el ingrediente que el usuario ha seleccionado en la tabla
     private void eliminarIngredienteSeleccionado() {
         int selectedRow = ingredientesTable.getSelectedRow(); // Obtiene la fila seleccionada
         if (selectedRow == -1) { // Si no hay ninguna fila seleccionada
             JOptionPane.showMessageDialog(this, "Por favor, seleccione un ingrediente para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
             return; // Sale del método
         }

         // Pide confirmación al usuario antes de eliminar
         int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea eliminar el ingrediente seleccionado?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
         if (confirm == JOptionPane.YES_OPTION) { // Si el usuario confirma la eliminación
             // Obtiene el ID del ingrediente de la primera columna de la fila seleccionada
             Long ingredienteId = (Long) tableModel.getValueAt(selectedRow, 0);

             Session session = null;
             Transaction transaction = null;
             try {
                 session = sessionFactory.openSession(); // Abre una nueva sesión
                 transaction = session.beginTransaction(); // Inicia una transacción

                 Ingrediente ingrediente = session.get(Ingrediente.class, ingredienteId); // Busca el ingrediente por su ID
                 if (ingrediente != null) {
                     session.delete(ingrediente); // Elimina el ingrediente
                     transaction.commit(); // Confirma la transacción
                     JOptionPane.showMessageDialog(this, "Ingrediente eliminado exitosamente.");
                     cargarDatosTabla(); // Vuelve a cargar la tabla para mostrar el cambio
                 } else {
                     JOptionPane.showMessageDialog(this, "Ingrediente no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
                 }
             } catch (Exception ex) {
                 if (transaction != null) {
                     transaction.rollback(); // Si hay un error, revierte la transacción
                 }
                 JOptionPane.showMessageDialog(this, "Error al eliminar el ingrediente: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                 ex.printStackTrace();
             } finally {
                 if (session != null) {
                     session.close(); // Cierra la sesión de Hibernate
                 }
             }
         }
     }

     // Método para volver a la ventana anterior (IngresoIngrediente)
     private void irAtras() {
         if (ventanaAnterior != null) {
             ventanaAnterior.setVisible(true); // Hace visible la ventana anterior
         }
         dispose(); // Cierra la ventana actual (VisualizarIngredientes)
     }
 }