package com.olsoftware.comerciantes.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.olsoftware.comerciantes.dto.ComercianteDTO;
import com.olsoftware.comerciantes.dto.ComercianteReporteDTO;
import com.olsoftware.comerciantes.dto.EstablecimientoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteService {

    private final ComercianteService comercianteService;

    // Formato de moneda para Colombia
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.of("es", "CO"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * RETO 10: Generar reporte en formato plano (.csv) separado por pipe (|)
     */
    public byte[] generarReporteComerciantesCsv() {
        List<ComercianteReporteDTO> lista = comercianteService.obtenerReporteComerciantesActivos();

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        // Encabezados separados por pipe
        pw.println("Nombre o Razón Social|Departamento|Municipio|Teléfono|Correo Electrónico|Fecha de Registro|Estado|Cantidad de Establecimientos|Total Activos|Cantidad de Empleados");

        for (ComercianteReporteDTO c : lista) {
            String telefono = c.getTelefono() != null ? c.getTelefono() : "";
            String correo = c.getCorreoElectronico() != null ? c.getCorreoElectronico() : "";
            String fecha = c.getFechaRegistro() != null ? c.getFechaRegistro().toString() : "";
            String totalActivos = c.getTotalActivos() != null ? c.getTotalActivos().toString() : "0.00";

            pw.println(String.join("|",
                    c.getNombreRazonSocial(),
                    c.getDepartamento(),
                    c.getMunicipio(),
                    telefono,
                    correo,
                    fecha,
                    c.getEstado(),
                    String.valueOf(c.getCantidadEstablecimientos()),
                    totalActivos,
                    String.valueOf(c.getCantidadEmpleados())
            ));
        }

        pw.flush();
        return sw.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * RETO 11: Generar Ficha del Comerciante en formato PDF
     */
    public byte[] generarFichaComerciantePdf(Long idComerciante) {
        ComercianteDTO c = comercianteService.obtenerPorId(idComerciante);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(document, out);
            document.open();

            // Estilos de Fuentes y Colores
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(17, 24, 39));
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(107, 114, 128));
            Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(30, 41, 59));
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(55, 65, 81));
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(17, 24, 39));
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font tableCellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(31, 41, 55));

            // Encabezado
            Paragraph title = new Paragraph("FICHA TÉCNICA DEL COMERCIANTE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("Agremiación Nacional de Comercio - Sistema de Información Mercantil", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // Tabla Datos Generales del Comerciante
            Paragraph sec1 = new Paragraph("1. Datos Generales del Comerciante", sectionTitleFont);
            sec1.setSpacingAfter(8);
            document.add(sec1);

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(15);
            infoTable.setWidths(new float[]{35f, 65f});

            agregarFilaInfo(infoTable, "Nombre o Razón Social:", c.getNombreRazonSocial(), labelFont, valueFont);
            agregarFilaInfo(infoTable, "Departamento:", c.getDepartamento(), labelFont, valueFont);
            agregarFilaInfo(infoTable, "Municipio / Ciudad:", c.getMunicipio(), labelFont, valueFont);
            agregarFilaInfo(infoTable, "Teléfono de Contacto:", c.getTelefono() != null ? c.getTelefono() : "No registrado", labelFont, valueFont);
            agregarFilaInfo(infoTable, "Correo Electrónico:", c.getCorreoElectronico() != null ? c.getCorreoElectronico() : "No registrado", labelFont, valueFont);
            agregarFilaInfo(infoTable, "Fecha de Registro Mercantil:", c.getFechaRegistro() != null ? c.getFechaRegistro().format(dateFormatter) : "N/A", labelFont, valueFont);
            agregarFilaInfo(infoTable, "Estado Actual:", c.getEstado(), labelFont, valueFont);
            document.add(infoTable);

            // Resumen Financiero y Operativo
            Paragraph sec2 = new Paragraph("2. Consolidado Financiero y Operativo", sectionTitleFont);
            sec2.setSpacingAfter(8);
            document.add(sec2);

            PdfPTable summaryTable = new PdfPTable(3);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingAfter(20);

            agregarTarjetaMetrica(summaryTable, "Total Establecimientos", String.valueOf(c.getCantidadEstablecimientos()), new Color(243, 244, 246));
            agregarTarjetaMetrica(summaryTable, "Total Ingresos / Activos", currencyFormatter.format(c.getTotalActivos()), new Color(236, 253, 245));
            agregarTarjetaMetrica(summaryTable, "Cantidad Total Empleados", String.valueOf(c.getCantidadEmpleados()), new Color(239, 246, 255));
            document.add(summaryTable);

            // Sección Establecimientos Vinculados
            Paragraph sec3 = new Paragraph("3. Establecimientos de Comercio Asociados", sectionTitleFont);
            sec3.setSpacingAfter(8);
            document.add(sec3);

            if (c.getEstablecimientos() == null || c.getEstablecimientos().isEmpty()) {
                Paragraph noEst = new Paragraph("Este comerciante no registra establecimientos asociados.", valueFont);
                noEst.setSpacingAfter(15);
                document.add(noEst);
            } else {
                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{50f, 30f, 20f});
                table.setSpacingAfter(20);

                // Headers
                PdfPCell h1 = new PdfPCell(new Phrase("Nombre del Establecimiento", tableHeaderFont));
                h1.setBackgroundColor(new Color(31, 41, 55));
                h1.setPadding(6);
                table.addCell(h1);

                PdfPCell h2 = new PdfPCell(new Phrase("Ingresos Brutos", tableHeaderFont));
                h2.setBackgroundColor(new Color(31, 41, 55));
                h2.setPadding(6);
                h2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(h2);

                PdfPCell h3 = new PdfPCell(new Phrase("No. Empleados", tableHeaderFont));
                h3.setBackgroundColor(new Color(31, 41, 55));
                h3.setPadding(6);
                h3.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(h3);

                // Rows
                boolean alt = false;
                for (EstablecimientoDTO est : c.getEstablecimientos()) {
                    Color bgColor = alt ? new Color(249, 250, 251) : Color.WHITE;

                    PdfPCell c1Cell = new PdfPCell(new Phrase(est.getNombreEstablecimiento(), tableCellFont));
                    c1Cell.setBackgroundColor(bgColor);
                    c1Cell.setPadding(6);
                    table.addCell(c1Cell);

                    PdfPCell c2Cell = new PdfPCell(new Phrase(currencyFormatter.format(est.getIngresos()), tableCellFont));
                    c2Cell.setBackgroundColor(bgColor);
                    c2Cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c2Cell.setPadding(6);
                    table.addCell(c2Cell);

                    PdfPCell c3Cell = new PdfPCell(new Phrase(String.valueOf(est.getNumeroEmpleados()), tableCellFont));
                    c3Cell.setBackgroundColor(bgColor);
                    c3Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c3Cell.setPadding(6);
                    table.addCell(c3Cell);

                    alt = !alt;
                }

                document.add(table);
            }

            // Pie de página
            Paragraph footer = new Paragraph("Generado automáticamente por el Sistema de Información Mercantil | Confidencial", subtitleFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar PDF de comerciante ID {}: {}", idComerciante, e.getMessage());
            throw new RuntimeException("Error al generar archivo PDF del comerciante", e);
        }
    }

    private void agregarFilaInfo(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell lCell = new PdfPCell(new Phrase(label, labelFont));
        lCell.setPadding(5);
        lCell.setBorderColor(new Color(229, 231, 235));
        lCell.setBackgroundColor(new Color(249, 250, 251));

        PdfPCell vCell = new PdfPCell(new Phrase(value, valueFont));
        vCell.setPadding(5);
        vCell.setBorderColor(new Color(229, 231, 235));

        table.addCell(lCell);
        table.addCell(vCell);
    }

    private void agregarTarjetaMetrica(PdfPTable table, String titulo, String valor, Color bgColor) {
        Font tFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(75, 85, 99));
        Font vFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(17, 24, 39));

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bgColor);
        cell.setPadding(10);
        cell.setBorderColor(new Color(229, 231, 235));

        Paragraph p1 = new Paragraph(titulo, tFont);
        Paragraph p2 = new Paragraph(valor, vFont);
        p2.setSpacingBefore(4);

        cell.addElement(p1);
        cell.addElement(p2);
        table.addCell(cell);
    }
}
