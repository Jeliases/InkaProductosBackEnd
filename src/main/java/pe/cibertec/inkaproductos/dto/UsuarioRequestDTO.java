package pe.cibertec.inkaproductos.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UsuarioRequestDTO {
    private String nombre;
    private String email;
    private String password; // Solo para creación o actualización de password
    private boolean enabled;
    private Set<Long> rolesIds; // IDs de los roles a asignar
}