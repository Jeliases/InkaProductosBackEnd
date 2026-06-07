package pe.cibertec.inkaproductos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.cibertec.inkaproductos.models.UnidadMedida;

public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, Integer> {
}