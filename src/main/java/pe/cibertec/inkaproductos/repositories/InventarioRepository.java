package pe.cibertec.inkaproductos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.cibertec.inkaproductos.models.Inventario;
import pe.cibertec.inkaproductos.models.InventarioId;
import pe.cibertec.inkaproductos.models.Producto;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, InventarioId> {

    @Query("SELECT i FROM Inventario i WHERE i.almacen.almacenId = :idAlmacen")
    List<Inventario> listarPorAlmacen(@Param("idAlmacen") Integer idAlmacen);

    // Busca un producto específico en un almacén específico
    @Query("""
        SELECT i 
        FROM Inventario i 
        WHERE i.almacen.almacenId = :idAlmacen 
          AND i.producto.productoId = :idProducto
    """)
    Optional<Inventario> buscarProductoEnAlmacen(
            @Param("idAlmacen") Integer idAlmacen,
            @Param("idProducto") Integer idProducto
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Inventario i
        SET i.cantidad = i.cantidad + :cantidad
        WHERE i.almacen.almacenId = :almacenId
          AND i.producto.productoId = :productoId
    """)
    int actualizarCantidad(
            @Param("almacenId") Integer almacenId,
            @Param("productoId") Integer productoId,
            @Param("cantidad") Double cantidad
    );

    @Query("""
        SELECT i 
        FROM Inventario i 
        WHERE i.almacen.almacenId = :almacenId 
          AND (:categoriaId IS NULL OR i.producto.categoria.categoriaId = :categoriaId)
    """)
    List<Inventario> filtrarInventario(
            @Param("almacenId") Integer almacenId,
            @Param("categoriaId") Integer categoriaId
    );


    List<Inventario> findByProducto(Producto producto);
}
