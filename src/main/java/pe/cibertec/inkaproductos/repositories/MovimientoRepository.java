package pe.cibertec.inkaproductos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.cibertec.inkaproductos.models.Movimiento;

import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Integer> {

}
