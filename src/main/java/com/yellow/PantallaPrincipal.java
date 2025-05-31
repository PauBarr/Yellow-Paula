package com.yellow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.Image;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import org.hibernate.SessionFactory;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D; // Importación correcta para RoundRectangle2D

public class PantallaPrincipal extends JFrame {

    private JPanel panel;
    private SessionFactory sessionFactory;

    public PantallaPrincipal(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;

        // Configuración del JFrame
        setTitle("Pantalla Principal - Yellow");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        // --- Panel Superior para Eslogan y Configuración (NORTH) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(255, 255, 220));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Eslogan en el centro del topPanel
        JLabel sloganLabel = new JLabel("Tus costos en un click", SwingConstants.CENTER);
        // Intentamos establecer la fuente, si no se encuentra, Swing usará una por defecto.
        sloganLabel.setFont(new Font("Comic Sans MS", Font.BOLD | Font.ITALIC, 24));
        sloganLabel.setForeground(new Color(60, 60, 60));
        topPanel.add(sloganLabel, BorderLayout.CENTER);

        // Botón/Icono de Configuración en la esquina superior derecha (EAST del topPanel)
        // Usamos el nuevo método para el botón de icono redondeado
        JButton btnConfiguracion = createRoundedIconButton("/icons/settings_icon.png");
        btnConfiguracion.setToolTipText("Configuración");
        btnConfiguracion.addActionListener(e -> {
            PantallaConfiguracion configScreen = new PantallaConfiguracion(this, sessionFactory);
            configScreen.setVisible(true);
            dispose();
        });
        JPanel configPanel = new JPanel();
        configPanel.setBackground(new Color(255, 255, 220));
        configPanel.add(btnConfiguracion);
        topPanel.add(configPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // --- Panel Central para Logo y Botones de Funcionalidad ---
        panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 255, 220));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // ---------------------- LOGO YELLOW ----------------------
        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/images/yellow_logo.png"));
            if (logoIcon.getImageLoadStatus() == java.awt.MediaTracker.ERRORED) {
                JLabel textLogo = new JLabel("YELLOW");
                textLogo.setFont(new Font("Ink Free", Font.BOLD, 48));
                textLogo.setForeground(new Color(255, 200, 0));
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.gridwidth = 3;
                panel.add(textLogo, gbc);
            } else {
                JLabel logoLabel = new JLabel(logoIcon);
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.gridwidth = 3;
                panel.add(logoLabel, gbc);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar el logo: " + e.getMessage());
            JLabel textLogo = new JLabel("YELLOW");
            textLogo.setFont(new Font("Ink Free", Font.BOLD, 48));
            textLogo.setForeground(new Color(255, 200, 0));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 3;
            panel.add(textLogo, gbc);
        }
        // ---------------------------------------------------------

        // Resetear gbc para los botones de funcionalidad
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        // Botones de funcionalidad principales, usando el método para botones redondeados
        JButton btnNuevoProyecto = createRoundedStyledButton("Nuevo Proyecto", "/icons/new_project_icon.png");
        JButton btnIngresoIngredientes = createRoundedStyledButton("Ingredientes", "/icons/ingredients_icon.png");
        JButton btnNotas = createRoundedStyledButton("Notas", "/icons/notes_icon.png");

        // Establecer acciones
        btnNuevoProyecto.addActionListener(e -> {
            IngresoReceta ingresoReceta = new IngresoReceta(this, sessionFactory);
            ingresoReceta.setVisible(true);
            this.setVisible(false);
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

        add(panel, BorderLayout.CENTER);
    }

    // Método auxiliar para crear y estilizar botones redondeados con iconos
    private JButton createRoundedStyledButton(String text, String iconPath) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dibujar el fondo redondeado
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));

                // Es importante llamar a super.paintComponent *después* de dibujar el fondo
                // para que el texto y el icono del botón se pinten encima de nuestro fondo redondeado.
                super.paintComponent(g);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dibujar el borde redondeado
                g2.setColor(getForeground().darker()); // Color del borde, un poco más oscuro que el texto
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));

                g2.dispose();
                // No llamar a super.paintBorder(g) para evitar el borde predeterminado de Swing
            }
        };

        button.setPreferredSize(new Dimension(180, 80));
        button.setBackground(new Color(85, 107, 47)); // Verde oliva
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Fuente para los botones
        button.setOpaque(false); // Indispensable para que paintComponent pueda dibujar un fondo personalizado
        button.setContentAreaFilled(false); // No rellenar el área de contenido por defecto

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            if (icon.getImageLoadStatus() == java.awt.MediaTracker.ERRORED) {
                 System.err.println("Error al cargar el icono: " + iconPath);
            } else {
                Image scaledImage = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                button.setIcon(new ImageIcon(scaledImage));
                button.setHorizontalTextPosition(SwingConstants.CENTER);
                button.setVerticalTextPosition(SwingConstants.BOTTOM);
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono para: " + text + ". Ruta: " + iconPath + ". Error: " + e.getMessage());
        }
        return button;
    }

    // Método auxiliar para crear un botón de icono redondeado (para Configuración)
    private JButton createRoundedIconButton(String iconPath) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dibujar el fondo redondeado
                g2.setColor(new Color(200, 200, 200, 150)); // Un gris semi-transparente para el fondo del icono
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 15, 15));

                super.paintComponent(g);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dibujar el borde redondeado
                g2.setColor(Color.GRAY); // Borde gris
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 15, 15));

                g2.dispose();
                // No llamar a super.paintBorder(g)
            }
        };
        button.setPreferredSize(new Dimension(50, 50));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false); // No queremos el borde por defecto de Swing

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            if (icon.getImageLoadStatus() == java.awt.MediaTracker.ERRORED) {
                 System.err.println("Error al cargar el icono de configuración: " + iconPath);
                 button.setText("Conf.");
                 button.setFont(new Font("Arial", Font.PLAIN, 10));
            } else {
                Image scaledImage = icon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH);
                button.setIcon(new ImageIcon(scaledImage));
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono de configuración. Ruta: " + iconPath + ". Error: " + e.getMessage());
            button.setText("Conf.");
            button.setFont(new Font("Arial", Font.PLAIN, 10));
        }
        return button;
    }
}