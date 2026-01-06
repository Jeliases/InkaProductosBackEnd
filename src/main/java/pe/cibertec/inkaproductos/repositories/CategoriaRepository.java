package pe.cibertec.inkaproductos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.cibertec.inkaproductos.models.Categoria;
import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    // Para buscar categorías por nombre exacto
    List<Categoria> findByNombreContainingIgnoreCase(String nombre);

    // Validación de duplicados
    boolean existsByNombre(String nombre);
}