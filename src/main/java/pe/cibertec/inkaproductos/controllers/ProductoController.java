package pe.cibertec.inkaproductos.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.cibertec.inkaproductos.dto.ProductoDTO;
import pe.cibertec.inkaproductos.dto.TransaccionDTO;
import pe.cibertec.inkaproductos.models.Producto;
import pe.cibertec.inkaproductos.services.ProductoService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public List<Producto> listar() {
        return productoService.listarTodos();
    }

    // Este método solo debe ser llamado por el perfil TI según tu lógica
    @PostMapping
    public ResponseEntity<Producto> guardar(@RequestBody Producto producto) {
        return ResponseEntity.ok(productoService.guardar(producto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filtrar")
    public List<ProductoDTO> filtrarProductos(
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Integer almacenId
    ) {
        return productoService.filtrarProductos(categoriaId, almacenId);
    }

    @PostMapping("/transaccion")
    public ResponseEntity<?> procesarTransaccion(@RequestBody TransaccionDTO dto,
                                                 Principal principal) {
        try {
            dto.setEsAdmin(true);
            // El interceptor JWT carga el correo automáticamente aquí desde la cabecera
            dto.setUsuarioEmail(principal.getName());

            productoService.procesarTransaccion(dto);

            return ResponseEntity.ok(Map.of("mensaje", "Transacción registrada con éxito"));
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }



}

