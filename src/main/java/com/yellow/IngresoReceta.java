package com.yellow;

import org.hibernate.SessionFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IngresoReceta extends JFrame { // Sigue siendo JFrame

    private SessionFactory sessionFactory;
    private JPanel cards; // Panel que usará CardLayout
    private CardLayout cardLayout;

    // Paneles que se mostrarán en el CardLayout
    private JPanel panelPrincipalRecetas; // Panel inicial de botones
    private PantallaCostos panelNuevaReceta;
    private VisualizarRecetas panelVerActualizarRecetas;
    private GestionCategoriasScreen panelGestionarCategorias;

    private JFrame pantallaAnteriorPrincipal; // Referencia a la PantallaPrincipal para regresar

    public IngresoReceta(JFrame pantallaAnteriorPrincipal, SessionFactory sessionFactory) {
        this.pantallaAnteriorPrincipal = pantallaAnteriorPrincipal;
        this.sessionFactory = sessionFactory;

        setTitle("Gestión de Recetas - Yellow");
        setSize(1200, 700); // Tamaño más grande para acomodar los paneles internos
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cierra solo esta ventana

        // Configurar el layout principal de IngresoReceta (BorderLayout para barra lateral y contenido)
        setLayout(new BorderLayout());

        // --- Panel de Navegación (Izquierda) ---
        JPanel panelNavegacion = new JPanel();
        panelNavegacion.setLayout(new GridLayout(5, 1, 10, 10)); // Botones apilados
        panelNavegacion.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        panelNavegacion.setBackground(new Color(220, 230, 255)); // Un color suave para la barra lateral

        JButton btnNuevaReceta = new JButton("<html>Nueva<br>Receta</html>");
        JButton btnVerActualizarRecetas = new JButton("<html>Ver / Actualizar<br>Recetas</html>");
        JButton btnGestionarCategorias = new JButton("<html>Gestionar<br>Categorías</html>");
        JButton btnRegresarPrincipal = new JButton("<html>Regresar a<br>Menú Principal</html>");

        // Estilo de botones de navegación
        Font navButtonFont = new Font("Arial", Font.BOLD, 14);
        Dimension navButtonSize = new Dimension(180, 70); // Ancho fijo, alto para dos líneas

        btnNuevaReceta.setFont(navButtonFont);
        btnNuevaReceta.setPreferredSize(navButtonSize);
        btnNuevaReceta.setBackground(new Color(85, 107, 47)); // Verde oliva
        btnNuevaReceta.setForeground(Color.WHITE);

        btnVerActualizarRecetas.setFont(navButtonFont);
        btnVerActualizarRecetas.setPreferredSize(navButtonSize);
        btnVerActualizarRecetas.setBackground(new Color(70, 130, 180)); // Azul acero
        btnVerActualizarRecetas.setForeground(Color.WHITE);

        btnGestionarCategorias.setFont(navButtonFont);
        btnGestionarCategorias.setPreferredSize(navButtonSize);
        btnGestionarCategorias.setBackground(new Color(255, 140, 0)); // Naranja oscuro
        btnGestionarCategorias.setForeground(Color.WHITE);

        btnRegresarPrincipal.setFont(navButtonFont);
        btnRegresarPrincipal.setPreferredSize(navButtonSize);
        btnRegresarPrincipal.setBackground(Color.GRAY); // Gris
        btnRegresarPrincipal.setForeground(Color.WHITE);


        panelNavegacion.add(btnNuevaReceta);
        panelNavegacion.add(btnVerActualizarRecetas);
        panelNavegacion.add(btnGestionarCategorias);
        panelNavegacion.add(new JLabel(" ")); // Espaciador
        panelNavegacion.add(btnRegresarPrincipal);

        add(panelNavegacion, BorderLayout.WEST); // Añadir al oeste para barra lateral

        // --- Panel Central con CardLayout ---
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.setBackground(new Color(255, 255, 220)); // Fondo del área de tarjetas

        // Panel Principal de la Gestión de Recetas (el que tenía los 3 botones originales)
        panelPrincipalRecetas = new JPanel(new GridBagLayout());
        panelPrincipalRecetas.setBackground(new Color(255, 255, 220));
        JLabel labelTituloPrincipal = new JLabel("SELECCIONE UNA OPCIÓN DE RECETAS");
        labelTituloPrincipal.setFont(new Font("Arial", Font.BOLD, 20));
        panelPrincipalRecetas.add(labelTituloPrincipal); // Se puede mejorar este panel

        // Inicializar los paneles de las funcionalidades
        panelNuevaReceta = new PantallaCostos(this, sessionFactory);
        // NOTA: VisualizarRecetas y GestionCategoriasScreen necesitan ser instanciadas con 'this' como padre,
        // pero también con el SessionFactory.
        panelVerActualizarRecetas = new VisualizarRecetas(this, sessionFactory);
        panelGestionarCategorias = new GestionCategoriasScreen(this, sessionFactory);


        // Añadir los paneles al CardLayout
        cards.add(panelPrincipalRecetas, "principal"); // Clave para volver
        cards.add(panelNuevaReceta, "nuevaReceta");
        cards.add(panelVerActualizarRecetas, "verRecetas");
        cards.add(panelGestionarCategorias, "gestionarCategorias");

        add(cards, BorderLayout.CENTER); // Añadir el panel de tarjetas al centro

        // --- Acciones de los botones de navegación ---
        btnNuevaReceta.addActionListener(e -> mostrarPanel("nuevaReceta"));
        btnVerActualizarRecetas.addActionListener(e -> {
            // Asegurarse de recargar los datos cuando se vuelve a este panel
            panelVerActualizarRecetas = new VisualizarRecetas(this, sessionFactory);
            cards.add(panelVerActualizarRecetas, "verRecetas"); // Reemplaza la tarjeta existente
            mostrarPanel("verRecetas");
        });
        btnGestionarCategorias.addActionListener(e -> {
            // Asegurarse de recargar los datos cuando se vuelve a este panel
            panelGestionarCategorias = new GestionCategoriasScreen(this, sessionFactory);
            cards.add(panelGestionarCategorias, "gestionarCategorias"); // Reemplaza la tarjeta existente
            mostrarPanel("gestionarCategorias");
        });
        btnRegresarPrincipal.addActionListener(e -> regresarAPantallaPrincipal());

        // Mostrar el panel principal al inicio
        cardLayout.show(cards, "principal");

        setVisible(true); // Hacer visible la ventana
    }

    /**
     * Muestra el panel especificado en el CardLayout.
     * @param panelName El nombre de la tarjeta a mostrar (ej. "nuevaReceta", "verRecetas").
     */
    public void mostrarPanel(String panelName) {
        cardLayout.show(cards, panelName);
    }

    /**
     * Muestra el panel de recetas con una categoría pre-filtrada.
     * Este método es llamado desde GestionCategoriasScreen.
     * @param panelName El nombre de la tarjeta "verRecetas".
     * @param categoriaFiltro La categoría a pre-seleccionar.
     */
    public void mostrarPanel(String panelName, Categoria categoriaFiltro) {
        // Para asegurar que el filtro se aplique correctamente al recargar la tabla
        panelVerActualizarRecetas = new VisualizarRecetas(this, sessionFactory, categoriaFiltro);
        cards.add(panelVerActualizarRecetas, "verRecetas"); // Reemplaza la tarjeta existente
        cardLayout.show(cards, panelName);
    }

    private void regresarAPantallaPrincipal() {
        if (pantallaAnteriorPrincipal != null) {
            pantallaAnteriorPrincipal.setVisible(true);
        }
        dispose(); // Cierra esta ventana de gestión de recetas
    }
}