package pe.cibertec.inkaproductos.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.cibertec.inkaproductos.dto.ProductoDTO;
import pe.cibertec.inkaproductos.dto.ProductoRequestDTO;
import pe.cibertec.inkaproductos.services.ProductoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public List<ProductoDTO> listar(
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Integer almacenId,
            @RequestParam(required = false) Boolean activo) {
        return productoService.listar(categoriaId, almacenId, activo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> getProductoById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(productoService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> crearProducto(@RequestBody ProductoRequestDTO request, Authentication auth) {
        try {
            ProductoDTO nuevoProducto = productoService.crear(request, auth.getName());
            return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
        } catch (Exception e) {
            // Captura cualquier error de negocio (SKU duplicado, almacén no existe) y lo devuelve como 400
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Integer id, @RequestBody ProductoRequestDTO request, Authentication auth) {
        try {
            ProductoDTO productoActualizado = productoService.actualizar(id, request, auth.getName());
            return ResponseEntity.ok(productoActualizado);
        } catch (Exception e) {
            // Captura cualquier error de negocio (SKU no modificable, etc.) y lo devuelve como 400
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivarProducto(@PathVariable Integer id, Authentication auth) {
        try {
            productoService.desactivar(id, auth.getName());
            return ResponseEntity.ok(Map.of("mensaje", "Producto desactivado correctamente."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}