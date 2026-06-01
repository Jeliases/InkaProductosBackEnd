package pe.cibertec.inkaproductos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.cibertec.inkaproductos.models.Rol;

public interface RolRepository extends JpaRepository<Rol, Long> {
}