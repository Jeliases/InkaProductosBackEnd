package pe.cibertec.inkaproductos.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import pe.cibertec.inkaproductos.models.Rol;
import pe.cibertec.inkaproductos.models.Usuario;
import pe.cibertec.inkaproductos.repositories.UsuarioRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Buscamos el usuario por su correo en MySQL
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("El correo no existe en el sistema: " + email));

        // 2. Extraemos los roles de la base de datos
        String[] roles = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .toArray(String[]::new);

        // 3. Le pasamos el usuario a Spring Security para que verifique la contraseña
        return org.springframework.security.core.userdetails.User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword()) // Usamos tu método exacto getPassword()
                .roles(roles)
                .build();
    }
}