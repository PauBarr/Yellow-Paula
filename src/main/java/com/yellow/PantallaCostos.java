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
import java.awt.event.KeyEvent;
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
import java.util.Set;
import java.util.HashSet;


public class PantallaCostos extends JFrame {

    private static final long serialVersionUID = 1L;
    private SessionFactory sessionFactory;
    private JTable tablaIngredientes;
    private JComboBox<Ingrediente> comboBoxIngredientes;
    private JComboBox<String> comboBoxUnidades;
    private JLabel etiquetaCostoTotal, etiquetaGastosExtra, etiquetaPrecioFinal;
    private JTextField campoCostoTotal, campoGastosExtra, campoPrecioFinal;
    private JButton botonImprimirPlanilla;

    private Map<String, Ingrediente> mapaIngredientes;

    private List<Categoria> categoriasSeleccionadas;
    private JButton botonSeleccionarCategorias;

    public PantallaCostos(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        mapaIngredientes = new HashMap<>();
        categoriasSeleccionadas = new ArrayList<>();
        initComponents();
        cargarIngredientes();
    }

    private void initComponents() {
        setTitle("Pantalla de Costos - Yellow");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(new Color(255, 255, 220));

        JPanel panelSuperior = new JPanel();
        panelSuperior.setBackground(new Color(255, 255, 220));
        JLabel etiquetaTitulo = new JLabel("NUEVA RECETA");
        etiquetaTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        etiquetaTitulo.setForeground(new Color(85, 107, 47));
        panelSuperior.add(etiquetaTitulo);

        String[] nombresColumnas = {"PRODUCTO", "TIPO COMPRA", "CANTIDAD COMPRA", "COSTO COMPRA", "CANT. UTILIZADA", "UNIDAD UTILIZADA", "COSTO REAL"};

        DefaultTableModel modeloTabla = new DefaultTableModel(nombresColumnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 4 || column == 5;
            }
        };

        tablaIngredientes = new JTable(modeloTabla);

