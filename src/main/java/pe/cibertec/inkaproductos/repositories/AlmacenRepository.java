package pe.cibertec.inkaproductos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.cibertec.inkaproductos.models.Almacen;
import java.util.Optional;

@Repository
public interface AlmacenRepository extends JpaRepository<Almacen, Integer> {

    // Sirve para validar que no se repitan nombres de almacenes
    Optional<Almacen> findByNombreIgnoreCase(String nombre);

    // Útil para verificar existencia rápida
    boolean existsByNombre(String nombre);
}