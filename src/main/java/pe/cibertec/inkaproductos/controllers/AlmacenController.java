package pe.cibertec.inkaproductos.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.cibertec.inkaproductos.models.Almacen;
import pe.cibertec.inkaproductos.repositories.AlmacenRepository;

import java.util.List;

@RestController
@RequestMapping("/api/almacenes")
@RequiredArgsConstructor
public class AlmacenController {

    private final AlmacenRepository almacenRepo;

    @GetMapping
    public List<Almacen> listar() {
        return almacenRepo.findByActivoTrue();
    }
}
