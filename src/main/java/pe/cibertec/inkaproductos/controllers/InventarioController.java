package pe.cibertec.inkaproductos.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.cibertec.inkaproductos.models.Inventario;
import pe.cibertec.inkaproductos.services.InventarioService;
import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping("/almacen/{idAlmacen}")
    public List<Inventario> listarPorAlmacen(@PathVariable Integer idAlmacen) {
        return inventarioService.listarPorAlmacen(idAlmacen);
    }

    @GetMapping("/filtrar")
    public List<Inventario> filtrar(
            @RequestParam Integer almacenId,
            @RequestParam(required = false) Integer categoriaId
    ) {
        return inventarioService.filtrar(almacenId, categoriaId);
    }
}