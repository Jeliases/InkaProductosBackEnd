package pe.cibertec.inkaproductos.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.cibertec.inkaproductos.models.SolicitudCompra;

import java.util.List;

public interface SolicitudCompraRepository extends JpaRepository<SolicitudCompra, Integer> {


    List<SolicitudCompra> findByEstadoIgnoreCase(String estado);

    List<SolicitudCompra> findByUsuarioSolicitante(String email);

    List<SolicitudCompra> findByEstado(String estado);

}
