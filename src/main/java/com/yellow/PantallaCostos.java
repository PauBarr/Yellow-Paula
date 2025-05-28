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

// Importaciones para PDFBox
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
    private JComboBox<Ingrediente> comboBoxIngredientes; // Combo para seleccionar productos
    private JComboBox<String> comboBoxUnidades; // Combo para seleccionar unidades de uso (gramos, unidades, ml, etc.)
    private JLabel labelCostoTotal, labelGastosExtra, labelPrecioFinal;
    private JTextField txtCostoTotal, txtGastosExtra, txtPrecioFinal;
    private JButton btnImprimirPlanilla;

    private Map<String, Ingrediente> ingredientesMap;

    public PantallaCostos(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        ingredientesMap = new HashMap<>();
        initComponents();
        cargarIngredientes();
    }

    private void initComponents() {
        setTitle("Pantalla de Costos - Pepas");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panelPrincipal = new JPanel(new BorderLayout());

        JPanel panelSuperior = new JPanel();
        JLabel labelTitulo = new JLabel("PEPAS");
        labelTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        labelTitulo.setForeground(new Color(85, 107, 47));
        panelSuperior.add(labelTitulo);

        // Columnas de la tabla actualizadas
        // "PRODUCTO" (Selección de Ingrediente)
        // "TIPO COMPRA" (ej. "kg", "unidad") - Mostrar la unidad de compra del ingrediente
        // "CANTIDAD COMPRA" (ej. 1000g para 1kg, 1 para 1 unidad) - La cantidad total que viene en el paquete/unidad comprada
        // "COSTO COMPRA" (ej. $5600 por el paquete, $200 por la banana) - El costo total de ese paquete/unidad
        // "CANT. UTILIZADA" (Cuanto se usa de la compra, ej. 200g, 1 unidad) - Editable
        // "UNIDAD UTILIZADA" (Unidad de la Cant. Utilizada, ej. "gramos", "unidades") - Editable
        // "COSTO REAL" (Calculado: (Costo Compra / Cantidad Compra) * Cant. Utilizada)
        String[] columnNames = {"PRODUCTO", "TIPO COMPRA", "CANTIDAD COMPRA", "COSTO COMPRA", "CANT. UTILIZADA", "UNIDAD UTILIZADA", "COSTO REAL"};

        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo editable el producto, cantidad utilizada y unidad utilizada
                return column == 0 || column == 4 || column == 5;
            }
        };

        tablaIngredientes = new JTable(model);

        // Editor para la columna "PRODUCTO"
        comboBoxIngredientes = new JComboBox<>();
        tablaIngredientes.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(comboBoxIngredientes));

        // Editor para la columna "UNIDAD UTILIZADA"
        comboBoxUnidades = new JComboBox<>(new String[]{"gramos", "mililitros", "unidades", "litros", "kilogramos", "cucharadas", "pizcas", "otros"});
        tablaIngredientes.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(comboBoxUnidades));


        comboBoxIngredientes.addActionListener(e -> {
            int row = tablaIngredientes.getSelectedRow();
            if (row >= 0) {
                Ingrediente selectedIngrediente = (Ingrediente) comboBoxIngredientes.getSelectedItem();
                if (selectedIngrediente != null) {
                    // Actualizar las columnas con los datos del ingrediente seleccionado
                    model.setValueAt(selectedIngrediente.getTipoPesoLt(), row, 1); // TIPO COMPRA
                    model.setValueAt(selectedIngrediente.getCantidadDeCompra(), row, 2); // CANTIDAD COMPRA
                    model.setValueAt(selectedIngrediente.getCostoDeCompra(), row, 3); // COSTO COMPRA

                    // Asegurar que Cant. Utilizada y Unidad Utilizada tengan valores iniciales
                    Object currentCantidad = model.getValueAt(row, 4);
                    if (currentCantidad == null || currentCantidad.toString().isEmpty()) {
                        model.setValueAt(0.0, row, 4);
                    }
                    Object currentUnidadU = model.getValueAt(row, 5);
                    if (currentUnidadU == null || currentUnidadU.toString().isEmpty()) {
                         // Intentar inicializar con la unidad de compra, si es una de las opciones de uso
                        String tipoCompra = selectedIngrediente.getTipoPesoLt();
                        if (java.util.Arrays.asList("gramos", "mililitros", "unidades", "litros", "kilogramos", "cucharadas", "pizcas", "otros").contains(tipoCompra)) {
                             model.setValueAt(tipoCompra, row, 5);
                        } else {
                            model.setValueAt("unidades", row, 5); // Valor por defecto
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

                // Recalcular si cambia la cantidad utilizada (columna 4) o si el ingrediente cambia (columna 0)
                if (column == 4 || column == 0) {
                    actualizarCostoReal(row);
                    actualizarCostoTotal();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaIngredientes);

        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));

        JPanel panelCostos = new JPanel();
        panelCostos.setLayout(new GridLayout(3, 2, 10, 10)); // Cambiado a 3 filas para el Precio Final

        labelCostoTotal = new JLabel("COSTO TOTAL DE INGREDIENTES  $"); // Texto actualizado
        txtCostoTotal = new JTextField("-");
        labelGastosExtra = new JLabel("GASTOS EXTRA  $");
        txtGastosExtra = new JTextField("0.0");
        labelPrecioFinal = new JLabel("PRECIO FINAL ESTIMADO  $"); // Texto actualizado
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

        JButton btnRecalcularPrecio = new JButton("RECALCULAR PRECIO FINAL");
        btnRecalcularPrecio.addActionListener(e -> actualizarPrecioFinal());
        panelBotonesInferior.add(btnRecalcularPrecio);

        JButton btnGuardarReceta = new JButton("GUARDAR RECETA");
        btnGuardarReceta.addActionListener(e -> guardarRecetaDesdePantallaCostos());
        panelBotonesInferior.add(btnGuardarReceta);

        btnImprimirPlanilla = new JButton("IMPRIMIR PLANILLA");
        btnImprimirPlanilla.addActionListener(e -> imprimirPlanilla());
        panelBotonesInferior.add(btnImprimirPlanilla);

        JButton btnEliminarIngredienteTabla = new JButton("ELIMINAR FILA"); // Texto actualizado
        btnEliminarIngredienteTabla.addActionListener(e -> eliminarIngredienteDeTabla());
        panelBotonesInferior.add(btnEliminarIngredienteTabla);

        JButton btnAgregarFila = new JButton("AGREGAR FILA");
        btnAgregarFila.addActionListener(e -> agregarFilaATabla());
        panelBotonesInferior.add(btnAgregarFila);

        JButton btnRegresar = new JButton("Regresar");
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
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);
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

    // Método para parsear números (ya existente y robusto)
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

    private void actualizarCostoReal(int row) {
        try {
            Object productoObj = tablaIngredientes.getValueAt(row, 0); // Producto (Ingrediente)
            Object cantidadUtilizadaObj = tablaIngredientes.getValueAt(row, 4); // CANT. UTILIZADA

            if (productoObj == null || !(productoObj instanceof Ingrediente)) {
                tablaIngredientes.setValueAt("-", row, 6); // Columna "COSTO REAL"
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
            if (cantidadDeCompra > 0) { // Evitar división por cero
                costoReal = (costoDeCompra / cantidadDeCompra) * cantidadUtilizada;
            }

            tablaIngredientes.setValueAt(String.format("$%.2f", costoReal), row, 6); // Columna "COSTO REAL"

        } catch (Exception e) {
            tablaIngredientes.setValueAt("-", row, 6); // Columna "COSTO REAL"
            System.err.println("Error inesperado al actualizar costo real en fila " + row + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void actualizarCostoTotal() {
        double total = 0.0;
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

        for (int i = 0; i < tablaIngredientes.getRowCount(); i++) {
            Object costoRealObj = tablaIngredientes.getValueAt(i, 6); // Ahora es la columna 6
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

            // Headers actualizados para el PDF
            String[] headers = {"PRODUCTO", "TIPO COMPRA", "CANT. COMPRA", "COSTO COMPRA", "CANT. UTIL.", "UNIDAD UTIL.", "COSTO REAL"};
            float[] colWidths = {0.20f, 0.12f, 0.12f, 0.15f, 0.12f, 0.14f, 0.15f}; // Ajustar anchos para 7 columnas

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
                String productName = productNameObj.toString(); // Usar toString() del Ingrediente para el nombre

                startX = margin;
                currentY -= rowHeight;

                for (int j = 0; j < tablaIngredientes.getColumnCount(); j++) {
                    Object value = tablaIngredientes.getValueAt(i, j);
                    String text = "";
                    if (value instanceof Ingrediente) { // Si es el objeto Ingrediente en la primera columna
                        text = ((Ingrediente) value).getNombre();
                    } else {
                        text = (value != null) ? value.toString() : "";
                    }

                    contentStream.beginText();
                    // Calcular offset para centrar el texto en la columna
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

        List<RecetaIngrediente> recetaIngredientes = new ArrayList<>();
        for (int i = 0; i < tablaIngredientes.getRowCount(); i++) {
            Object productoObj = tablaIngredientes.getValueAt(i, 0); // Objecto Ingrediente
            Object cantidadUtilizadaObj = tablaIngredientes.getValueAt(i, 4); // Cantidad Utilizada
            Object unidadUtilizadaObj = tablaIngredientes.getValueAt(i, 5); // Unidad Utilizada

            if (productoObj != null && productoObj instanceof Ingrediente &&
                cantidadUtilizadaObj != null && !cantidadUtilizadaObj.toString().isEmpty()) {

                Ingrediente ingrediente = (Ingrediente) productoObj;
                try {
                    double cantidadUtilizada = parsearNumero(cantidadUtilizadaObj.toString());
                    String unidadUtilizada = (unidadUtilizadaObj != null) ? unidadUtilizadaObj.toString() : "";

                    // Calcular el costo real de este ingrediente en la receta
                    double costoDeCompra = ingrediente.getCostoDeCompra();
                    double cantidadDeCompra = ingrediente.getCantidadDeCompra();
                    double costoRealParaEsteIngrediente = 0.0;
                    if (cantidadDeCompra > 0) {
                        costoRealParaEsteIngrediente = (costoDeCompra / cantidadDeCompra) * cantidadUtilizada;
                    }

                    RecetaIngrediente ri = new RecetaIngrediente(nuevaReceta, ingrediente, cantidadUtilizada);
                    // Aquí la idea es que el RecetaIngrediente sepa su costo real por sí mismo
                    // Pero como el método getCostoReal() de RecetaIngrediente usa Ingrediente.getCostoUnitario(),
                    // deberíamos asegurar que eso funcione con el nuevo Ingrediente.
                    // Para evitar más cambios complejos en RecetaIngrediente ahora, podemos pasarle el costo calculado.
                    ri.setCostoTotal(costoRealParaEsteIngrediente); // Usamos setCostoTotal temporalmente para guardar el valor
                    ri.setUnidadUtilizada(unidadUtilizada); // Nuevo campo en RecetaIngrediente si se necesita guardar

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
        nuevaReceta.recalcularCostoTotal(); // Este método en Receta también deberá usar la nueva lógica.

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
        // Las nuevas filas no tienen valores por defecto para las columnas que antes eran 0.0 o String.format.
        // Se inicializan como nulos o cadenas vacías para que el usuario las complete.
        model.addRow(new Object[]{null, "", 0.0, 0.0, 0.0, "", String.format("$%.2f", 0.0)});
    }
}