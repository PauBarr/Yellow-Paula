package com.yellow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.Image; // Para escalar iconos
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory; // Para el espaciado y bordes
import org.hibernate.SessionFactory;

public class PantallaPrincipal extends JFrame {

    private JPanel panel;
    private SessionFactory sessionFactory;

    public PantallaPrincipal(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;

        // Configuración del JFrame
        setTitle("Pantalla Principal - Yellow");
        setSize(900, 600); // Tamaño fijo como PantallaCostos
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Usar BorderLayout para el JFrame para colocar el eslogan y la configuración arriba
        setLayout(new BorderLayout());

        // --- Panel Superior para Eslogan y Configuración (NORTH) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(255, 255, 220)); // Fondo amarillo muy claro
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Espaciado

        // Eslogan en el centro del topPanel
        JLabel sloganLabel = new JLabel("Tus costos en un click", SwingConstants.CENTER);
        sloganLabel.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 24));
        sloganLabel.setForeground(new Color(60, 60, 60));
        topPanel.add(sloganLabel, BorderLayout.CENTER);

        // Botón/Icono de Configuración en la esquina superior derecha (EAST del topPanel)
        JButton btnConfiguracion = createIconButton("/icons/settings_icon.png");
        btnConfiguracion.setToolTipText("Configuración");
        btnConfiguracion.addActionListener(e -> {
            // Abrir la nueva ventana de Configuración
            PantallaConfiguracion configScreen = new PantallaConfiguracion(this, sessionFactory);
            configScreen.setVisible(true);
            dispose(); // Cierra la pantalla principal si quieres que solo se vea la configuración
        });
        JPanel configPanel = new JPanel(); // Para alinear a la derecha
        configPanel.setBackground(new Color(255, 255, 220)); // Mismo fondo que topPanel
        configPanel.add(btnConfiguracion);
        topPanel.add(configPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH); // Añadir el topPanel al JFrame

        // --- Panel Central para Logo y Botones de Funcionalidad ---
        panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 255, 220)); // Fondo amarillo muy claro

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(10, 10, 10, 10); // Espaciado entre componentes
        gbc.anchor = GridBagConstraints.CENTER; // Centrar los componentes

        // ---------------------- LOGO YELLOW ----------------------
        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/images/yellow_logo.png"));
            if (logoIcon.getImageLoadStatus() == java.awt.MediaTracker.ERRORED) {
                JLabel textLogo = new JLabel("YELLOW");
                textLogo.setFont(new Font("Arial", Font.BOLD, 48));
                textLogo.setForeground(new Color(255, 200, 0));
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.gridwidth = 3; // Ocupa el ancho de los 3 botones para centrarlo
                panel.add(textLogo, gbc);
            } else {
                JLabel logoLabel = new JLabel(logoIcon);
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.gridwidth = 3; // Ocupa el ancho de los 3 botones para centrarlo
                panel.add(logoLabel, gbc);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar el logo: " + e.getMessage());
            JLabel textLogo = new JLabel("YELLOW");
            textLogo.setFont(new Font("Arial", Font.BOLD, 48));
            textLogo.setForeground(new Color(255, 200, 0));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 3;
            panel.add(textLogo, gbc);
        }
        // ---------------------------------------------------------

        // Resetear gbc para los botones de funcionalidad
        gbc.gridy = 1; // Fila para los botones
        gbc.gridwidth = 1; // Cada botón ocupa una columna
        gbc.fill = GridBagConstraints.NONE; // No expandir los botones

        // Botones de funcionalidad principales
        JButton btnNuevoProyecto = createStyledButton("Nuevo Proyecto", "/icons/new_project_icon.png");
        JButton btnIngresoIngredientes = createStyledButton("Ingredientes", "/icons/ingredients_icon.png");
        JButton btnNotas = createStyledButton("Notas", "/icons/notes_icon.png");

        // Establecer acciones (asegúrate de que estas clases y constructores existan y sean correctos)
        btnNuevoProyecto.addActionListener(e -> {
            // MODIFICADO: Abrir IngresoReceta (que ahora es el JFrame principal para la gestión de recetas)
            // Se le pasa 'this' (la PantallaPrincipal actual) como la ventana anterior
            IngresoReceta ingresoReceta = new IngresoReceta(this, sessionFactory);
            ingresoReceta.setVisible(true);
            this.setVisible(false); // Oculta PantallaPrincipal
        });

        btnIngresoIngredientes.addActionListener(e -> {
            IngresoIngrediente ingresoIngrediente = new IngresoIngrediente(this, sessionFactory);
            ingresoIngrediente.setVisible(true);
            dispose();
        });

        btnNotas.addActionListener(e -> {
            VentanaNotas ventanaNotas = new VentanaNotas();
            ventanaNotas.setVisible(true);
            dispose();
        });


        // Añadir botones al panel
        gbc.gridx = 0; panel.add(btnNuevoProyecto, gbc);
        gbc.gridx = 1; panel.add(btnIngresoIngredientes, gbc);
        gbc.gridx = 2; panel.add(btnNotas, gbc);

        // Agregar el panel central al JFrame
        add(panel, BorderLayout.CENTER);
    }

    // Método auxiliar para crear y estilizar botones con iconos
    private JButton createStyledButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(150, 60)); // Tamaño más grande para los botones
        button.setBackground(Color.DARK_GRAY); // Color de fondo gris oscuro
        button.setForeground(Color.WHITE); // Color de texto blanco
        button.setFont(new Font("Arial", Font.BOLD, 14)); // Fuente negrita y tamaño adecuado

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            if (icon.getImageLoadStatus() == java.awt.MediaTracker.ERRORED) {
                 System.err.println("Error al cargar el icono: " + iconPath);
            } else {
                Image scaledImage = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                button.setIcon(new ImageIcon(scaledImage));
                button.setHorizontalTextPosition(SwingConstants.CENTER);
                button.setVerticalTextPosition(SwingConstants.BOTTOM);
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono para: " + text + ". Ruta: " + iconPath + ". Error: " + e.getMessage());
        }
        return button;
    }

    // Método auxiliar para crear un botón solo con icono (para Configuración)
    private JButton createIconButton(String iconPath) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(40, 40)); // Tamaño pequeño para el icono
        button.setOpaque(false); // Hacer el botón transparente
        button.setContentAreaFilled(false); // Quitar el relleno del área de contenido
        button.setBorderPainted(false); // Quitar el borde

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            if (icon.getImageLoadStatus() == java.awt.MediaTracker.ERRORED) {
                 System.err.println("Error al cargar el icono de configuración: " + iconPath);
                 button.setText("Conf."); // Texto alternativo si el icono falla
            } else {
                Image scaledImage = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                button.setIcon(new ImageIcon(scaledImage));
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono de configuración. Ruta: " + iconPath + ". Error: " + e.getMessage());
            button.setText("Conf."); // Texto alternativo si el icono falla
        }
        return button;
    }

    // El método main para probar (solo para pruebas directas de la pantalla, no para la ejecución principal de la app)
    /*
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Debes pasar un SessionFactory real aquí si quieres que funcione correctamente.
            // Para una prueba visual simple, puedes pasar null, pero las funcionalidades fallarán.
            PantallaPrincipal pantalla = new PantallaPrincipal(null);
            pantalla.setVisible(true);
        });
    }
    */
}