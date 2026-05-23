package pe.cibertec.inkaproductos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.cibertec.inkaproductos.models.MovimientoStock;
import java.util.List;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

    @Query("""
        SELECT ms FROM MovimientoStock ms
        JOIN FETCH ms.producto p
        JOIN FETCH ms.origen
        JOIN FETCH ms.destino
        WHERE p.productoId = :productoId
        ORDER BY ms.fecha DESC
    """)
    List<MovimientoStock> kardexPorProducto(@Param("productoId") Integer productoId);

    @Query("""
        SELECT ms FROM MovimientoStock ms
        JOIN FETCH ms.producto p
        JOIN FETCH ms.origen o
        JOIN FETCH ms.destino d
        WHERE o.almacenId = :almacenId OR d.almacenId = :almacenId
        ORDER BY ms.fecha DESC
    """)
    List<MovimientoStock> kardexPorAlmacen(@Param("almacenId") Integer almacenId);
}
