package pe.cibertec.inkaproductos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.cibertec.inkaproductos.models.Usuario;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Para autenticación con BCrypt
    Optional<Usuario> findByEmail(String email);

    // Verifica si el correo existe antes de registrar
    boolean existsByEmail(String email);


}