package pe.cibertec.inkaproductos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.cibertec.inkaproductos.models.MovimientoDetalle;
import java.util.List;

public interface MovimientoDetalleRepository extends JpaRepository<MovimientoDetalle, Integer> {

    List<MovimientoDetalle> findByMovimientoId(Integer movimientoId);

}
