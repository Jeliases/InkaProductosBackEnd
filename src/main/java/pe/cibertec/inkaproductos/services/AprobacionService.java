package pe.cibertec.inkaproductos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cibertec.inkaproductos.models.*;
import pe.cibertec.inkaproductos.repositories.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AprobacionService {

    private final SolicitudCompraRepository solicitudRepo;
    private final SolicitudDetalleRepository detalleRepo;
    private final InventarioRepository inventarioRepo;
    private final ProductoService productoService;

    public List<SolicitudCompra> listarPendientes() {
        return solicitudRepo.findByEstadoIgnoreCase("PENDIENTE");
    }

    @Transactional
    public SolicitudCompra aprobar(Integer solicitudId) {

        SolicitudCompra s = solicitudRepo.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!s.getEstado().equals("PENDIENTE"))
            throw new RuntimeException("Solo se pueden aprobar solicitudes pendientes");

        List<SolicitudCompraDetalle> detalles =
                detalleRepo.findBySolicitud_SolicitudId(solicitudId);

        for (SolicitudCompraDetalle d : detalles) {
            productoService.moverStock(
                    s.getOrigen().getAlmacenId(),
                    s.getDestino().getAlmacenId(),
                    d.getProducto().getProductoId(),
                    d.getCantidad()
            );
        }

        s.setEstado("APROBADA");
        return solicitudRepo.save(s);
    }

    @Transactional
    public SolicitudCompra rechazar(Integer solicitudId) {

        SolicitudCompra s = solicitudRepo.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!s.getEstado().equals("PENDIENTE"))
            throw new RuntimeException("Solo se pueden rechazar solicitudes pendientes");

        s.setEstado("RECHAZADA");
        return solicitudRepo.save(s);
    }
}
