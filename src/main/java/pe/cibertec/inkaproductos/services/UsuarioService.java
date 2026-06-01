package pe.cibertec.inkaproductos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cibertec.inkaproductos.dto.UsuarioDTO;
import pe.cibertec.inkaproductos.dto.UsuarioRequestDTO;
import pe.cibertec.inkaproductos.models.Rol;
import pe.cibertec.inkaproductos.models.Usuario;
import pe.cibertec.inkaproductos.repositories.RolRepository;
import pe.cibertec.inkaproductos.repositories.UsuarioRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioDTO> findAll() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioDTO findById(Long id) {
        return usuarioRepository.findById(id)
                .map(UsuarioDTO::from)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Transactional
    public UsuarioDTO create(UsuarioRequestDTO request) {
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está en uso");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setEnabled(request.isEnabled());

        Set<Rol> roles = rolRepository.findAllById(request.getRolesIds()).stream().collect(Collectors.toSet());
        if (roles.isEmpty()) {
            throw new RuntimeException("Debe especificar al menos un rol válido.");
        }
        usuario.setRoles(roles);

        return UsuarioDTO.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioDTO update(Long id, UsuarioRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setEnabled(request.isEnabled());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        Set<Rol> roles = rolRepository.findAllById(request.getRolesIds()).stream().collect(Collectors.toSet());
        if (roles.isEmpty()) {
            throw new RuntimeException("Debe especificar al menos un rol válido.");
        }
        usuario.setRoles(roles);

        return UsuarioDTO.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public void delete(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }
}