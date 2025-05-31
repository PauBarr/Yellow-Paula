package com.yellow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
// Importaciones para el pintado personalizado de botones
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

public class VisualizarNotas extends JFrame {

    // CAMBIO: Usaremos JList en lugar de JTextArea
    private JList<String> notasList;
    private DefaultListModel<String> listModel; // Modelo para JList

    private VentanaNotas ventanaPadreNotas; // Referencia a la VentanaNotas principal

    public VisualizarNotas(VentanaNotas ventanaPadreNotas) {
        this.ventanaPadreNotas = ventanaPadreNotas; // Guardar la referencia

        setTitle("Visualizar Notas");
        setSize(900, 600); // CAMBIO: Tamaño consistente con otras pantallas
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cierra solo esta ventana
        setLocationRelativeTo(null); // Centrar la ventana

        // CAMBIO: Usaremos BorderLayout para la estructura principal
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(255, 255, 220)); // Fondo amarillo claro

        // --- Panel Superior para el Título ---
        JPanel panelSuperior = new JPanel();
        panelSuperior.setBackground(new Color(255, 255, 220));
        panelSuperior.setBorder(new EmptyBorder(20, 0, 10, 0));
        JLabel tituloLabel = new JLabel("MIS NOTAS GUARDADAS", SwingConstants.CENTER);
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 28)); // Fuente más grande y negrita
        tituloLabel.setForeground(new Color(60, 60, 60));
        panelSuperior.add(tituloLabel);
        add(panelSuperior, BorderLayout.NORTH); // Añadir al NORTE del JFrame

        // --- Panel Central para la lista de notas ---
        listModel = new DefaultListModel<>();
        notasList = new JList<>(listModel);
        notasList.setFont(new Font("Segoe UI", Font.PLAIN, 16)); // Fuente para la lista
        notasList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Permite seleccionar una nota
        notasList.setBackground(Color.WHITE); // Fondo blanco para el área de la lista
        JScrollPane scrollPane = new JScrollPane(notasList);
        scrollPane.setBorder(BorderFactory.createCompoundBorder( // Borde con espaciado
                BorderFactory.createEmptyBorder(10, 50, 10, 50),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        ));
        add(scrollPane, BorderLayout.CENTER); // Añadir al CENTRO del JFrame

        // --- Panel Inferior para los botones ---
        JPanel panelBotones = new JPanel();
        panelBotones.setBackground(new Color(255, 255, 220));
        panelBotones.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20)); // Espaciado entre botones

        // Botón para eliminar la nota seleccionada
        JButton btnEliminarNota = createRoundedButton("Eliminar Nota", new Color(178, 34, 34)); // Rojo fuego
        btnEliminarNota.addActionListener(e -> eliminarNota());
        panelBotones.add(btnEliminarNota);

        // Botón para regresar a VentanaNotas
        JButton btnRegresar = createRoundedButton("Regresar", Color.DARK_GRAY); // Gris oscuro
        btnRegresar.addActionListener(e -> {
            this.ventanaPadreNotas.setVisible(true); // Muestra VentanaNotas
            dispose(); // Cierra VisualizarNotas
        });
        panelBotones.add(btnRegresar);

        add(panelBotones, BorderLayout.SOUTH); // Añadir al SUR del JFrame

        // Cargar las notas desde el archivo al iniciar
        cargarNotas();
    }

    // Método auxiliar para crear botones redondeados
    private JButton createRoundedButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 15, 15)); // Redondeo de 15
                super.paintComponent(g);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE); // Borde blanco
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 15, 15));
                g2.dispose();
            }
        };
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Fuente para botones
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(180, 45)); // Tamaño preferido para botones
        return button;
    }

    private void cargarNotas() {
        listModel.clear(); // Limpiar el modelo de la lista antes de cargar
        File file = new File("nota.txt");
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "El archivo nota.txt no existe. No hay notas para mostrar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (!linea.trim().isEmpty()) { // No añadir líneas vacías
                    listModel.addElement(linea); // Añadir cada línea como una nota individual
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al leer el archivo de notas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void eliminarNota() {
        int selectedIndex = notasList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona una nota para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String notaAEliminar = listModel.getElementAt(selectedIndex);
        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea eliminar la nota seleccionada?\n" + notaAEliminar, "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            listModel.remove(selectedIndex); // Eliminar del modelo

            // Reescribir el archivo sin la nota eliminada
            try (FileWriter writer = new FileWriter("nota.txt")) {
                for (int i = 0; i < listModel.getSize(); i++) {
                    writer.write(listModel.getElementAt(i) + System.lineSeparator());
                }
                JOptionPane.showMessageDialog(this, "Nota eliminada con éxito.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar las notas después de eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}