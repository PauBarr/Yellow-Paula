package com.yellow;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Date;
import java.util.ArrayList;
import java.awt.Desktop;

// Importaciones para PDFBox (ya presentes)
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import java.io.File;
import java.io.IOException;


public class PantallaCostos extends JFrame {

    private static final long serialVersionUID = 1L;
    private SessionFactory sessionFactory;
    private JTable tablaIngredientes;
    private JComboBox<Ingrediente> comboBoxIngredientes;
    private JComboBox<String> comboBoxUnidades;
    private JLabel labelCostoTotal, labelGastosExtra, labelPrecioFinal;
    private JTextField txtCostoTotal, txtGastosExtra, txtPrecioFinal;
    private JButton btnImprimirPlanilla;

    private Map<String, Ingrediente> ingredientesMap;

    // --- NUEVOS COMPONENTES PARA CATEGORÍAS ---
    private JList<Categoria> categoriaList; // Para seleccionar categorías
    private DefaultListModel<Categoria> categoriaListModel; // Modelo para la JList
    // --- FIN NUEVOS COMPONENTES ---

    public PantallaCostos(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        ingredientesMap = new HashMap<>();
        initComponents();
        cargarIngredientes();
        cargarCategorias(); // Cargar las categorías al iniciar la pantalla
    }

