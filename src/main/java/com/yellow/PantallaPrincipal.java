package com.yellow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.Image;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import org.hibernate.SessionFactory;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D; 
import javax.swing.AbstractButton;
import com.yellow.AgendaScreen;

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

        // --- Panel Superior para Eslogan y Botón "Y" (NORTH) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(255, 255, 220));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Eslogan en el centro del topPanel
        JLabel sloganLabel = new JLabel("Tus costos en un click", SwingConstants.CENTER);
        sloganLabel.setFont(new Font("Comic Sans MS", Font.BOLD | Font.ITALIC, 24));
        sloganLabel.setForeground(new Color(60, 60, 60));
        topPanel.add(sloganLabel, BorderLayout.CENTER);

        // Botón "Y" en la esquina superior derecha (EAST del topPanel)
        JButton btnAcercaDe = new JButton("Y"); // Texto simple 'Y'
        btnAcercaDe.setPreferredSize(new Dimension(50, 50));
        btnAcercaDe.setFont(new Font("Arial", Font.BOLD, 24)); // Fuente grande para la 'Y'
        btnAcercaDe.setBackground(new Color(255, 200, 0)); // Color amarillo
        btnAcercaDe.setForeground(Color.WHITE); // Texto blanco
        btnAcercaDe.setFocusPainted(false);
        btnAcercaDe.setBorderPainted(false);
        btnAcercaDe.setOpaque(true); // Necesario para que el color de fondo se vea
        btnAcercaDe.setContentAreaFilled(true); // Asegurarse de que el área de contenido se rellene

        // Estilo redondeado para el botón "Y"
        btnAcercaDe.setBorder(BorderFactory.createEmptyBorder()); // Eliminar borde predeterminado
        btnAcercaDe.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                AbstractButton button = (AbstractButton) c;
                button.setRolloverEnabled(true);
                button.setFocusPainted(false);
                button.setBorderPainted(false);
            }

            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                JButton button = (JButton) c;
                Color background = button.getBackground();
                if (button.getModel().isRollover()) {
                    background = background.darker(); // Oscurecer al pasar el ratón
                }
                g2.setColor(background);
                g2.fill(new RoundRectangle2D.Double(0, 0, button.getWidth() - 1, button.getHeight() - 1, 15, 15)); // Redondeo

                // Dibujar texto e icono
                super.paint(g2, c);
                g2.dispose();
            }
        });


        btnAcercaDe.addActionListener(e -> {
            // MODIFICADO: Ahora abre PantallaConfiguracion sin especificar pestaña, ya que es solo "Acerca de"
            PantallaConfiguracion aboutScreen = new PantallaConfiguracion(this, sessionFactory);
            aboutScreen.setVisible(true);
            dispose();
        });
        JPanel aboutButtonPanel = new JPanel();
        aboutButtonPanel.setBackground(new Color(255, 255, 220));
        aboutButtonPanel.add(btnAcercaDe);
        topPanel.add(aboutButtonPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // --- Panel Central para Logo y Botones de Funcionalidad ---
        panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 255, 220));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // ---------------------- LOGO YELLOW ----------------------
        try {
            // Intenta cargar el logo
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/images/yellow_logo.png")); 
            if (logoIcon.getImageLoadStatus() == java.awt.MediaTracker.ERRORED) {
                // Si el logo no se carga, muestra el texto "YELLOW"
                JLabel textLogo = new JLabel("YELLOW");
                textLogo.setFont(new Font("Ink Free", Font.BOLD, 48));
                textLogo.setForeground(new Color(255, 200, 0));
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.gridwidth = GridBagConstraints.REMAINDER; // Ocupa todo el ancho restante
                panel.add(textLogo, gbc);
            } else {
                // Si el logo se carga, muestra la imagen
                JLabel logoLabel = new JLabel(logoIcon);
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.gridwidth = GridBagConstraints.REMAINDER; // Ocupa todo el ancho restante
                panel.add(logoLabel, gbc);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar el logo: " + e.getMessage());
            // En caso de cualquier otra excepción, muestra el texto "YELLOW"
            JLabel textLogo = new JLabel("YELLOW");
            textLogo.setFont(new Font("Ink Free", Font.BOLD, 48));
            textLogo.setForeground(new Color(255, 200, 0));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = GridBagConstraints.REMAINDER; // Ocupa todo el ancho restante
            panel.add(textLogo, gbc);
        }
        // ---------------------------------------------------------

        // Resetear gbc para los botones de funcionalidad
        gbc.gridy = 1; // Fila para los primeros botones
        gbc.gridwidth = 1; // Un solo espacio de grilla
        gbc.fill = GridBagConstraints.NONE;

        // Botones de funcionalidad principales, usando el método para botones redondeados
        JButton btnNuevoProyecto = createRoundedStyledButton("Nuevo Proyecto", "/icons/new_project_icon.png");
        JButton btnIngresoIngredientes = createRoundedStyledButton("Ingredientes", "/icons/ingredients_icon.png");
        JButton btnNotas = createRoundedStyledButton("Notas", "/icons/notes_icon.png");
        
        // Novedad: Nuevos botones "Papeleria" y "Agenda"
        JButton btnPapeleria = createRoundedStyledButton("Papeleria", "/icons/papeleria_icon.png"); // TODO: Reemplazar con el icono real
        JButton btnAgenda = createRoundedStyledButton("Agenda", "/icons/agenda_icon.png"); // TODO: Reemplazar con el icono real


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
        
        btnPapeleria.addActionListener(e -> {
            // AHORA ESTE BOTÓN ABRIRÁ LA VENTANA DE GESTIÓN DE PAPELERÍA
            GestionPapeleriaScreen gestionPapeleria = new GestionPapeleriaScreen(this, sessionFactory);
            gestionPapeleria.setVisible(true);
            this.setVisible(false); // Oculta la pantalla principal al abrir la de papelería
        });

        btnAgenda.addActionListener(e -> {
            AgendaScreen agendaScreen = new AgendaScreen(this, sessionFactory);
            agendaScreen.setVisible(true);
            this.setVisible(false); // Oculta la pantalla principal
        });


        // Añadir botones al panel
        gbc.gridx = 0; panel.add(btnNuevoProyecto, gbc);
        gbc.gridx = 1; panel.add(btnIngresoIngredientes, gbc);
        gbc.gridx = 2; panel.add(btnNotas, gbc);
        
        // Novedad: Añadir los nuevos botones en una nueva fila o seguir en la misma
        gbc.gridy = 2; // Mueve a la siguiente fila
        gbc.gridx = 0; panel.add(btnPapeleria, gbc);
        gbc.gridx = 1; panel.add(btnAgenda, gbc);


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
}