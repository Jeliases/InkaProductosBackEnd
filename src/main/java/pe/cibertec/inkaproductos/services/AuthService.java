package pe.cibertec.inkaproductos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pe.cibertec.inkaproductos.dto.AuthResponse;
import pe.cibertec.inkaproductos.dto.LoginRequest;
import pe.cibertec.inkaproductos.models.Usuario;
import pe.cibertec.inkaproductos.repositories.UsuarioRepository;
import pe.cibertec.inkaproductos.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse login(LoginRequest req) {
        Usuario u = usuarioRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!u.isEnabled())
            throw new RuntimeException("Usuario deshabilitado");

        if (!passwordEncoder.matches(req.getPassword(), u.getPasswordHash()))
            throw new RuntimeException("Contraseña incorrecta");

        String rol = u.getRoles().stream()
                .findFirst()
                .map(r -> r.getNombre())
                .orElse("USER");

        String token = jwtUtil.generate(u.getEmail(), rol);
        return new AuthResponse(token, u.getEmail(), rol, u.getNombre());
    }
}
