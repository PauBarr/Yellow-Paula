package com.yellow;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.io.FileWriter;
import java.io.IOException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

public class VentanaNotas extends JFrame {

    private JPanel contentPane;
    private JEditorPane editorPane;

    public VentanaNotas() {
        // Establece la operación por defecto al cerrar la ventana (cerrar solo esta ventana)
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // CORRECCIÓN: Aumentar el ancho de la ventana para que quepan todos los botones
        // Se mantiene el alto, pero el ancho es mayor para mejor distribución
        setSize(1200, 650); // Aumentado el ancho y un poco el alto para mayor comodidad
        // Centra la ventana en la pantalla
        setLocationRelativeTo(null);

        contentPane = new JPanel();
        // Colores de fondo y primer plano para el panel principal
        contentPane.setBackground(new Color(255, 255, 220)); // Un color crema claro
        contentPane.setForeground(new Color(0, 0, 0)); // Color de texto negro
        // Borde vacío para espacio alrededor del contenido
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        // Usa un BorderLayout para organizar los componentes principales (título, editor, opciones)
        contentPane.setLayout(new BorderLayout());

        // Etiqueta del título en la parte superior
        JLabel titleLabel = new JLabel("TUS NOTAS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28)); // Fuente y tamaño del título
        titleLabel.setForeground(new Color(60, 60, 60)); // Color de texto oscuro para el título
        contentPane.add(titleLabel, BorderLayout.NORTH); // Añade el título en la parte superior

        // Área para escribir las notas
        editorPane = new JEditorPane();
        editorPane.setFont(new Font("Segoe UI", Font.PLAIN, 16)); // Fuente para el editor de texto
        JScrollPane scrollPane = new JScrollPane(editorPane); // Agrega un scroll si el texto es muy largo
        contentPane.add(scrollPane, BorderLayout.CENTER); // Añade el editor en el centro

        // CORRECCIÓN: Panel principal para los botones de opciones en la parte inferior
        // Ahora usa un BorderLayout para organizar sub-paneles de botones
        JPanel optionsPanel = new JPanel();
        optionsPanel.setBackground(new Color(255, 255, 220)); // Mismo color de fondo que el panel principal
        optionsPanel.setLayout(new BorderLayout()); // Usamos BorderLayout para organizar las filas de botones
        contentPane.add(optionsPanel, BorderLayout.SOUTH); // Añade el panel de opciones en la parte inferior

        // Sub-panel para los botones de edición/visualización (fila superior de botones)
        JPanel topButtonsPanel = new JPanel();
        topButtonsPanel.setBackground(new Color(255, 255, 220));
        // FlowLayout para que los botones se centren y envuelvan si es necesario
        topButtonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Espacio entre botones

        // Botón para guardar la nota
        JButton btnGuardar = createRoundedButton("Guardar Nota", new Color(255, 140, 0)); // Botón verde oscuro
        btnGuardar.addActionListener(e -> guardarNota()); // Llama al método guardarNota al hacer clic
        topButtonsPanel.add(btnGuardar);

        // Botón para cambiar el color del texto
        JButton btnCambiarColorTexto = createRoundedButton("Cambiar Color del Texto", new Color(70, 130, 180)); // Botón azul
        btnCambiarColorTexto.addActionListener(e -> cambiarColorTexto()); // Llama al método cambiarColorTexto
        topButtonsPanel.add(btnCambiarColorTexto);

        // Botón para cambiar la fuente del texto
        JButton btnCambiarFuente = createRoundedButton("Cambiar Fuente", new Color(255, 140, 0)); // Botón naranja
        btnCambiarFuente.addActionListener(e -> cambiarFuente()); // Llama al método cambiarFuente
        topButtonsPanel.add(btnCambiarFuente);

        // Botón para visualizar notas (abre una nueva ventana VisualizarNotas)
        JButton btnVisualizarNotas = createRoundedButton("Visualizar Notas", new Color(70, 130, 180)); // Botón azul
        btnVisualizarNotas.addActionListener(e -> {
            this.setVisible(false); // Oculta la ventana actual
            // Se asume que VisualizarNotas requiere una referencia a esta ventana para poder volver
            new VisualizarNotas(this).setVisible(true); // Abre la ventana de VisualizarNotas
        });
        topButtonsPanel.add(btnVisualizarNotas);

        // Botón para cambiar el color de fondo del editor de texto
        JButton btnCambiarColorFondo = createRoundedButton("Cambiar Color de Fondo", new Color(70, 130, 180)); // Botón azul
        btnCambiarColorFondo.addActionListener(e -> cambiarColorFondo()); // Llama al método cambiarColorFondo
        topButtonsPanel.add(btnCambiarColorFondo);

        // Botón para limpiar el contenido del editor de texto
        JButton btnLimpiarContenido = createRoundedButton("Limpiar Contenido", Color.DARK_GRAY); // Botón gris oscuro
        btnLimpiarContenido.addActionListener(e -> limpiarContenido()); // Llama al método limpiarContenido
        topButtonsPanel.add(btnLimpiarContenido);

        // Añade el panel de botones de la fila superior al centro del optionsPanel
        optionsPanel.add(topButtonsPanel, BorderLayout.CENTER);

        // Sub-panel para el botón "Regresar" (fila inferior de botones)
        JPanel bottomButtonsPanel = new JPanel();
        bottomButtonsPanel.setBackground(new Color(255, 255, 220));
        // FlowLayout para que el botón de regresar se centre en su propia fila
        bottomButtonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Botón para regresar a la pantalla principal
        JButton btnRegresar = createRoundedButton("Regresar", new Color(178, 34, 34)); // Botón rojo oscuro
        btnRegresar.addActionListener(e -> {
            // Asegura que las operaciones de UI se ejecuten en el hilo de despacho de eventos (EDT)
            SwingUtilities.invokeLater(() -> {
                // Crea una nueva instancia de PantallaPrincipal y la hace visible
                // Se asume que PantallaPrincipal necesita Main.getSessionFactory() como argumento
                new PantallaPrincipal(Main.getSessionFactory()).setVisible(true);
                // Cierra la ventana actual de VentanaNotas
                dispose();
            });
        });
        bottomButtonsPanel.add(btnRegresar);

        // Añade el panel de botones de la fila inferior (con el botón Regresar) al sur del optionsPanel
        optionsPanel.add(bottomButtonsPanel, BorderLayout.SOUTH);
    }

    /**
     * Método auxiliar para crear botones con esquinas redondeadas.
     * Mejora la estética de los botones.
     * @param text El texto que se mostrará en el botón.
     * @param bgColor El color de fondo del botón.
     * @return Un JButton con un diseño personalizado.
     */
    private JButton createRoundedButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // Habilita el suavizado de bordes para una apariencia más limpia
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground()); // Establece el color de fondo del botón
                // Dibuja un rectángulo redondeado relleno como fondo del botón
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 15, 15));
                super.paintComponent(g); // Llama al método original para pintar el texto e ícono
                g2.dispose(); // Libera los recursos del contexto gráfico
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE); // Color del borde (blanco)
                // Dibuja un rectángulo redondeado como borde del botón
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 15, 15));
                g2.dispose(); // Libera los recursos del contexto gráfico
            }
        };
        button.setBackground(bgColor); // Establece el color de fondo del botón (usado en paintComponent)
        button.setForeground(Color.WHITE); // Color del texto del botón (blanco)
        button.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Fuente del texto del botón
        button.setOpaque(false); // Hace que el botón sea transparente para ver el fondo redondeado personalizado
        button.setContentAreaFilled(false); // No rellena el área de contenido por defecto (para ver el redondeo)
        button.setBorderPainted(false); // No pinta el borde por defecto de Swing
        button.setPreferredSize(new Dimension(200, 45)); // Establece un tamaño preferido para el botón
        return button;
    }

    /**
     * Guarda el texto actual del JEditorPane en un archivo llamado "nota.txt".
     * Añade el texto al final del archivo.
     */
    private void guardarNota() {
        String texto = editorPane.getText(); // Obtiene el texto del editor
        try (FileWriter writer = new FileWriter("nota.txt", true)) { // Abre el archivo en modo de añadir (true)
            writer.write(texto + System.lineSeparator()); // Escribe el texto y un salto de línea
            JOptionPane.showMessageDialog(this, "Nota guardada con éxito.", "Guardar Nota", JOptionPane.INFORMATION_MESSAGE); // Muestra un mensaje de éxito
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar la nota.", "Error", JOptionPane.ERROR_MESSAGE); // Muestra un mensaje de error
            ex.printStackTrace(); // Imprime la pila de errores para depuración en la consola
        }
    }

    /**
     * Permite al usuario cambiar el color del texto en el JEditorPane
     * utilizando un JColorChooser.
     */
    private void cambiarColorTexto() {
        // Abre un selector de color con el color actual del texto como valor inicial
        Color nuevoColorTexto = JColorChooser.showDialog(this, "Elige un color para el texto",
                editorPane.getForeground());
        if (nuevoColorTexto != null) { // Si se seleccionó un color (el diálogo no fue cancelado)
            editorPane.setForeground(nuevoColorTexto); // Establece el nuevo color de texto
        }
    }

    /**
     * Permite al usuario cambiar la fuente del texto en el JEditorPane
     * ofreciendo una selección de fuentes comunes.
     */
    private void cambiarFuente() {
        // Opciones de fuentes predefinidas para el diálogo
        String[] opciones = { "Serif", "SansSerif", "Monospaced", "Segoe UI", "Arial", "Verdana", "Times New Roman" };
        // Abre un diálogo de entrada para que el usuario elija una fuente
        String seleccion = (String) JOptionPane.showInputDialog(this, "Elige una fuente", "Cambiar Fuente",
                JOptionPane.PLAIN_MESSAGE, null, opciones, "Segoe UI"); // "Segoe UI" como valor inicial por defecto
        if (seleccion != null) { // Si se seleccionó una fuente
            // Establece la nueva fuente manteniendo el estilo (PLAIN) y el tamaño actual
            editorPane.setFont(new Font(seleccion, Font.PLAIN, editorPane.getFont().getSize()));
        }
    }

    /**
     * Permite al usuario cambiar el color de fondo del JEditorPane
     * utilizando un JColorChooser.
     */
    private void cambiarColorFondo() {
        // Abre un selector de color con el color de fondo actual como valor inicial
        Color nuevoColorFondo = JColorChooser.showDialog(this, "Elige un color para el fondo",
                editorPane.getBackground());
        if (nuevoColorFondo != null) { // Si se seleccionó un color
            editorPane.setBackground(nuevoColorFondo); // Establece el nuevo color de fondo
        }
    }

    /**
     * Limpia todo el contenido del JEditorPane, dejando el área de texto en blanco.
     */
    private void limpiarContenido() {
        editorPane.setText(""); // Establece el texto del editor a una cadena vacía
    }
}