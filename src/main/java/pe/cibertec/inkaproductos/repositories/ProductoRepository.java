package pe.cibertec.inkaproductos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.cibertec.inkaproductos.models.Producto;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    @Query("""
    SELECT p FROM Producto p
    WHERE (:categoriaId IS NULL OR p.categoria.categoriaId = :categoriaId)
""")
    List<Producto> filtrarProductos(@Param("categoriaId") Integer categoriaId);


}