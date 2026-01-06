package pe.cibertec.inkaproductos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.cibertec.inkaproductos.models.SolicitudCompraDetalle;

import java.util.List;

@Repository
public interface SolicitudDetalleRepository extends JpaRepository<SolicitudCompraDetalle, Integer> {

    // ✔ CARGAR DETALLES POR solicitud_id
    List<SolicitudCompraDetalle> findBySolicitud_SolicitudId(Integer solicitudId);
}
