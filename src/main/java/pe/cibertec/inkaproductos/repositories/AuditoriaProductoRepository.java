package pe.cibertec.inkaproductos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.cibertec.inkaproductos.models.AuditoriaProducto;

public interface AuditoriaProductoRepository extends JpaRepository<AuditoriaProducto, Long> {
}