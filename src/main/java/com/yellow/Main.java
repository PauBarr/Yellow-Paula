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
            // Configura el SessionFactory y agrega la clase Ingrediente explícitamente
            factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(Ingrediente.class)
                .buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Error al inicializar SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }

        // Agregar un shutdown hook para cerrar el factory al finalizar la aplicación
        Runtime.getRuntime().addShutdownHook(new Thread(Main::cerrarFactory));
    }


    public static void main(String[] args) {
        // Mostrar la pantalla principal usando Swing
        SwingUtilities.invokeLater(() -> {
            PantallaPrincipal pantalla = new PantallaPrincipal(factory);  // Pasa el SessionFactory a PantallaPrincipal
            pantalla.setVisible(true);
        });
    }

    // Método para obtener la SessionFactory
    public static SessionFactory getSessionFactory() {
        return factory;
    }

    // Método para guardar ingredientes en la base de datos
    public static void guardarIngrediente(Ingrediente ingrediente) {
        Session session = null;
        try {
            session = factory.openSession();
            session.beginTransaction();
            session.save(ingrediente);  // Guardar el ingrediente
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session != null && session.getTransaction() != null) {
                session.getTransaction().rollback();  // Revertir en caso de error
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();  // Asegurar cierre de la sesión
            }
        }
    }

    // Método para guardar recetas en la base de datos
    public static void guardarReceta(Receta nuevaReceta) {
        Session session = null;
        try {
            session = factory.openSession();
            session.beginTransaction();
            session.save(nuevaReceta);  // Guardar la receta
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session != null && session.getTransaction() != null) {
                session.getTransaction().rollback();  // Revertir en caso de error
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();  // Cerrar la sesión
            }
        }
    }

    // Método para cerrar el SessionFactory al finalizar
    public static void cerrarFactory() {
        if (factory != null) {
            factory.close();
        }
    }
}