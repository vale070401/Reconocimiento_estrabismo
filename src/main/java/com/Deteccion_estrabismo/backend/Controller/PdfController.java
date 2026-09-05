package com.Deteccion_estrabismo.backend.Controller;

import com.Deteccion_estrabismo.backend.Service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfService pdfService;

    @GetMapping("/historia-clinica/{documentoIdentidad}")
    public ResponseEntity<Resource> descargarHistoriaClinica(@PathVariable Integer documentoIdentidad) {
        return pdfService.generarHistoriaClinicaPdf(documentoIdentidad);
    }
}