        comboBoxIngredientes = new JComboBox<>();
        tablaIngredientes.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(comboBoxIngredientes));

        comboBoxUnidades = new JComboBox<>(new String[]{"gramos", "mililitros", "unidades", "litros", "kilogramos", "cucharadas", "pizcas", "otros"});
        tablaIngredientes.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(comboBoxUnidades));

        comboBoxIngredientes.addActionListener(e -> {
            int fila = tablaIngredientes.getSelectedRow();
            if (fila >= 0) {
                Ingrediente ingredienteSeleccionado = (Ingrediente) comboBoxIngredientes.getSelectedItem();
                if (ingredienteSeleccionado != null) {
                    modeloTabla.setValueAt(ingredienteSeleccionado.getTipoPesoLt(), fila, 1);
                    modeloTabla.setValueAt(ingredienteSeleccionado.getCantidadDeCompra(), fila, 2);
                    modeloTabla.setValueAt(ingredienteSeleccionado.getCostoDeCompra(), fila, 3);

                    Object cantidadActual = modeloTabla.getValueAt(fila, 4);
                    if (cantidadActual == null || cantidadActual.toString().isEmpty()) {
                        modeloTabla.setValueAt(0.0, fila, 4);
                    }
                    Object unidadActualU = modeloTabla.getValueAt(fila, 5);
                    if (unidadActualU == null || unidadActualU.toString().isEmpty()) {
                        String tipoCompra = ingredienteSeleccionado.getTipoPesoLt();
                        if (java.util.Arrays.asList("gramos", "mililitros", "unidades", "litros", "kilogramos", "cucharadas", "pizcas", "otros").contains(tipoCompra.toLowerCase())) {
                             modeloTabla.setValueAt(tipoCompra, fila, 5);
                        } else {
                            modeloTabla.setValueAt("unidades", fila, 5);
                        }
                    }
                    actualizarCostoReal(fila);
                    actualizarCostoTotal();
                }
            }
        });

        modeloTabla.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int fila = e.getFirstRow();
                int columna = e.getColumn();

                if (columna == 4 || columna == 0) {
                    actualizarCostoReal(fila);
                    actualizarCostoTotal();
                }
            }
        });

        JScrollPane panelDesplazamiento = new JScrollPane(tablaIngredientes);

        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.add(panelDesplazamiento, BorderLayout.CENTER);

        panelPrincipal.add(panelCentral, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));
        panelInferior.setBackground(new Color(255, 255, 220));

        JPanel panelCostos = new JPanel();
        panelCostos.setLayout(new GridLayout(3, 2, 10, 10));
        panelCostos.setBackground(new Color(255, 255, 220));

        etiquetaCostoTotal = new JLabel("COSTO TOTAL DE INGREDIENTES  $");
        campoCostoTotal = new JTextField("-");
        etiquetaGastosExtra = new JLabel("GASTOS EXTRA  $");
        campoGastosExtra = new JTextField("0.0");
        etiquetaPrecioFinal = new JLabel("PRECIO FINAL ESTIMADO  $");
        etiquetaPrecioFinal.setForeground(Color.RED);
        campoPrecioFinal = new JTextField("-");

        campoCostoTotal.setEditable(false);
        campoPrecioFinal.setEditable(false);

        panelCostos.add(etiquetaCostoTotal);
        panelCostos.add(campoCostoTotal);
        panelCostos.add(etiquetaGastosExtra);
        panelCostos.add(campoGastosExtra);
        panelCostos.add(etiquetaPrecioFinal);
        panelCostos.add(campoPrecioFinal);

        // MODIFICACIÓN APLICADA AQUÍ: CAMBIO DE FlowLayout A GridLayout
        JPanel panelBotonesInferior = new JPanel(new GridLayout(2, 4, 10, 10)); // 2 filas, 4 columnas
        panelBotonesInferior.setBackground(new Color(255, 255, 220));

        JButton botonRecalcularPrecio = new JButton("RECALCULAR PRECIO FINAL");
        botonRecalcularPrecio.setBackground(Color.DARK_GRAY);
        botonRecalcularPrecio.setForeground(Color.WHITE);
        botonRecalcularPrecio.setFont(new Font("Arial", Font.BOLD, 12));
        botonRecalcularPrecio.addActionListener(e -> actualizarPrecioFinal());
        panelBotonesInferior.add(botonRecalcularPrecio);

        botonSeleccionarCategorias = new JButton("SELECCIONAR CATEGORÍAS");
        botonSeleccionarCategorias.setBackground(new Color(70, 130, 180));
        botonSeleccionarCategorias.setForeground(Color.WHITE);
        botonSeleccionarCategorias.setFont(new Font("Arial", Font.BOLD, 12));
        botonSeleccionarCategorias.addActionListener(e -> seleccionarCategorias());
        panelBotonesInferior.add(botonSeleccionarCategorias);


        JButton botonGuardarReceta = new JButton("GUARDAR RECETA");
        botonGuardarReceta.setBackground(new Color(85, 107, 47));
        botonGuardarReceta.setForeground(Color.WHITE);
        botonGuardarReceta.setFont(new Font("Arial", Font.BOLD, 12));
        botonGuardarReceta.addActionListener(e -> guardarRecetaDesdePantallaCostos());
        panelBotonesInferior.add(botonGuardarReceta);

        // --- CÓDIGO PARA EL ATAJO DE TECLADO ---
        Action guardarAccion = new AbstractAction("guardarReceta") {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarRecetaDesdePantallaCostos();
            }
        };

        panelPrincipal.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                      .put(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK), "guardarReceta");

        panelPrincipal.getActionMap().put("guardarReceta", guardarAccion);
        // --- FIN CÓDIGO PARA EL ATAJO DE TECLADO ---


        botonImprimirPlanilla = new JButton("IMPRIMIR PLANILLA");
        botonImprimirPlanilla.setBackground(new Color(70, 130, 180));
        botonImprimirPlanilla.setForeground(Color.WHITE);
        botonImprimirPlanilla.setFont(new Font("Arial", Font.BOLD, 12));
        botonImprimirPlanilla.addActionListener(e -> imprimirPlanilla());
        panelBotonesInferior.add(botonImprimirPlanilla);

        JButton botonEliminarIngredienteTabla = new JButton("ELIMINAR FILA");
        botonEliminarIngredienteTabla.setBackground(new Color(178, 34, 34));
        botonEliminarIngredienteTabla.setForeground(Color.WHITE);
        botonEliminarIngredienteTabla.setFont(new Font("Arial", Font.BOLD, 12));
        botonEliminarIngredienteTabla.addActionListener(e -> eliminarIngredienteDeTabla());
        panelBotonesInferior.add(botonEliminarIngredienteTabla);

        JButton botonAgregarFila = new JButton("AGREGAR FILA");
        botonAgregarFila.setBackground(Color.DARK_GRAY);
        botonAgregarFila.setForeground(Color.WHITE);
        botonAgregarFila.setFont(new Font("Arial", Font.BOLD, 12));
        botonAgregarFila.addActionListener(e -> agregarFilaATabla());
        panelBotonesInferior.add(botonAgregarFila);

        // --- Botón de Regresar Restaurado ---
        JButton botonRegresar = new JButton("Regresar");
        botonRegresar.setBackground(Color.GRAY);
        botonRegresar.setForeground(Color.WHITE);
        botonRegresar.setFont(new Font("Arial", Font.BOLD, 12));
        botonRegresar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                regresarAPantallaPrincipal();
            }
        });
        panelBotonesInferior.add(botonRegresar); // Se agrega de nuevo al panel de botones
        // --- Fin Restauración del Botón de Regresar ---

        panelInferior.add(panelCostos);
        panelInferior.add(Box.createVerticalStrut(10));
        panelInferior.add(panelBotonesInferior);

        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(panelCentral, BorderLayout.CENTER);
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        add(panelPrincipal);

        agregarDocumentListener(campoGastosExtra);

        actualizarPrecioFinal();
        agregarFilaATabla();
    }

    private void agregarDocumentListener(JTextField campoTexto) {
        campoTexto.getDocument().addDocumentListener(new DocumentListener() {
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

    private double parsearNumero(String texto) {
        if (texto == null || texto.trim().isEmpty() || texto.trim().equals("-")) {
            return 0.0;
        }
        String textoLimpio = texto.trim().replaceAll("[^\\d\\.,-]", "");

        if (textoLimpio.contains(".") && textoLimpio.contains(",")) {
            textoLimpio = textoLimpio.replace(".", "");
            textoLimpio = textoLimpio.replace(",", ".");
        } else if (textoLimpio.contains(",")) {
            textoLimpio = textoLimpio.replace(",", ".");
        }

        try {
            return Double.parseDouble(textoLimpio);
        } catch (NumberFormatException e) {
            System.err.println("Error al parsear el número '" + texto + "' después de limpiar. Se devolverá 0.0. Limpiado a: '" + textoLimpio + "'");
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
                mapaIngredientes.put(ingrediente.getNombre(), ingrediente);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los ingredientes", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void seleccionarCategorias() {
        SeleccionarCategoriasDialog dialogoCategorias = new SeleccionarCategoriasDialog(this, sessionFactory, categoriasSeleccionadas);
        dialogoCategorias.setVisible(true);

        if (dialogoCategorias.isConfirmed()) {
            categoriasSeleccionadas = dialogoCategorias.getSelectedCategories();
            JOptionPane.showMessageDialog(this, "Categorías seleccionadas: " + categoriasSeleccionadas.stream().map(Categoria::getNombreCategoria).reduce((a, b) -> a + ", " + b).orElse("Ninguna"));
        }
    }

    private void actualizarCostoReal(int fila) {
        try {
            Object objetoProducto = tablaIngredientes.getValueAt(fila, 0);
            Object objetoCantidadUtilizada = tablaIngredientes.getValueAt(fila, 4);

            if (objetoProducto == null || !(objetoProducto instanceof Ingrediente)) {
                tablaIngredientes.setValueAt("-", fila, 6);
                return;
            }

            Ingrediente ingrediente = (Ingrediente) objetoProducto;

            double cantidadUtilizada = 0.0;
            if (objetoCantidadUtilizada != null && !objetoCantidadUtilizada.toString().trim().isEmpty()) {
                cantidadUtilizada = parsearNumero(objetoCantidadUtilizada.toString());
            }

            double costoDeCompra = ingrediente.getCostoDeCompra();
            double cantidadDeCompra = ingrediente.getCantidadDeCompra();

            double costoReal = 0.0;
            if (cantidadDeCompra > 0) {
                costoReal = (costoDeCompra / cantidadDeCompra) * cantidadUtilizada;
            }

            tablaIngredientes.setValueAt(String.format("$%.2f", costoReal), fila, 6);

        } catch (Exception e) {
            tablaIngredientes.setValueAt("-", fila, 6);
            System.err.println("Error inesperado al actualizar costo real en fila " + fila + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void actualizarCostoTotal() {
        double total = 0.0;
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

        for (int i = 0; i < tablaIngredientes.getRowCount(); i++) {
            Object objetoCostoReal = tablaIngredientes.getValueAt(i, 6);
            total += parsearNumero(objetoCostoReal.toString());
        }
        campoCostoTotal.setText(formatoMoneda.format(total));
        actualizarPrecioFinal();
    }

    private void actualizarPrecioFinal() {
        double costoTotal = 0.0;
        double gastosExtra = 0.0;

        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

        costoTotal = parsearNumero(campoCostoTotal.getText());
        gastosExtra = parsearNumero(campoGastosExtra.getText());

        double precioFinal = costoTotal + gastosExtra;
        campoPrecioFinal.setText(formatoMoneda.format(precioFinal));
    }

    private void imprimirPlanilla() {
        PDDocument documento = new PDDocument();
        PDPage pagina = new PDPage(PDRectangle.A4);
        documento.addPage(pagina);

        try (PDPageContentStream contenidoStream = new PDPageContentStream(documento, pagina)) {
            contenidoStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contenidoStream.beginText();
            contenidoStream.newLineAtOffset(50, 750);
            contenidoStream.showText("Planilla de Costos de Receta");
            contenidoStream.endText();

            contenidoStream.setFont(PDType1Font.HELVETICA, 10);
            float margen = 50;
            float inicioY = 700;
            float anchoTabla = pagina.getMediaBox().getWidth() - 2 * margen;
            float altoFila = 20;

            String[] encabezados = {"PRODUCTO", "TIPO COMPRA", "CANT. COMPRA", "COSTO COMPRA", "CANT. UTIL.", "UNIDAD UTIL.", "COSTO REAL"};
            float[] anchosColumnas = {0.20f, 0.12f, 0.12f, 0.15f, 0.12f, 0.14f, 0.15f};

            float Yactual = inicioY;

            contenidoStream.setLeading(altoFila);
            Yactual -= altoFila;
            float inicioX = margen;
            for (int i = 0; i < encabezados.length; i++) {
                contenidoStream.beginText();
                contenidoStream.newLineAtOffset(inicioX + (anchoTabla * anchosColumnas[i] / 2) - (PDType1Font.HELVETICA_BOLD.getStringWidth(encabezados[i]) / 1000 * 10 / 2), inicioY);
                contenidoStream.showText(encabezados[i]);
                contenidoStream.endText();
                inicioX += anchoTabla * anchosColumnas[i];
            }

            contenidoStream.setLineWidth(1);
            contenidoStream.moveTo(margen, inicioY - 5);
            contenidoStream.lineTo(margen + anchoTabla, inicioY - 5);
            contenidoStream.stroke();


            for (int i = 0; i < tablaIngredientes.getRowCount(); i++) {
                Object objetoNombreProducto = tablaIngredientes.getValueAt(i, 0);
                if (objetoNombreProducto == null || objetoNombreProducto.toString().isEmpty()) {
                    continue;
                }
                String nombreProducto = objetoNombreProducto.toString();

                inicioX = margen;
                Yactual -= altoFila;

                for (int j = 0; j < tablaIngredientes.getColumnCount(); j++) {
                    Object valor = tablaIngredientes.getValueAt(i, j);
                    String texto = "";
                    if (valor instanceof Ingrediente) {
                        texto = ((Ingrediente) valor).getNombre();
                    } else {
                        texto = (valor != null) ? valor.toString() : "";
                    }

                    contenidoStream.beginText();
                    float anchoTexto = PDType1Font.HELVETICA.getStringWidth(texto) / 1000 * 10;
                    float desplazamiento = inicioX + (anchoTabla * anchosColumnas[j] / 2) - (anchoTexto / 2);
                    contenidoStream.newLineAtOffset(desplazamiento, Yactual);
                    contenidoStream.showText(texto);
                    contenidoStream.endText();
                    inicioX += anchoTabla * anchosColumnas[j];
                }
                contenidoStream.moveTo(margen, Yactual - 5);
                contenidoStream.lineTo(margen + anchoTabla, Yactual - 5);
                contenidoStream.stroke();
            }

            contenidoStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            Yactual -= 30;
            contenidoStream.beginText();
            contenidoStream.newLineAtOffset(margen, Yactual);
            contenidoStream.showText("COSTO TOTAL DE INGREDIENTES: " + campoCostoTotal.getText());
            contenidoStream.newLineAtOffset(0, -altoFila);
            contenidoStream.showText("GASTOS EXTRA: " + campoGastosExtra.getText());
            contenidoStream.newLineAtOffset(0, -altoFila);
            contenidoStream.showText("PRECIO FINAL ESTIMADO: " + campoPrecioFinal.getText());
            contenidoStream.endText();


        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al generar el PDF: " + ex.getMessage(), "Error de PDF", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        JFileChooser selectorArchivos = new JFileChooser();
        selectorArchivos.setDialogTitle("Guardar Planilla de Costos");
        int seleccionUsuario = selectorArchivos.showSaveDialog(this);

        if (seleccionUsuario == JFileChooser.APPROVE_OPTION) {
            File archivoAGuardar = selectorArchivos.getSelectedFile();
            if (!archivoAGuardar.getName().toLowerCase().endsWith(".pdf")) {
                archivoAGuardar = new File(archivoAGuardar.getAbsolutePath() + ".pdf");
            }
            try {
                documento.save(archivoAGuardar);
                documento.close();
                JOptionPane.showMessageDialog(this, "Planilla guardada en: " + archivoAGuardar.getAbsolutePath());
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(archivoAGuardar);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar el archivo: " + ex.getMessage(), "Error de Guardado", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        } else {
            try {
                documento.close();
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

        double costoTotalCalculado = parsearNumero(campoCostoTotal.getText());
        nuevaReceta.setCostoTotal(costoTotalCalculado);

        if (categoriasSeleccionadas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione al menos una categoría para la receta.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        nuevaReceta.setCategorias(categoriasSeleccionadas);


        List<RecetaIngrediente> ingredientesReceta = new ArrayList<>();
        for (int i = 0; i < tablaIngredientes.getRowCount(); i++) {
            Object objetoProducto = tablaIngredientes.getValueAt(i, 0);
            Object objetoCantidadUtilizada = tablaIngredientes.getValueAt(i, 4);
            Object objetoUnidadUtilizada = tablaIngredientes.getValueAt(i, 5);

            if (objetoProducto != null && objetoProducto instanceof Ingrediente &&
                objetoCantidadUtilizada != null && !objetoCantidadUtilizada.toString().isEmpty()) {

                Ingrediente ingrediente = (Ingrediente) objetoProducto;
                try {
                    double cantidadUtilizada = parsearNumero(objetoCantidadUtilizada.toString());
                    String unidadUtilizada = (objetoUnidadUtilizada != null) ? objetoUnidadUtilizada.toString() : "";

                    double costoDeCompra = ingrediente.getCostoDeCompra();
                    double cantidadDeCompra = ingrediente.getCantidadDeCompra();
                    double costoRealParaEsteIngrediente = 0.0;
                    if (cantidadDeCompra > 0) {
                        costoRealParaEsteIngrediente = (costoDeCompra / cantidadDeCompra) * cantidadUtilizada;
                    }

                    RecetaIngrediente ri = new RecetaIngrediente(nuevaReceta, ingrediente, cantidadUtilizada);
                    ri.setCostoTotal(costoRealParaEsteIngrediente);
                    ri.setUnidadUtilizada(unidadUtilizada);

                    ingredientesReceta.add(ri);

                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Cantidad utilizada inválida para el ingrediente '" + ingrediente.getNombre() + "'.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        if (ingredientesReceta.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La receta no contiene ingredientes. No se guardará.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        nuevaReceta.setRecetaIngredientes(ingredientesReceta);
        nuevaReceta.recalcularCostoTotal();

        Session sesion = null;
        Transaction transaccion = null;
        try {
            sesion = sessionFactory.openSession();
            transaccion = sesion.beginTransaction();
            sesion.save(nuevaReceta);
            transaccion.commit();
            JOptionPane.showMessageDialog(this, "Receta '" + nombreReceta + "' guardada con éxito.");
            categoriasSeleccionadas.clear();
        } catch (Exception e) {
            if (transaccion != null) {
                transaccion.rollback();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al guardar la receta: " + e.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (sesion != null) {
                sesion.close();
            }
        }
    }

    private void eliminarIngredienteDeTabla() {
        int filaSeleccionada = tablaIngredientes.getSelectedRow();
        if (filaSeleccionada >= 0) {
            DefaultTableModel modelo = (DefaultTableModel) tablaIngredientes.getModel();
            modelo.removeRow(filaSeleccionada);
            actualizarCostoTotal();
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione una fila para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void agregarFilaATabla() {
        DefaultTableModel modelo = (DefaultTableModel) tablaIngredientes.getModel();
        modelo.addRow(new Object[]{null, "", 0.0, 0.0, 0.0, "", String.format("$%.2f", 0.0)});
    }
}