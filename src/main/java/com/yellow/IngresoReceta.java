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
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setLocationRelativeTo(null);

         // Crear botones
         JButton btnNuevaReceta = new JButton("Nueva receta");
         // ¡Hemos quitado el botón btnActualizarReceta!
         JButton btnRecetas = new JButton("Ver / Actualizar Recetas"); // Cambiado el texto para que quede claro que desde aquí se actualiza
         JButton btnRegresar = new JButton("Regresar");

         // Asignar acciones a los botones
         btnNuevaReceta.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 nuevaReceta();
             }
         });

         btnRecetas.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 mostrarYActualizarRecetas(); // Nueva acción para este botón
             }
         });

         btnRegresar.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 regresarAPantallaPrincipal();
             }
         });

         // Crear el panel y agregar los botones
         JPanel panel = new JPanel();
         panel.setLayout(new GridLayout(3, 1, 10, 10)); // Ahora son 3 filas porque quitamos un botón
         panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

         // Añadir los botones al panel
         panel.add(btnNuevaReceta);
         panel.add(btnRecetas); // El botón que ahora gestiona la visualización y actualización
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

     // Este método reemplaza a "actualizarReceta" y "mostrarRecetas"
     private void mostrarYActualizarRecetas() {
         JOptionPane.showMessageDialog(this, "Abriendo pantalla para ver y actualizar recetas...");
         // Aquí deberías abrir una nueva ventana con un JTable similar a VisualizarIngredientes,
         // donde se listen las recetas y haya opciones para editar o eliminar cada una.
         // Por ejemplo:
         // this.dispose(); // Oculta la ventana actual
         // VisualizarRecetas ventanaRecetas = new VisualizarRecetas(this, sessionFactory);
         // ventanaRecetas.setVisible(true);
     }

     private void regresarAPantallaPrincipal() {
         this.dispose();
         PantallaPrincipal pantallaPrincipal = new PantallaPrincipal(sessionFactory);
         pantallaPrincipal.setVisible(true);
     }
 }