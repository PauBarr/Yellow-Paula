package com.yellow;

 import org.hibernate.SessionFactory;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;

 public class IngresoReceta extends JFrame {

     private SessionFactory sessionFactory;

     public IngresoReceta(SessionFactory sessionFactory) {
         this.sessionFactory = sessionFactory;

         setTitle("Gestión de Recetas");
         setSize(400, 400); // Aumentado el tamaño para acomodar el nuevo botón
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         setLocationRelativeTo(null);

         // Crear botones
         JButton btnNuevaReceta = new JButton("Nueva receta");
         JButton btnRecetas = new JButton("Ver / Actualizar Recetas");
         JButton btnGestionarCategorias = new JButton("Gestionar Categorías"); // Nuevo botón
         JButton btnRegresar = new JButton("Regresar");

         // Asignar acciones a los botones
         btnNuevaReceta.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 nuevaReceta();
             }
         });

         btnRecetas.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 mostrarYActualizarRecetas();
             }
         });

         // Acción para el nuevo botón "Gestionar Categorías"
         btnGestionarCategorias.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 dispose(); // Oculta la ventana actual (IngresoReceta)
                 // Abre la nueva ventana de gestión de categorías
                 // Pasamos 'this' para que GestionCategoriasScreen pueda regresar a IngresoReceta
                 GestionCategoriasScreen gestionCategorias = new GestionCategoriasScreen(IngresoReceta.this, sessionFactory);
                 gestionCategorias.setVisible(true);
             }
         });

         btnRegresar.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 regresarAPantallaPrincipal();
             }
         });

         // Crear el panel y agregar los botones
         JPanel panel = new JPanel();
         // Ajustado el GridLayout para 4 filas y 1 columna para el nuevo botón
         panel.setLayout(new GridLayout(4, 1, 10, 10));
         panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

         // Añadir los botones al panel
         panel.add(btnNuevaReceta);
         panel.add(btnRecetas);
         panel.add(btnGestionarCategorias); // Añadir el nuevo botón aquí
         panel.add(btnRegresar);

         // Añadir el panel a la ventana
         add(panel);

         setVisible(true);
     }

     private void nuevaReceta() {
         this.dispose();
         PantallaCostos ventana = new PantallaCostos(sessionFactory);
         ventana.setVisible(true);
     }

     private void mostrarYActualizarRecetas() {
         this.dispose();
         VisualizarRecetas visualizarRecetas = new VisualizarRecetas(this, sessionFactory);
         visualizarRecetas.setVisible(true);
     }

     private void regresarAPantallaPrincipal() {
         this.dispose();
         PantallaPrincipal pantallaPrincipal = new PantallaPrincipal(sessionFactory);
         pantallaPrincipal.setVisible(true);
     }
 }