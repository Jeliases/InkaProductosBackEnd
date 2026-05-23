package pe.cibertec.inkaproductos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.cibertec.inkaproductos.models.Inventario;
import pe.cibertec.inkaproductos.models.InventarioId;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, InventarioId> {

    @Query("""
        SELECT i FROM Inventario i
        JOIN FETCH i.producto p
        JOIN FETCH p.categoria
        JOIN FETCH p.unidadMedida
        WHERE i.almacen.almacenId = :almacenId
          AND (:categoriaId IS NULL OR p.categoria.categoriaId = :categoriaId)
          AND p.activo = true
    """)
    List<Inventario> findByAlmacen(
        @Param("almacenId") Integer almacenId,
        @Param("categoriaId") Integer categoriaId);

    @Query("""
        SELECT i FROM Inventario i
        WHERE i.almacen.almacenId = :almacenId
          AND i.producto.productoId = :productoId
    """)
    Optional<Inventario> findByAlmacenAndProducto(
        @Param("almacenId") Integer almacenId,
        @Param("productoId") Integer productoId);

    // Query directa a BD — evita caché JPA, atómica
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Inventario i
        SET i.cantidad = i.cantidad + :delta,
            i.actualizado = CURRENT_TIMESTAMP
        WHERE i.almacen.almacenId = :almacenId
          AND i.producto.productoId = :productoId
    """)
    int actualizarCantidad(
        @Param("almacenId") Integer almacenId,
        @Param("productoId") Integer productoId,
        @Param("delta") BigDecimal delta);

    // Stock total por producto — UNA query, sin N+1
    @Query("""
        SELECT i.producto.productoId, SUM(i.cantidad)
        FROM Inventario i
        GROUP BY i.producto.productoId
    """)
    List<Object[]> stockTotalPorProducto();
}
