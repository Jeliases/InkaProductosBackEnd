package pe.cibertec.inkaproductos.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.cibertec.inkaproductos.dto.KardexDTO;
import pe.cibertec.inkaproductos.dto.TrasladoRequest;
import pe.cibertec.inkaproductos.services.TrasladoService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/traslados")
@RequiredArgsConstructor
public class TrasladoController {

    private final TrasladoService trasladoService;

    @PostMapping
    public ResponseEntity<?> procesar(@RequestBody TrasladoRequest req,
                                      Authentication auth) {
        try {
            trasladoService.procesar(req, auth.getName());
            return ResponseEntity.ok(Map.of("mensaje", "Traslado realizado con éxito"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/kardex/producto/{id}")
    public List<KardexDTO> kardexPorProducto(@PathVariable Integer id) {
        return trasladoService.kardexPorProducto(id);
    }

    @GetMapping("/kardex/almacen/{id}")
    public List<KardexDTO> kardexPorAlmacen(@PathVariable Integer id) {
        return trasladoService.kardexPorAlmacen(id);
    }
}
