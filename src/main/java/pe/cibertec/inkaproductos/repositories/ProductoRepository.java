package pe.cibertec.inkaproductos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.cibertec.inkaproductos.models.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // Método para buscar un producto por su SKU. Devuelve un Optional.
    Optional<Producto> findBySku(String sku);

    // Consulta mejorada para filtrar por categoría y estado de actividad.
    @Query("""
        SELECT p FROM Producto p
        WHERE (:activo IS NULL OR p.activo = :activo)
          AND (:categoriaId IS NULL OR p.categoria.categoriaId = :categoriaId)
    """)
    List<Producto> findWithFilters(@Param("categoriaId") Integer categoriaId, @Param("activo") Boolean activo);
}