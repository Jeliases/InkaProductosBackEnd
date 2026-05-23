package pe.cibertec.inkaproductos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.cibertec.inkaproductos.models.EstadoSolicitud;
import pe.cibertec.inkaproductos.models.SolicitudCompra;
import java.util.List;

public interface SolicitudRepository extends JpaRepository<SolicitudCompra, Integer> {

    @Query("""
        SELECT s FROM SolicitudCompra s
        JOIN FETCH s.origen
        JOIN FETCH s.destino
        JOIN FETCH s.detalles d
        JOIN FETCH d.producto
        WHERE s.estado = :estado
        ORDER BY s.fechaSolicitud DESC
    """)
    List<SolicitudCompra> findByEstado(EstadoSolicitud estado);

    @Query("""
        SELECT s FROM SolicitudCompra s
        JOIN FETCH s.origen
        JOIN FETCH s.destino
        JOIN FETCH s.detalles d
        JOIN FETCH d.producto
        WHERE s.usuarioSolicitante = :email
        ORDER BY s.fechaSolicitud DESC
    """)
    List<SolicitudCompra> findByUsuario(String email);
}
