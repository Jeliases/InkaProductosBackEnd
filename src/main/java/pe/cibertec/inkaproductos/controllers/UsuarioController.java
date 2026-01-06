package pe.cibertec.inkaproductos.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.cibertec.inkaproductos.models.Usuario;
import pe.cibertec.inkaproductos.services.UsuarioService;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasRole('TI')")
    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    @PostMapping("/{rol}")
    @PreAuthorize("hasRole('TI')")
    public Usuario crear(@RequestBody Usuario usuario,
                         @PathVariable String rol) {
        return usuarioService.crear(usuario, rol);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TI')")
    public Usuario editar(@PathVariable Long id, @RequestBody Usuario usuario) {
        return usuarioService.editar(id, usuario);
    }

    @PutMapping("/{id}/rol/{rol}")
    @PreAuthorize("hasRole('TI')")
    public Usuario cambiarRol(@PathVariable Long id, @PathVariable String rol) {
        return usuarioService.cambiarRol(id, rol);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TI')")
    public void eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
    }
}
