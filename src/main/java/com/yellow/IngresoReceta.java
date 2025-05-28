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
         setSize(400, 300);
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cambiado a DISPOSE_ON_CLOSE para que solo cierre esta ventana
         setLocationRelativeTo(null);

         // Crear botones
         JButton btnNuevaReceta = new JButton("Nueva receta");
         JButton btnRecetas = new JButton("Ver / Actualizar Recetas");
         JButton btnRegresar = new JButton("Regresar");

         // Asignar acciones a los botones
         btnNuevaReceta.addActionListener(new ActionListener() { //
             public void actionPerformed(ActionEvent e) { //
                 nuevaReceta(); //
             }
         });

         btnRecetas.addActionListener(new ActionListener() { //
             public void actionPerformed(ActionEvent e) { //
                 mostrarYActualizarRecetas(); //
             }
         });

         btnRegresar.addActionListener(new ActionListener() { //
             public void actionPerformed(ActionEvent e) { //
                 regresarAPantallaPrincipal(); //
             }
         });

         // Crear el panel y agregar los botones
         JPanel panel = new JPanel();
         panel.setLayout(new GridLayout(3, 1, 10, 10)); //
         panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); //

         // Añadir los botones al panel
         panel.add(btnNuevaReceta);
         panel.add(btnRecetas);
         panel.add(btnRegresar);

         // Añadir el panel a la ventana
         add(panel);

         setVisible(true);
     }

     private void nuevaReceta() {
         this.dispose(); //
         PantallaCostos ventana = new PantallaCostos(sessionFactory); //
         ventana.setVisible(true); //
     }

     // Este método es el que se modifica para abrir VisualizarRecetas
     private void mostrarYActualizarRecetas() {
         this.dispose(); // Oculta la ventana actual (IngresoReceta)
         VisualizarRecetas visualizarRecetas = new VisualizarRecetas(this, sessionFactory); // Crea una instancia de VisualizarRecetas
         visualizarRecetas.setVisible(true); // Hace visible la ventana de VisualizarRecetas
     }

     private void regresarAPantallaPrincipal() {
         this.dispose(); //
         PantallaPrincipal pantallaPrincipal = new PantallaPrincipal(sessionFactory); //
         pantallaPrincipal.setVisible(true); //
     }
 }