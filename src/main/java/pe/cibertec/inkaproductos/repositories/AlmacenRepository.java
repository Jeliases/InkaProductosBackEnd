package pe.cibertec.inkaproductos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.cibertec.inkaproductos.models.Almacen;
import java.util.List;

public interface AlmacenRepository extends JpaRepository<Almacen, Integer> {
    List<Almacen> findByActivoTrue();
}
