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
    private JComboBox<Ingrediente> comboBoxIngredientes;
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

        String[] columnNames = {"PRODUCTO", "TIPO PESO/LT", "PESO/LT R", "COSTO UNIT.", "CANT. UTILIZADA", "COSTO REAL"};
        
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 4;
            }
        }; 
        
        tablaIngredientes = new JTable(model);

        comboBoxIngredientes = new JComboBox<>();
        tablaIngredientes.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(comboBoxIngredientes));

        comboBoxIngredientes.addActionListener(e -> {
            int row = tablaIngredientes.getSelectedRow();
            if (row >= 0) {
                Ingrediente selectedIngrediente = (Ingrediente) comboBoxIngredientes.getSelectedItem();
                if (selectedIngrediente != null) {
                    model.setValueAt(selectedIngrediente.getTipoPesoLt(), row, 1);
                    model.setValueAt(selectedIngrediente.getPesoLtR(), row, 2);
                    model.setValueAt(selectedIngrediente.getCostoUnitario(), row, 3);
                    
                    Object currentCantidad = model.getValueAt(row, 4);
                    if (currentCantidad == null || currentCantidad.toString().isEmpty()) {
                        model.setValueAt(0.0, row, 4); 
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

                if (column == 4 || column == 3) {
                    actualizarCostoReal(row);
                    actualizarCostoTotal();
                } 
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaIngredientes);

        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));

        JPanel panelCostos = new JPanel();
        panelCostos.setLayout(new GridLayout(2, 2, 10, 10));

        labelCostoTotal = new JLabel("COSTO TOTAL POR PRODUCTO  $");
        txtCostoTotal = new JTextField("-");
        labelGastosExtra = new JLabel("GASTOS EXTRA  $");
        txtGastosExtra = new JTextField("0.0"); 
        labelPrecioFinal = new JLabel("PRECIO FINAL  $");
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
        
        JButton btnEliminarIngredienteTabla = new JButton("ELIMINAR INGREDIENTE");
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

    // *** NUEVA FUNCIÓN AUXILIAR PARA PARSEAR NÚMEROS DE FORMA ROBUSTA ***
    private double parsearNumero(String text) {
        if (text == null || text.trim().isEmpty() || text.trim().equals("-")) {
            return 0.0;
        }
        // Limpiar el string:
        // 1. Eliminar símbolos de moneda y otros caracteres no numéricos (excepto . y ,)
        String cleanedText = text.trim().replaceAll("[^\\d\\.,-]", ""); // Mantiene dígitos, punto, coma y guion (para negativos)
        
        // 2. Determinar si la coma es separador decimal o de miles.
        // Heurística simple: Si hay un punto Y una coma, la coma es decimal (ej. 1.234,56).
        // Si solo hay una coma, la coma es decimal (ej. 123,45).
        // Si solo hay un punto, el punto es decimal (ej. 123.45).
        
        if (cleanedText.contains(".") && cleanedText.contains(",")) {
            // Ejemplo: 1.234,56 -> 1234.56
            cleanedText = cleanedText.replace(".", ""); // Elimina el separador de miles (punto)
            cleanedText = cleanedText.replace(",", "."); // Reemplaza la coma decimal por punto
        } else if (cleanedText.contains(",")) {
            // Ejemplo: 123,45 -> 123.45 (coma es decimal)
            cleanedText = cleanedText.replace(",", "."); // Reemplaza la coma decimal por punto
        }
        // Si solo hay punto, ya está bien para Double.parseDouble (ej. 123.45)

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
            Object cantidadObj = tablaIngredientes.getValueAt(row, 4);
            Object costoUnitarioObj = tablaIngredientes.getValueAt(row, 3);

            double cantidad = 0.0;
            double costoUnitario = 0.0;

            if (cantidadObj != null && !cantidadObj.toString().trim().isEmpty()) {
                // Usar la función robusta para parsear
                cantidad = parsearNumero(cantidadObj.toString());
            }

            if (costoUnitarioObj != null && !costoUnitarioObj.toString().trim().isEmpty()) {
                // Usar la función robusta para parsear
                costoUnitario = parsearNumero(costoUnitarioObj.toString());
            }
            
            double costoReal = cantidad * costoUnitario;
            tablaIngredientes.setValueAt(String.format("$%.2f", costoReal), row, 5);
            
        } catch (Exception e) { 
            tablaIngredientes.setValueAt("-", row, 5);
            System.err.println("Error inesperado al actualizar costo real en fila " + row + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void actualizarCostoTotal() {
        double total = 0.0;
        // Solo para formatear la salida, no para parsear de aquí
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "AR")); 

        for (int i = 0; i < tablaIngredientes.getRowCount(); i++) {
            Object costoRealObj = tablaIngredientes.getValueAt(i, 5);
            // Usar la función robusta para parsear el costo real de la tabla
            total += parsearNumero(costoRealObj.toString());
        }
        txtCostoTotal.setText(format.format(total));
        actualizarPrecioFinal(); 
    }

    private void actualizarPrecioFinal() {
        double costoTotal = 0.0;
        double gastosExtra = 0.0;

        // Se mantiene para formatear la salida
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "AR")); 

        // Usar la función robusta para parsear el costo total
        costoTotal = parsearNumero(txtCostoTotal.getText());

        // Usar la función robusta para parsear los gastos extra
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

            String[] headers = {"PRODUCTO", "TIPO", "PESO/LT", "COSTO U.", "CANT. UTIL.", "COSTO REAL"};
            float[] colWidths = {0.25f, 0.15f, 0.15f, 0.15f, 0.15f, 0.15f};

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
                Object productName = tablaIngredientes.getValueAt(i, 0);
                if (productName == null || productName.toString().isEmpty()) {
                    continue;
                }
                
                startX = margin;
                currentY -= rowHeight;

                for (int j = 0; j < tablaIngredientes.getColumnCount(); j++) {
                    Object value = tablaIngredientes.getValueAt(i, j);
                    String text = (value != null) ? value.toString() : "";
                    contentStream.beginText();
                    contentStream.newLineAtOffset(startX + (tableWidth * colWidths[j] / 2) - (PDType1Font.HELVETICA.getStringWidth(text) / 1000 * 10 / 2), currentY);
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
            contentStream.showText("COSTO TOTAL POR PRODUCTO: " + txtCostoTotal.getText());
            contentStream.newLineAtOffset(0, -rowHeight);
            contentStream.showText("GASTOS EXTRA: " + txtGastosExtra.getText());
            contentStream.newLineAtOffset(0, -rowHeight); 
            contentStream.showText("PRECIO FINAL: " + txtPrecioFinal.getText());
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

        double costoTotalCalculado = 0.0;
        // *** CAMBIO CLAVE: Usar la función robusta para parsear el costo total al guardar ***
        costoTotalCalculado = parsearNumero(txtCostoTotal.getText());
        
        nuevaReceta.setCostoTotal(costoTotalCalculado);

        List<RecetaIngrediente> recetaIngredientes = new ArrayList<>();
        for (int i = 0; i < tablaIngredientes.getRowCount(); i++) {
            Object productoObj = tablaIngredientes.getValueAt(i, 0);
            Object cantidadObj = tablaIngredientes.getValueAt(i, 4);

            if (productoObj != null && !productoObj.toString().isEmpty() && cantidadObj != null && !cantidadObj.toString().isEmpty()) {
                String nombreIngrediente = productoObj.toString();
                try {
                    // Usar la función robusta para parsear la cantidad utilizada
                    double cantidadUtilizada = parsearNumero(cantidadObj.toString());

                    Ingrediente ingrediente = ingredientesMap.get(nombreIngrediente);
                    if (ingrediente == null) {
                        try (Session session = sessionFactory.openSession()) {
                            ingrediente = session.createQuery("FROM Ingrediente WHERE nombre = :nombre", Ingrediente.class)
                                .setParameter("nombre", nombreIngrediente)
                                .uniqueResult();
                        }
                    }

                    if (ingrediente != null) {
                        RecetaIngrediente ri = new RecetaIngrediente(nuevaReceta, ingrediente, cantidadUtilizada);
                        ri.setCostoTotal(cantidadUtilizada * ingrediente.getCostoUnitario()); 
                        recetaIngredientes.add(ri);
                    } else {
                        JOptionPane.showMessageDialog(this, "Ingrediente '" + nombreIngrediente + "' no encontrado. No se guardará en la receta.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Cantidad utilizada inválida para el ingrediente '" + nombreIngrediente + "'.", "Error", JOptionPane.ERROR_MESSAGE);
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
        model.addRow(new Object[]{null, "", 0.0, 0.0, 0.0, String.format("$%.2f", 0.0)});
    }
}