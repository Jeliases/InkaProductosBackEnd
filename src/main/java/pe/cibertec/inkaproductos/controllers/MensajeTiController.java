package pe.cibertec.inkaproductos.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.cibertec.inkaproductos.models.MensajeTi;
import pe.cibertec.inkaproductos.services.MensajeTiService;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class MensajeTiController {

    private final MensajeTiService mensajeTiService;

    @PostMapping
    public ResponseEntity<MensajeTi> enviar(@RequestBody MensajeTi mensaje) {
        return ResponseEntity.ok(mensajeTiService.enviarSolicitud(mensaje));
    }

    @GetMapping("/pendientes")
    public List<MensajeTi> listarPendientes() {
        return mensajeTiService.obtenerPendientes();
    }

    @PutMapping("/{id}/atender")
    public ResponseEntity<MensajeTi> atender(@PathVariable Integer id) {
        return ResponseEntity.ok(mensajeTiService.atenderTicket(id));
    }
}