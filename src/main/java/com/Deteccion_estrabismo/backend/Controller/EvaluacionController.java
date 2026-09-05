package com.Deteccion_estrabismo.backend.Controller;

import com.Deteccion_estrabismo.backend.Dto.EvaluacionRequestDto;
import com.Deteccion_estrabismo.backend.Dto.EvaluacionResponseDto;
import com.Deteccion_estrabismo.backend.Service.EvaluacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluaciones")
@RequiredArgsConstructor
public class EvaluacionController {

    private final EvaluacionService evaluacionService;

    @PostMapping
    public ResponseEntity<?> crearEvaluacion(@RequestBody EvaluacionRequestDto request) {
        try {
            System.out.println("📥 Recibiendo evaluación para documento: " + request.getDocumentoIdentidad());

            EvaluacionResponseDto evaluacion = evaluacionService.crearEvaluacion(request);

            System.out.println("✅ Evaluación guardada exitosamente para documento: " + request.getDocumentoIdentidad());
            return ResponseEntity.ok(evaluacion);

        } catch (RuntimeException e) {
            System.out.println("❌ Error creando evaluación: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("💥 Error interno: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Error interno del servidor");
        }
    }

    @GetMapping("/documento/{documentoIdentidad}")
    public ResponseEntity<List<EvaluacionResponseDto>> obtenerEvaluacionesPorDocumento(
            @PathVariable Integer documentoIdentidad) {
        List<EvaluacionResponseDto> evaluaciones = evaluacionService
                .obtenerEvaluacionesPorDocumento(documentoIdentidad);
        return ResponseEntity.ok(evaluaciones);
    }

    @GetMapping
    public ResponseEntity<List<EvaluacionResponseDto>> obtenerTodasLasEvaluaciones() {
        List<EvaluacionResponseDto> evaluaciones = evaluacionService.obtenerTodasLasEvaluaciones();
        return ResponseEntity.ok(evaluaciones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvaluacionResponseDto> obtenerEvaluacionPorId(@PathVariable Long id) {
        try {
            EvaluacionResponseDto evaluacion = evaluacionService.obtenerEvaluacionPorId(id);
            return ResponseEntity.ok(evaluacion);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/resultado/{resultado}")
    public ResponseEntity<List<EvaluacionResponseDto>> obtenerEvaluacionesPorResultado(
            @PathVariable boolean resultado) {
        List<EvaluacionResponseDto> evaluaciones = evaluacionService.obtenerEvaluacionesPorResultado(resultado);
        return ResponseEntity.ok(evaluaciones);
    }
}
