package pe.cibertec.inkaproductos.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.cibertec.inkaproductos.models.Almacen;
import pe.cibertec.inkaproductos.services.AlmacenService;

import java.util.List;

@RestController
@RequestMapping("/api/almacenes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AlmacenController {

    private final AlmacenService almacenService;

    @GetMapping
    public List<Almacen> listar() {
        return almacenService.listarTodos();
    }
}