    private void initComponents() {
        setTitle("Pantalla de Costos - Yellow"); // Título actualizado
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(new Color(255, 255, 220)); // Fondo amarillo claro

        JPanel panelSuperior = new JPanel();
        panelSuperior.setBackground(new Color(255, 255, 220)); // Mismo fondo
        JLabel labelTitulo = new JLabel("PEPAS"); // Considera cambiar a "Nueva Receta" o algo más descriptivo
        labelTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        labelTitulo.setForeground(new Color(85, 107, 47));
        panelSuperior.add(labelTitulo);

        // Columnas de la tabla (sin cambios aquí)
        String[] columnNames = {"PRODUCTO", "TIPO COMPRA", "CANTIDAD COMPRA", "COSTO COMPRA", "CANT. UTILIZADA", "UNIDAD UTILIZADA", "COSTO REAL"};

        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 4 || column == 5;
            }
        };

        tablaIngredientes = new JTable(model);

        comboBoxIngredientes = new JComboBox<>();
        tablaIngredientes.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(comboBoxIngredientes));

        comboBoxUnidades = new JComboBox<>(new String[]{"gramos", "mililitros", "unidades", "litros", "kilogramos", "cucharadas", "pizcas", "otros"});
        tablaIngredientes.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(comboBoxUnidades));

        comboBoxIngredientes.addActionListener(e -> {
            int row = tablaIngredientes.getSelectedRow();
            if (row >= 0) {
                Ingrediente selectedIngrediente = (Ingrediente) comboBoxIngredientes.getSelectedItem();
                if (selectedIngrediente != null) {
                    model.setValueAt(selectedIngrediente.getTipoPesoLt(), row, 1);
                    model.setValueAt(selectedIngrediente.getCantidadDeCompra(), row, 2);
                    model.setValueAt(selectedIngrediente.getCostoDeCompra(), row, 3);

                    Object currentCantidad = model.getValueAt(row, 4);
                    if (currentCantidad == null || currentCantidad.toString().isEmpty()) {
                        model.setValueAt(0.0, row, 4);
                    }
                    Object currentUnidadU = model.getValueAt(row, 5);
                    if (currentUnidadU == null || currentUnidadU.toString().isEmpty()) {
                        String tipoCompra = selectedIngrediente.getTipoPesoLt();
                        if (java.util.Arrays.asList("gramos", "mililitros", "unidades", "litros", "kilogramos", "cucharadas", "pizcas", "otros").contains(tipoCompra.toLowerCase())) { // .toLowerCase() para mayor robustez
                             model.setValueAt(tipoCompra, row, 5);
                        } else {
                            model.setValueAt("unidades", row, 5);
                        }
                    }
                    actualizarCostoReal(row);
                    actualizarCostoTotal();
                }
            }
        });

        model.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();

                if (column == 4 || column == 0) {
                    actualizarCostoReal(row);
                    actualizarCostoTotal();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaIngredientes);

        // --- Panel Central, con la tabla y el selector de categorías ---
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10)); // Espaciado entre componentes
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel para el selector de categorías
        JPanel categoriaSelectionPanel = new JPanel(new BorderLayout());
        categoriaSelectionPanel.setBorder(BorderFactory.createTitledBorder("Seleccionar Categorías"));
        categoriaSelectionPanel.setBackground(Color.WHITE);

        categoriaListModel = new DefaultListModel<>();
        categoriaList = new JList<>(categoriaListModel);
        categoriaList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // Permite selección múltiple
        JScrollPane categoriaScrollPane = new JScrollPane(categoriaList);
        categoriaScrollPane.setPreferredSize(new Dimension(200, 100)); // Tamaño preferido
        categoriaSelectionPanel.add(categoriaScrollPane, BorderLayout.CENTER);

        centerPanel.add(categoriaSelectionPanel, BorderLayout.EAST); // Colocar a la derecha de la tabla

        // --- Fin Panel Central ---


        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));
        panelInferior.setBackground(new Color(255, 255, 220)); // Mismo fondo

        JPanel panelCostos = new JPanel();
        panelCostos.setLayout(new GridLayout(3, 2, 10, 10));
        panelCostos.setBackground(new Color(255, 255, 220)); // Mismo fondo

        labelCostoTotal = new JLabel("COSTO TOTAL DE INGREDIENTES  $");
        txtCostoTotal = new JTextField("-");
        labelGastosExtra = new JLabel("GASTOS EXTRA  $");
        txtGastosExtra = new JTextField("0.0");
        labelPrecioFinal = new JLabel("PRECIO FINAL ESTIMADO  $");
        labelPrecioFinal.setForeground(Color.RED);
        txtPrecioFinal = new JTextField("-");

        txtCostoTotal.setEditable(false);
        txtPrecioFinal.setEditable(false);

        panelCostos.add(labelCostoTotal);
        panelCostos.add(txtCostoTotal);
        panelCostos.add(labelGastosExtra);
        panelCostos.add(txtGastosExtra);
        panelCostos.add(labelPrecioFinal);
        panelCostos.add(txtPrecioFinal);

        JPanel panelBotonesInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelBotonesInferior.setBackground(new Color(255, 255, 220)); // Mismo fondo

        JButton btnRecalcularPrecio = new JButton("RECALCULAR PRECIO FINAL");
        // Estilizar botones
        btnRecalcularPrecio.setBackground(Color.DARK_GRAY);
        btnRecalcularPrecio.setForeground(Color.WHITE);
        btnRecalcularPrecio.setFont(new Font("Arial", Font.BOLD, 12));
        btnRecalcularPrecio.addActionListener(e -> actualizarPrecioFinal());
        panelBotonesInferior.add(btnRecalcularPrecio);

        JButton btnGuardarReceta = new JButton("GUARDAR RECETA");
        btnGuardarReceta.setBackground(new Color(85, 107, 47)); // Verde oliva
        btnGuardarReceta.setForeground(Color.WHITE);
        btnGuardarReceta.setFont(new Font("Arial", Font.BOLD, 12));
        btnGuardarReceta.addActionListener(e -> guardarRecetaDesdePantallaCostos());
        panelBotonesInferior.add(btnGuardarReceta);

        btnImprimirPlanilla = new JButton("IMPRIMIR PLANILLA");
        btnImprimirPlanilla.setBackground(new Color(70, 130, 180)); // Azul acero
        btnImprimirPlanilla.setForeground(Color.WHITE);
        btnImprimirPlanilla.setFont(new Font("Arial", Font.BOLD, 12));
        btnImprimirPlanilla.addActionListener(e -> imprimirPlanilla());
        panelBotonesInferior.add(btnImprimirPlanilla);

        JButton btnEliminarIngredienteTabla = new JButton("ELIMINAR FILA");
        btnEliminarIngredienteTabla.setBackground(new Color(178, 34, 34)); // Rojo fuego
        btnEliminarIngredienteTabla.setForeground(Color.WHITE);
        btnEliminarIngredienteTabla.setFont(new Font("Arial", Font.BOLD, 12));
        btnEliminarIngredienteTabla.addActionListener(e -> eliminarIngredienteDeTabla());
        panelBotonesInferior.add(btnEliminarIngredienteTabla);

        JButton btnAgregarFila = new JButton("AGREGAR FILA");
        btnAgregarFila.setBackground(Color.DARK_GRAY);
        btnAgregarFila.setForeground(Color.WHITE);
        btnAgregarFila.setFont(new Font("Arial", Font.BOLD, 12));
        btnAgregarFila.addActionListener(e -> agregarFilaATabla());
        panelBotonesInferior.add(btnAgregarFila);

        JButton btnRegresar = new JButton("Regresar");
        btnRegresar.setBackground(Color.GRAY);
        btnRegresar.setForeground(Color.WHITE);
        btnRegresar.setFont(new Font("Arial", Font.BOLD, 12));
        btnRegresar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                regresarAPantallaPrincipal();
            }
        });
        panelBotonesInferior.add(btnRegresar);

        panelInferior.add(panelCostos);
        panelInferior.add(Box.createVerticalStrut(10));
        panelInferior.add(panelBotonesInferior);

        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(centerPanel, BorderLayout.CENTER); // Añadir el centerPanel aquí
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        add(panelPrincipal);

        agregarDocumentListener(txtGastosExtra);

        actualizarPrecioFinal();
        agregarFilaATabla();
    }

    private void agregarDocumentListener(JTextField textField) {
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarPrecioFinal();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarPrecioFinal();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarPrecioFinal();
            }
        });
    }

    private double parsearNumero(String text) {
        if (text == null || text.trim().isEmpty() || text.trim().equals("-")) {
            return 0.0;
        }
        String cleanedText = text.trim().replaceAll("[^\\d\\.,-]", "");

        if (cleanedText.contains(".") && cleanedText.contains(",")) {
            cleanedText = cleanedText.replace(".", "");
            cleanedText = cleanedText.replace(",", ".");
        } else if (cleanedText.contains(",")) {
            cleanedText = cleanedText.replace(",", ".");
        }

        try {
            return Double.parseDouble(cleanedText);
        } catch (NumberFormatException e) {
            System.err.println("Error al parsear el número '" + text + "' después de limpiar. Se devolverá 0.0. Limpiado a: '" + cleanedText + "'");
            return 0.0;
        }
    }

    private void regresarAPantallaPrincipal() {
        this.dispose();
        PantallaPrincipal pantallaPrincipal = new PantallaPrincipal(sessionFactory);
        pantallaPrincipal.setVisible(true);
    }

    private void cargarIngredientes() {
        try (Session session = sessionFactory.openSession()) {
            List<Ingrediente> ingredientes = session.createQuery("FROM Ingrediente", Ingrediente.class).list();
            for (Ingrediente ingrediente : ingredientes) {
                comboBoxIngredientes.addItem(ingrediente);
                ingredientesMap.put(ingrediente.getNombre(), ingrediente);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los ingredientes", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // --- NUEVO MÉTODO PARA CARGAR CATEGORÍAS ---
    private void cargarCategorias() {
        categoriaListModel.clear(); // Limpiar la lista antes de cargar
        try (Session session = sessionFactory.openSession()) {
            List<Categoria> categorias = session.createQuery("FROM Categoria", Categoria.class).list();
            for (Categoria categoria : categorias) {
                categoriaListModel.addElement(categoria); // Añadir categorías al modelo de la JList
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar las categorías: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    // --- FIN NUEVO MÉTODO ---

    private void actualizarCostoReal(int row) {
        try {
            Object productoObj = tablaIngredientes.getValueAt(row, 0);
            Object cantidadUtilizadaObj = tablaIngredientes.getValueAt(row, 4);

            if (productoObj == null || !(productoObj instanceof Ingrediente)) {
                tablaIngredientes.setValueAt("-", row, 6);
                return;
            }

            Ingrediente ingrediente = (Ingrediente) productoObj;

            double cantidadUtilizada = 0.0;
            if (cantidadUtilizadaObj != null && !cantidadUtilizadaObj.toString().trim().isEmpty()) {
                cantidadUtilizada = parsearNumero(cantidadUtilizadaObj.toString());
            }

            double costoDeCompra = ingrediente.getCostoDeCompra();
            double cantidadDeCompra = ingrediente.getCantidadDeCompra();

            double costoReal = 0.0;
            if (cantidadDeCompra > 0) {
                costoReal = (costoDeCompra / cantidadDeCompra) * cantidadUtilizada;
            }

            tablaIngredientes.setValueAt(String.format("$%.2f", costoReal), row, 6);

        } catch (Exception e) {
            tablaIngredientes.setValueAt("-", row, 6);
            System.err.println("Error inesperado al actualizar costo real en fila " + row + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void actualizarCostoTotal() {
        double total = 0.0;
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

        for (int i = 0; i < tablaIngredientes.getRowCount(); i++) {
            Object costoRealObj = tablaIngredientes.getValueAt(i, 6);
            total += parsearNumero(costoRealObj.toString());
        }
        txtCostoTotal.setText(format.format(total));
        actualizarPrecioFinal();
    }

    private void actualizarPrecioFinal() {
        double costoTotal = 0.0;
        double gastosExtra = 0.0;

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

        costoTotal = parsearNumero(txtCostoTotal.getText());
        gastosExtra = parsearNumero(txtGastosExtra.getText());

        double precioFinal = costoTotal + gastosExtra;
        txtPrecioFinal.setText(format.format(precioFinal));
    }

    private void imprimirPlanilla() {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Planilla de Costos de Receta");
            contentStream.endText();

            contentStream.setFont(PDType1Font.HELVETICA, 10);
            float margin = 50;
            float yStart = 700;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            float rowHeight = 20;

            String[] headers = {"PRODUCTO", "TIPO COMPRA", "CANT. COMPRA", "COSTO COMPRA", "CANT. UTIL.", "UNIDAD UTIL.", "COSTO REAL"};
            float[] colWidths = {0.20f, 0.12f, 0.12f, 0.15f, 0.12f, 0.14f, 0.15f};

            float currentY = yStart;

            contentStream.setLeading(rowHeight);
            currentY -= rowHeight;
            float startX = margin;
            for (int i = 0; i < headers.length; i++) {
                contentStream.beginText();
                contentStream.newLineAtOffset(startX + (tableWidth * colWidths[i] / 2) - (PDType1Font.HELVETICA_BOLD.getStringWidth(headers[i]) / 1000 * 10 / 2), yStart);
                contentStream.showText(headers[i]);
                contentStream.endText();
                startX += tableWidth * colWidths[i];
            }

            contentStream.setLineWidth(1);
            contentStream.moveTo(margin, yStart - 5);
            contentStream.lineTo(margin + tableWidth, yStart - 5);
            contentStream.stroke();


            for (int i = 0; i < tablaIngredientes.getRowCount(); i++) {
                Object productNameObj = tablaIngredientes.getValueAt(i, 0);
                if (productNameObj == null || productNameObj.toString().isEmpty()) {
                    continue;
                }
                String productName = productNameObj.toString();

                startX = margin;
                currentY -= rowHeight;

                for (int j = 0; j < tablaIngredientes.getColumnCount(); j++) {
                    Object value = tablaIngredientes.getValueAt(i, j);
                    String text = "";
                    if (value instanceof Ingrediente) {
                        text = ((Ingrediente) value).getNombre();
                    } else {
                        text = (value != null) ? value.toString() : "";
                    }

                    contentStream.beginText();
                    float textWidth = PDType1Font.HELVETICA.getStringWidth(text) / 1000 * 10;
                    float offset = startX + (tableWidth * colWidths[j] / 2) - (textWidth / 2);
                    contentStream.newLineAtOffset(offset, currentY);
                    contentStream.showText(text);
                    contentStream.endText();
                    startX += tableWidth * colWidths[j];
                }
                contentStream.moveTo(margin, currentY - 5);
                contentStream.lineTo(margin + tableWidth, currentY - 5);
                contentStream.stroke();
            }

            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            currentY -= 30;
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, currentY);
            contentStream.showText("COSTO TOTAL DE INGREDIENTES: " + txtCostoTotal.getText());
            contentStream.newLineAtOffset(0, -rowHeight);
            contentStream.showText("GASTOS EXTRA: " + txtGastosExtra.getText());
            contentStream.newLineAtOffset(0, -rowHeight);
            contentStream.showText("PRECIO FINAL ESTIMADO: " + txtPrecioFinal.getText());
            contentStream.endText();


        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al generar el PDF: " + ex.getMessage(), "Error de PDF", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Planilla de Costos");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
            }
            try {
                document.save(fileToSave);
                document.close();
                JOptionPane.showMessageDialog(this, "Planilla guardada en: " + fileToSave.getAbsolutePath());
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(fileToSave);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar el archivo: " + ex.getMessage(), "Error de Guardado", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        } else {
            try {
                document.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void guardarRecetaDesdePantallaCostos() {
        String nombreReceta = JOptionPane.showInputDialog(this, "Ingrese el nombre de la receta:");
        if (nombreReceta == null || nombreReceta.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre de la receta no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String descripcionReceta = JOptionPane.showInputDialog(this, "Ingrese la descripción de la receta (opcional):");
        if (descripcionReceta == null) {
            descripcionReceta = "";
        }

        Receta nuevaReceta = new Receta(nombreReceta.trim(), descripcionReceta.trim());
        nuevaReceta.setFechaCreacion(new Date());

        double costoTotalCalculado = parsearNumero(txtCostoTotal.getText());
        nuevaReceta.setCostoTotal(costoTotalCalculado);

        // --- Obtener categorías seleccionadas ---
        List<Categoria> selectedCategories = categoriaList.getSelectedValuesList();
        if (selectedCategories.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione al menos una categoría para la receta.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        nuevaReceta.setCategorias(selectedCategories); // Asignar categorías a la receta
        // --- Fin Obtener categorías seleccionadas ---


        List<RecetaIngrediente> recetaIngredientes = new ArrayList<>();
        for (int i = 0; i < tablaIngredientes.getRowCount(); i++) {
            Object productoObj = tablaIngredientes.getValueAt(i, 0);
            Object cantidadUtilizadaObj = tablaIngredientes.getValueAt(i, 4);
            Object unidadUtilizadaObj = tablaIngredientes.getValueAt(i, 5);

            if (productoObj != null && productoObj instanceof Ingrediente &&
                cantidadUtilizadaObj != null && !cantidadUtilizadaObj.toString().isEmpty()) {

                Ingrediente ingrediente = (Ingrediente) productoObj;
                try {
                    double cantidadUtilizada = parsearNumero(cantidadUtilizadaObj.toString());
                    String unidadUtilizada = (unidadUtilizadaObj != null) ? unidadUtilizadaObj.toString() : "";

                    double costoDeCompra = ingrediente.getCostoDeCompra();
                    double cantidadDeCompra = ingrediente.getCantidadDeCompra();
                    double costoRealParaEsteIngrediente = 0.0;
                    if (cantidadDeCompra > 0) {
                        costoRealParaEsteIngrediente = (costoDeCompra / cantidadDeCompra) * cantidadUtilizada;
                    }

                    RecetaIngrediente ri = new RecetaIngrediente(nuevaReceta, ingrediente, cantidadUtilizada);
                    ri.setCostoTotal(costoRealParaEsteIngrediente);
                    ri.setUnidadUtilizada(unidadUtilizada);

                    recetaIngredientes.add(ri);

                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Cantidad utilizada inválida para el ingrediente '" + ingrediente.getNombre() + "'.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        if (recetaIngredientes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La receta no contiene ingredientes. No se guardará.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        nuevaReceta.setRecetaIngredientes(recetaIngredientes);
        nuevaReceta.recalcularCostoTotal();

        Session session = null;
        Transaction tx = null;
        try {
            session = sessionFactory.openSession();
            tx = session.beginTransaction();
            session.save(nuevaReceta);
            tx.commit();
            JOptionPane.showMessageDialog(this, "Receta '" + nombreReceta + "' guardada con éxito.");
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al guardar la receta: " + e.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void eliminarIngredienteDeTabla() {
        int selectedRow = tablaIngredientes.getSelectedRow();
        if (selectedRow >= 0) {
            DefaultTableModel model = (DefaultTableModel) tablaIngredientes.getModel();
            model.removeRow(selectedRow);
            actualizarCostoTotal();
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione una fila para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void agregarFilaATabla() {
        DefaultTableModel model = (DefaultTableModel) tablaIngredientes.getModel();
        model.addRow(new Object[]{null, "", 0.0, 0.0, 0.0, "", String.format("$%.2f", 0.0)});
    }
}