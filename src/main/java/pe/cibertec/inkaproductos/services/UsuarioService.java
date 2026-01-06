package pe.cibertec.inkaproductos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pe.cibertec.inkaproductos.models.Rol;
import pe.cibertec.inkaproductos.models.Usuario;
import pe.cibertec.inkaproductos.repositories.RolRepository;
import pe.cibertec.inkaproductos.repositories.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    // LOGIN
    public Usuario login(String email, String password) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario != null && passwordEncoder.matches(password, usuario.getPassword())) {
            return usuario;
        }
        return null;
    }

    // LISTAR
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    // CREAR USUARIO
    public Usuario crear(Usuario nuevo, String rolNombre) {

        nuevo.setUsuarioId(null);
        nuevo.setPassword(passwordEncoder.encode(nuevo.getPassword()));
        nuevo.setEnabled(1);

        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rolNombre));

        // LISTA MUTABLE
        nuevo.setRoles(new ArrayList<>(List.of(rol)));

        return usuarioRepository.save(nuevo);
    }

    // EDITAR
    public Usuario editar(Long id, Usuario datos) {

        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no existe"));

        u.setNombre(datos.getNombre());
        u.setEmail(datos.getEmail());

        return usuarioRepository.save(u);
    }

    // CAMBIAR ROL
    public Usuario cambiarRol(Long id, String rolNuevo) {

        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no existe"));

        Rol rol = rolRepository.findByNombre(rolNuevo)
                .orElseThrow(() -> new RuntimeException("Rol no existe"));

        // Crear lista si viene null
        if (u.getRoles() == null) {
            u.setRoles(new ArrayList<>());
        }

        // Evitar duplicados
        boolean yaTiene = u.getRoles().stream()
                .anyMatch(r -> r.getNombre().equalsIgnoreCase(rolNuevo));

        if (!yaTiene) {
            u.getRoles().add(rol);
        }

        return usuarioRepository.save(u);
    }


    // ELIMINAR
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no existe");
        }
        usuarioRepository.deleteById(id);
    }
}
