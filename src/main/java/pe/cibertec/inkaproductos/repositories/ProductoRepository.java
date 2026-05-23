package pe.cibertec.inkaproductos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.cibertec.inkaproductos.models.Producto;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    @Query("""
        SELECT p FROM Producto p
        JOIN FETCH p.categoria
        JOIN FETCH p.unidadMedida
        WHERE p.activo = true
          AND (:categoriaId IS NULL OR p.categoria.categoriaId = :categoriaId)
    """)
    List<Producto> findActivos(@Param("categoriaId") Integer categoriaId);
}
