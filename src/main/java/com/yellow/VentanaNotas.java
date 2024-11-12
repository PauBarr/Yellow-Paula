package com.yellow;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.io.FileWriter;
import java.io.IOException;

public class VentanaNotas extends JFrame {

    private JPanel contentPane;
    private JEditorPane editorPane;

    public VentanaNotas() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);  // Maximiza la ventana
        contentPane = new JPanel();
        contentPane.setBackground(new Color(255, 255, 128)); // Fondo inicial
        contentPane.setForeground(new Color(0, 0, 0));
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout());
        

        // Agregar el editor de texto
        editorPane = new JEditorPane();
        
        JScrollPane scrollPane = new JScrollPane(editorPane);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        // Panel de opciones (guardar, cambiar color, cambiar fuente)
        JPanel optionsPanel = new JPanel();
        contentPane.add(optionsPanel, BorderLayout.SOUTH);

        // Botón para guardar la nota
        JButton btnGuardar = new JButton("Guardar Nota");
        btnGuardar.addActionListener(e -> guardarNota());
        optionsPanel.add(btnGuardar);

        // Botón para cambiar el color del texto
        JButton btnCambiarColorTexto = new JButton("Cambiar Color del Texto");
        btnCambiarColorTexto.addActionListener(e -> cambiarColorTexto());
        optionsPanel.add(btnCambiarColorTexto);

        // Botón para cambiar la fuente
        JButton btnCambiarFuente = new JButton("Cambiar Fuente");
        btnCambiarFuente.addActionListener(e -> cambiarFuente());
        optionsPanel.add(btnCambiarFuente);

     // Botón para visualizar las notas
        JButton btnVisualizarNotas = new JButton("Visualizar Notas");
        btnVisualizarNotas.addActionListener(e -> {
            this.setVisible(false);  // Oculta la ventana de notas
            new VisualizarNotas(this).setVisible(true);  // Abre la ventana de VisualizarNotas
        });
        optionsPanel.add(btnVisualizarNotas);


       
        // Nuevo botón: Cambiar color de fondo
        JButton btnCambiarColorFondo = new JButton("Cambiar Color de Fondo");
        btnCambiarColorFondo.addActionListener(e -> cambiarColorFondo());
        optionsPanel.add(btnCambiarColorFondo);

        // Nuevo botón: Limpiar contenido
        JButton btnLimpiarContenido = new JButton("Limpiar Contenido");
        btnLimpiarContenido.addActionListener(e -> limpiarContenido());
        optionsPanel.add(btnLimpiarContenido);
        
        // Botón para regresar a la pantalla principal
        JButton btnRegresar = new JButton("Regresar a la Pantalla Principal");
        btnRegresar.addActionListener(e -> {
            new PantallaPrincipal(null).setVisible(true); // Regresa a la pantalla principal
            dispose(); // Cierra esta ventana actual
        });
        optionsPanel.add(btnRegresar);

    }

    // Método para guardar la nota en un archivo
    private void guardarNota() {
        String texto = editorPane.getText();
        try (FileWriter writer = new FileWriter("nota.txt", true)) {
            writer.write(texto + System.lineSeparator());
            JOptionPane.showMessageDialog(this, "Nota guardada con éxito.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar la nota.");
            ex.printStackTrace();
        }
    }

    // Método para cambiar el color del texto
    private void cambiarColorTexto() {
        Color nuevoColorTexto = JColorChooser.showDialog(this, "Elige un color para el texto",
                editorPane.getForeground());
        if (nuevoColorTexto != null) {
            editorPane.setForeground(nuevoColorTexto); // Cambiar el color del texto en el editor
        }
    }

    // Método para cambiar la fuente
    private void cambiarFuente() {
        String[] opciones = { "Serif", "SansSerif", "Monospaced" };
        String seleccion = (String) JOptionPane.showInputDialog(this, "Elige una fuente", "Cambiar Fuente",
                JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);
        if (seleccion != null) {
            editorPane.setFont(new Font(seleccion, Font.PLAIN, 14));
        }
    }

    // Nuevo método para cambiar el color de fondo
    private void cambiarColorFondo() {
        Color nuevoColorFondo = JColorChooser.showDialog(this, "Elige un color para el fondo",
                editorPane.getBackground());
        if (nuevoColorFondo != null) {
            editorPane.setBackground(nuevoColorFondo); // Cambiar el color de fondo de la ventana
        }
    }

    // Nuevo método para limpiar el contenido del editor
    private void limpiarContenido() {
        editorPane.setText(""); // Limpiar el editor de texto
    }

}
