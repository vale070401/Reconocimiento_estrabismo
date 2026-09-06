package com.Deteccion_estrabismo.backend.Service;

import com.Deteccion_estrabismo.backend.Entities.Evaluacion;
import com.Deteccion_estrabismo.backend.Entities.Pacientes;
import com.Deteccion_estrabismo.backend.Repository.EvaluacionRepository;
import com.Deteccion_estrabismo.backend.Repository.PacientesRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final PacientesRepository pacienteRepository;
    private final EvaluacionRepository evaluacionRepository;

    public ResponseEntity<Resource> generarHistoriaClinicaPdf(Integer documentoIdentidad) {
        try {
            // Buscar paciente y evaluaciones
            Pacientes paciente = pacienteRepository.findByDocumentoIdentidad(documentoIdentidad)
                    .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

            List<Evaluacion> evaluaciones = evaluacionRepository.findByPacienteId(paciente.getId());

            // Crear PDF en memoria
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Agregar contenido al PDF
            agregarCabecera(document, paciente);
            agregarInformacionPaciente(document, paciente);
            agregarEvaluaciones(document, evaluaciones);
            agregarPiePagina(document);

            document.close();

            // Convertir a Resource para descarga
            byte[] pdfBytes = outputStream.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);

            // Configurar headers para descarga
            String nombreArchivo = "historia_clinica_" + paciente.getDocumentoIdentidad() + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(resource);

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF: " + e.getMessage());
        }
    }

    private void agregarCabecera(Document document, Pacientes paciente) {
        // Título
        Paragraph titulo = new Paragraph("HISTORIA CLÍNICA - DETECCIÓN DE ESTRABISMO")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.BLUE);
        document.add(titulo);

        // Línea separadora
        document.add(new LineSeparator(new SolidLine()).setMarginTop(10).setMarginBottom(10));

        // Fecha de generación
        Paragraph fecha = new Paragraph(
                "Generado el: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setItalic();
        document.add(fecha);

        document.add(new Paragraph("\n"));
    }

    private void agregarInformacionPaciente(Document document, Pacientes paciente) {
        // Subtítulo
        Paragraph subtitulo = new Paragraph("INFORMACIÓN DEL PACIENTE")
                .setFontSize(14)
                .setBold()
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(5);
        document.add(subtitulo);

        // Tabla de información del paciente
        float[] columnWidths = { 1, 3 };
        Table tablaPaciente = new Table(UnitValue.createPercentArray(columnWidths));
        tablaPaciente.setWidth(UnitValue.createPercentValue(100));

        agregarFilaTabla(tablaPaciente, "Documento:", paciente.getDocumentoIdentidad().toString());
        agregarFilaTabla(tablaPaciente, "Nombres:", paciente.getNombres());
        agregarFilaTabla(tablaPaciente, "Apellidos:", paciente.getApellidos());

        // Tipo de documento
        if (paciente.getTipoDocumento() != null) {
            agregarFilaTabla(tablaPaciente, "Tipo Documento:", paciente.getTipoDocumento().toString());
        }

        // Fecha de nacimiento y edad calculada
        if (paciente.getFechaNacimiento() != null) {
            LocalDate fechaNacimiento = paciente.getFechaNacimiento().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            agregarFilaTabla(tablaPaciente, "Fecha Nacimiento:",
                    fechaNacimiento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            // Calcular edad
            int edad = calcularEdad(fechaNacimiento);
            agregarFilaTabla(tablaPaciente, "Edad:", edad + " años");
        }

        // Género
        if (paciente.getGenero() != null) {
            agregarFilaTabla(tablaPaciente, "Género:", paciente.getGenero());
        }

        // Responsable
        if (paciente.getResponsable() != null) {
            agregarFilaTabla(tablaPaciente, "Responsable:",
                    paciente.getResponsable().getNombres() + " " + paciente.getResponsable().getApellidos());
        }

        document.add(tablaPaciente);
        document.add(new Paragraph("\n"));
    }

    private void agregarEvaluaciones(Document document, List<Evaluacion> evaluaciones) {
        Paragraph subtitulo = new Paragraph("EVALUACIONES DE ESTRABISMO")
                .setFontSize(14)
                .setBold()
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(5);
        document.add(subtitulo);

        if (evaluaciones.isEmpty()) {
            document.add(new Paragraph("No se encontraron evaluaciones registradas.")
                    .setItalic()
                    .setFontColor(ColorConstants.GRAY));
            return;
        }

        // Tabla de evaluaciones (SIN observaciones)
        float[] columnWidths = { 1, 2, 1, 2 }; // 4 columnas en lugar de 5
        Table tablaEvaluaciones = new Table(UnitValue.createPercentArray(columnWidths));
        tablaEvaluaciones.setWidth(UnitValue.createPercentValue(100));

        // Encabezados de la tabla (SIN observaciones)
        tablaEvaluaciones.addHeaderCell(crearCeldaEncabezado("#"));
        tablaEvaluaciones.addHeaderCell(crearCeldaEncabezado("Fecha"));
        tablaEvaluaciones.addHeaderCell(crearCeldaEncabezado("Resultado"));
        tablaEvaluaciones.addHeaderCell(crearCeldaEncabezado("Confianza"));

        // Datos de las evaluaciones (SIN observaciones)
        int contador = 1;
        for (Evaluacion evaluacion : evaluaciones) {
            tablaEvaluaciones.addCell(crearCeldaNormal(String.valueOf(contador++)));
            tablaEvaluaciones.addCell(crearCeldaNormal(evaluacion.getFechaEvaluacion().toString()));

            // Celda de resultado con color
            Cell celdaResultado = crearCeldaNormal(evaluacion.isResultado() ? "ESTRABISMO" : "NORMAL");
            if (evaluacion.isResultado()) {
                celdaResultado.setBackgroundColor(ColorConstants.RED).setFontColor(ColorConstants.WHITE);
            } else {
                celdaResultado.setBackgroundColor(ColorConstants.GREEN).setFontColor(ColorConstants.WHITE);
            }
            tablaEvaluaciones.addCell(celdaResultado);

            tablaEvaluaciones
                    .addCell(crearCeldaNormal(String.format("%.2f%%", evaluacion.getConfianzaPrediccion() * 100)));
        }

        document.add(tablaEvaluaciones);

        // Estadísticas
        agregarEstadisticas(document, evaluaciones);
    }

    private void agregarEstadisticas(Document document, List<Evaluacion> evaluaciones) {
        long totalEvaluaciones = evaluaciones.size();
        long conEstrabismo = evaluaciones.stream().filter(Evaluacion::isResultado).count();
        long sinEstrabismo = totalEvaluaciones - conEstrabismo;
        double porcentajeEstrabismo = totalEvaluaciones > 0 ? (double) conEstrabismo / totalEvaluaciones * 100 : 0;

        Paragraph estadisticas = new Paragraph("\nESTADÍSTICAS:")
                .setBold()
                .setFontSize(12);
        document.add(estadisticas);

        // ✅ CORREGIDO: Usar com.itextpdf.layout.element.List
        com.itextpdf.layout.element.List estadisticasList = new com.itextpdf.layout.element.List()
                .setSymbolIndent(12)
                .setListSymbol("\u2022"); // Bullet point

        estadisticasList.add(new ListItem("Total de evaluaciones: " + totalEvaluaciones));
        estadisticasList.add(new ListItem("Evaluaciones con estrabismo: " + conEstrabismo));
        estadisticasList.add(new ListItem("Evaluaciones normales: " + sinEstrabismo));
        estadisticasList.add(new ListItem("Porcentaje de detección: " + String.format("%.1f%%", porcentajeEstrabismo)));

        document.add(estadisticasList);
    }

    private void agregarPiePagina(Document document) {
        document.add(new Paragraph("\n\n"));

        Paragraph piePagina = new Paragraph(
                "Este documento fue generado automáticamente por el Sistema de Detección de Estrabismo")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic()
                .setFontColor(ColorConstants.GRAY);
        document.add(piePagina);
    }

    // Método para calcular edad desde la fecha de nacimiento
    private int calcularEdad(LocalDate fechaNacimiento) {
        LocalDate ahora = LocalDate.now();
        return (int) TimeUnit.DAYS.toDays(ahora.toEpochDay() - fechaNacimiento.toEpochDay()) / 365;
    }

    // Métodos auxiliares para crear celdas
    private Cell crearCeldaEncabezado(String texto) {
        return new Cell().add(new Paragraph(texto))
                .setBold()
                .setBackgroundColor(ColorConstants.DARK_GRAY)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private Cell crearCeldaNormal(String texto) {
        return new Cell().add(new Paragraph(texto != null ? texto : ""))
                .setPadding(5)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private void agregarFilaTabla(Table tabla, String clave, String valor) {
        tabla.addCell(crearCeldaNormal(clave).setBold());
        tabla.addCell(crearCeldaNormal(valor));
    }
}