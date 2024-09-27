package com.yellow;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import javax.swing.SwingUtilities;

public class Main {

    private static SessionFactory factory;

    // Bloque estático para inicializar la SessionFactory una vez al cargar la clase
    static {
        try {
            factory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Error al inicializar SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static void main(String[] args) {
        // Mostrar la pantalla principal usando Swing
        SwingUtilities.invokeLater(() -> {
            PantallaPrincipal pantalla = new PantallaPrincipal();
            pantalla.setVisible(true);
        });
    }

    // Método para obtener la SessionFactory
    public static SessionFactory getSessionFactory() {
        return factory;
    }

    // Método para guardar ingredientes en la base de datos
    public static void guardarIngrediente(Ingrediente ingrediente) {
        Session session = factory.openSession();
        try {
            session.beginTransaction();
            session.save(ingrediente);  // Guardar el ingrediente
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();  // Revertir en caso de error
            }
            e.printStackTrace();
        } finally {
            session.close();  // Cerrar la sesión
        }
    }

    // Método para cerrar el SessionFactory al finalizar
    public static void cerrarFactory() {
        if (factory != null) {
            factory.close();
        }
    }

    // Método para guardar recetas en la base de datos (puedes implementar la lógica)
    public static void guardarReceta(Receta nuevaReceta) {
        // Implementa la lógica para guardar una receta
    }
}
