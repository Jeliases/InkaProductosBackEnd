package pe.cibertec.inkaproductos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.cibertec.inkaproductos.models.MovimientoStock;

import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
    List<MovimientoStock> findByProducto_ProductoIdOrderByFechaAsc(Integer productoId);

    List<MovimientoStock> findByOrigen_AlmacenIdOrDestino_AlmacenIdOrderByFechaDesc(
            Integer origenId, Integer destinoId);
}