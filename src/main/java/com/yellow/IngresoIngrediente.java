package com.yellow;

 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.SessionFactory;
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 import java.awt.Font;

 public class IngresoIngrediente extends JFrame {

     private JTextField articuloField;
     private JRadioButton unidadRadio;
     private JRadioButton litrosRadio;
     private JRadioButton kilogramosRadio;
     private JRadioButton especialRadio;
     private JTextField cantidadField;
     private JTextField precioField;

     private JButton ingresarButton;
     private JButton salirButton;
     private JButton limpiarButton;
     private JButton verIngredientesButton; // ¡Este es el nuevo botón!

     private JComboBox<Ingrediente> selectorIngrediente;
     private Ingrediente ingredienteActual;

     private JFrame ventanaAnterior;
     private SessionFactory sessionFactory;
     private ButtonGroup unidadGroup;

     public IngresoIngrediente(JFrame ventanaAnterior, SessionFactory sessionFactory) {
         this.ventanaAnterior = ventanaAnterior;
         this.sessionFactory = sessionFactory;
         initComponents();
         cargarIngredientesEnSelector();
     }

     private void initComponents() {
         setTitle("Ingreso/Actualización de Ingredientes");
         setSize(450, 450);
         setLocationRelativeTo(null);
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         setLayout(null);

         JLabel tituloLabel = new JLabel("GESTIÓN DE INGREDIENTES");
         tituloLabel.setBounds(20, 10, 400, 30);
         tituloLabel.setFont(new Font("Arial", Font.BOLD, 20));
         add(tituloLabel);

         JLabel selectorLabel = new JLabel("Seleccionar Ingrediente:");
         selectorLabel.setBounds(20, 50, 150, 25);
         add(selectorLabel);
         selectorIngrediente = new JComboBox<>();
         selectorIngrediente.setBounds(180, 50, 200, 25);
         selectorIngrediente.addActionListener(e -> cargarDatosIngredienteSeleccionado());
         add(selectorIngrediente);

         JLabel articuloLabel = new JLabel("Artículo (Nombre):");
         articuloLabel.setBounds(20, 90, 150, 25);
         articuloField = new JTextField();
         articuloField.setBounds(180, 90, 200, 25);

         JLabel unidadLabel = new JLabel("Unidad:");
         unidadLabel.setBounds(20, 130, 100, 25);

         unidadRadio = new JRadioButton("Unidad");
         unidadRadio.setBounds(40, 160, 100, 25);
         litrosRadio = new JRadioButton("Litros");
         litrosRadio.setBounds(40, 190, 100, 25);
         kilogramosRadio = new JRadioButton("Kilogramos");
         kilogramosRadio.setBounds(40, 220, 100, 25);
         especialRadio = new JRadioButton("Especial");
         especialRadio.setBounds(40, 250, 100, 25);

         unidadGroup = new ButtonGroup();
         unidadGroup.add(unidadRadio);
         unidadGroup.add(litrosRadio);
         unidadGroup.add(kilogramosRadio);
         unidadGroup.add(especialRadio);

         JLabel cantidadLabel = new JLabel("Cantidad (Peso/Lt R):");
         cantidadLabel.setBounds(150, 160, 150, 25);
         cantidadField = new JTextField();
         cantidadField.setBounds(300, 160, 100, 25);

         JLabel precioLabel = new JLabel("Costo Unitario:");
         precioLabel.setBounds(150, 200, 150, 25);
         precioField = new JTextField();
         precioField.setBounds(300, 200, 100, 25);

         ingresarButton = new JButton("GUARDAR / ACTUALIZAR");
         ingresarButton.setBounds(20, 300, 200, 35);

         limpiarButton = new JButton("NUEVO / LIMPIAR");
         limpiarButton.setBounds(230, 300, 180, 35);

         salirButton = new JButton("SALIR");
         salirButton.setBounds(20, 350, 150, 35);

         // Definimos el nuevo botón y su posición
         verIngredientesButton = new JButton("VER INGREDIENTES");
         verIngredientesButton.setBounds(180, 350, 230, 35); // Lo coloqué al lado del botón SALIR

         ingresarButton.addActionListener(e -> guardarOActualizarIngrediente());
         limpiarButton.addActionListener(e -> limpiarCampos());
         salirButton.addActionListener(e -> salir());
         verIngredientesButton.addActionListener(e -> verIngredientes()); // ¡Nueva acción para el botón!

         add(articuloLabel);
         add(articuloField);
         add(unidadLabel);
         add(unidadRadio);
         add(litrosRadio);
         add(kilogramosRadio);
         add(especialRadio);
         add(cantidadLabel);
         add(cantidadField);
         add(precioLabel);
         add(precioField);
         add(ingresarButton);
         add(limpiarButton);
         add(salirButton);
         add(verIngredientesButton); // ¡Añadimos el nuevo botón a la ventana!
     }

     private void cargarIngredientesEnSelector() {
         selectorIngrediente.removeAllItems();
         selectorIngrediente.addItem(null);
         try (Session session = sessionFactory.openSession()) {
             List<Ingrediente> ingredientes = session.createQuery("FROM Ingrediente", Ingrediente.class).list();
             for (Ingrediente ing : ingredientes) {
                 selectorIngrediente.addItem(ing);
             }
         } catch (Exception e) {
             JOptionPane.showMessageDialog(this, "Error al cargar ingredientes en el selector: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
             e.printStackTrace();
         }
     }

     private void cargarDatosIngredienteSeleccionado() {
         ingredienteActual = (Ingrediente) selectorIngrediente.getSelectedItem();
         if (ingredienteActual != null) {
             articuloField.setText(ingredienteActual.getNombre());
             cantidadField.setText(String.valueOf(ingredienteActual.getPesoLtR()));
             precioField.setText(String.valueOf(ingredienteActual.getCostoUnitario()));

             unidadGroup.clearSelection();
             String tipo = ingredienteActual.getTipoPesoLt();
             if ("Unidad".equals(tipo)) {
                 unidadRadio.setSelected(true);
             } else if ("Litros".equals(tipo)) {
                 litrosRadio.setSelected(true);
             } else if ("Kilogramos".equals(tipo)) {
                 kilogramosRadio.setSelected(true);
             } else if ("Especial".equals(tipo)) {
                 especialRadio.setSelected(true);
             }
         } else {
             limpiarCampos();
         }
     }

     private void guardarOActualizarIngrediente() {
         String producto = articuloField.getText();
         String tipo = getTipoSeleccionado();
         String precioText = precioField.getText();
         String cantidadText = cantidadField.getText();

         if (producto.isEmpty() || tipo == null || precioText.isEmpty() || cantidadText.isEmpty()) {
             JOptionPane.showMessageDialog(this, "Todos los campos (Artículo, Unidad, Cantidad, Precio) son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
             return;
         }

         try {
             double precio = Double.parseDouble(precioText);
             double cantidad = Double.parseDouble(cantidadText);

             Session session = null;
             Transaction transaction = null;
             try {
                 session = sessionFactory.openSession();
                 transaction = session.beginTransaction();

                 if (ingredienteActual == null) {
                     Ingrediente nuevoIngrediente = new Ingrediente(producto, tipo, cantidad, precio, 0.0);
                     session.save(nuevoIngrediente);
                     JOptionPane.showMessageDialog(this, "Nuevo ingrediente ingresado exitosamente.");
                 } else {
                     ingredienteActual.setNombre(producto);
                     ingredienteActual.setTipoPesoLt(tipo);
                     ingredienteActual.setPesoLtR(cantidad);
                     ingredienteActual.setCostoUnitario(precio);

                     session.merge(ingredienteActual);
                     JOptionPane.showMessageDialog(this, "Ingrediente actualizado exitosamente.");
                 }
                 transaction.commit();

                 cargarIngredientesEnSelector();
                 limpiarCampos();

             } catch (Exception ex) {
                 if (transaction != null) {
                     transaction.rollback();
                 }
                 JOptionPane.showMessageDialog(this, "Error al guardar/actualizar en la base de datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                 ex.printStackTrace();
             } finally {
                 if (session != null) {
                     session.close();
                 }
             }

         } catch (NumberFormatException ex) {
             JOptionPane.showMessageDialog(this, "Cantidad y Costo Unitario deben ser valores numéricos válidos.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
         }
     }

     private void limpiarCampos() {
         articuloField.setText("");
         cantidadField.setText("");
         precioField.setText("");
         unidadGroup.clearSelection();
         selectorIngrediente.setSelectedItem(null);
         ingredienteActual = null;
     }

     private String getTipoSeleccionado() {
         if (unidadRadio.isSelected()) {
             return "Unidad";
         } else if (litrosRadio.isSelected()) {
             return "Litros";
         } else if (kilogramosRadio.isSelected()) {
             return "Kilogramos";
         } else if (especialRadio.isSelected()) {
             return "Especial";
         }
         return null;
     }

     // Método para abrir la nueva ventana de VisualizarIngredientes
     private void verIngredientes() {
         this.setVisible(false); // Oculta la ventana actual (IngresoIngrediente)
         // Creamos la nueva ventana, pasándole la ventana actual y la sessionFactory
         VisualizarIngredientes visualizarIngredientes = new VisualizarIngredientes(this, sessionFactory);
         visualizarIngredientes.setVisible(true); // Hacemos visible la nueva ventana
     }

     private void salir() {
         if (ventanaAnterior != null) {
             ventanaAnterior.setVisible(true);
         }
         dispose();
     }
 }