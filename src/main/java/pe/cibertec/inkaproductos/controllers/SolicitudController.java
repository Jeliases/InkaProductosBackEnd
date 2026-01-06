package pe.cibertec.inkaproductos.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.cibertec.inkaproductos.dto.TransaccionDTO;
import pe.cibertec.inkaproductos.models.SolicitudCompra;
import pe.cibertec.inkaproductos.services.SolicitudService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;


    @GetMapping("/mias")
    public List<SolicitudCompra> misSolicitudes(@RequestParam String email) {
        return solicitudService.listarPorUsuario(email);
    }

    @GetMapping("/pendientes")
    public List<SolicitudCompra> pendientes() {
        return solicitudService.listarPendientes();
    }

    @PostMapping
    public ResponseEntity<?> crearSolicitud(@RequestBody TransaccionDTO dto) {
        try {
            dto.setEsAdmin(false);
            SolicitudCompra solicitud = solicitudService.crearSolicitud(dto);

            return ResponseEntity.ok(solicitud);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

}
