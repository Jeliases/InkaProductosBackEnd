package pe.cibertec.inkaproductos.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.cibertec.inkaproductos.dto.SolicitudCompraDTO;
import pe.cibertec.inkaproductos.dto.SolicitudRequest;
import pe.cibertec.inkaproductos.services.SolicitudService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody SolicitudRequest req,
                                   Authentication auth) {
        try {
            return ResponseEntity.ok(solicitudService.crear(req, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<SolicitudCompraDTO>> pendientes() {
        return ResponseEntity.ok(solicitudService.pendientes());
    }

    @GetMapping("/mis")
    public ResponseEntity<List<SolicitudCompraDTO>> misSolicitudes(Authentication auth) {
        return ResponseEntity.ok(solicitudService.misSolicitudes(auth.getName()));
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobar(@PathVariable Integer id, Authentication auth) {
        try {
            solicitudService.aprobar(id, auth.getName());
            return ResponseEntity.ok(Map.of("mensaje", "Solicitud aprobada y traslado ejecutado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazar(@PathVariable Integer id) {
        try {
            solicitudService.rechazar(id);
            return ResponseEntity.ok(Map.of("mensaje", "Solicitud rechazada"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}