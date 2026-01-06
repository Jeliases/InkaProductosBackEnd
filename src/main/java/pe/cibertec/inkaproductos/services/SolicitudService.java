package pe.cibertec.inkaproductos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cibertec.inkaproductos.dto.TransaccionDTO;
import pe.cibertec.inkaproductos.models.*;
import pe.cibertec.inkaproductos.repositories.*;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final SolicitudCompraRepository repoSolicitud;
    private final SolicitudDetalleRepository repoDetalle;
    private final AlmacenRepository repoAlmacen;
    private final ProductoRepository repoProducto;

    // =====================
    // USER crea solicitud
    // =====================
    @Transactional
    public SolicitudCompra crearSolicitud(TransaccionDTO dto) {

        if (dto.getOrigenId() == null || dto.getDestinoId() == null)
            throw new RuntimeException("Origen y destino son obligatorios");

        if (dto.getOrigenId().equals(dto.getDestinoId()))
            throw new RuntimeException("El origen y destino deben ser distintos");

        if (dto.getItems() == null || dto.getItems().isEmpty())
            throw new RuntimeException("La solicitud está vacía");

        SolicitudCompra sc = new SolicitudCompra();
        sc.setOrigen(repoAlmacen.findById(dto.getOrigenId()).orElseThrow());
        sc.setDestino(repoAlmacen.findById(dto.getDestinoId()).orElseThrow());
        sc.setUsuarioSolicitante(dto.getUsuarioEmail());
        sc.setEstado("PENDIENTE");

        sc = repoSolicitud.save(sc);

        List<SolicitudCompraDetalle> detalles = new ArrayList<>();

        for (var item : dto.getItems()) {
            SolicitudCompraDetalle d = new SolicitudCompraDetalle();
            d.setSolicitud(sc);
            d.setProducto(repoProducto.findById(item.getProductoId()).orElseThrow());
            d.setCantidad(item.getCantidad());
            detalles.add(d);
        }

        repoDetalle.saveAll(detalles);
        return sc;
    }

    public List<SolicitudCompra> listarPorUsuario(String email) {
        return repoSolicitud.findByUsuarioSolicitante(email);
    }

    public List<SolicitudCompra> listarPendientes() {
        return repoSolicitud.findByEstado("PENDIENTE");
    }
}
