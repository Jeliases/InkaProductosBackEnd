package pe.cibertec.inkaproductos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.cibertec.inkaproductos.models.Usuario;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private Long usuarioId;
    private String nombre;
    private String email;
    private boolean enabled;
    private Set<String> roles;

    public static UsuarioDTO from(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getUsuarioId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.isEnabled(),
                usuario.getRoles().stream()
                        .map(rol -> rol.getNombre())
                        .collect(Collectors.toSet())
        );
    }
}