package pe.cibertec.inkaproductos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cibertec.inkaproductos.dto.SolicitudRequest;
import pe.cibertec.inkaproductos.dto.TrasladoRequest;
import pe.cibertec.inkaproductos.models.*;
import pe.cibertec.inkaproductos.repositories.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final SolicitudRepository    solicitudRepo;
    private final AlmacenRepository      almacenRepo;
    private final ProductoRepository     productoRepo;
    private final TrasladoService        trasladoService;

    @Transactional
    public SolicitudCompra crear(SolicitudRequest req, String emailUsuario) {

        Almacen origen  = almacenRepo.findById(req.getOrigenId()).orElseThrow();
        Almacen destino = almacenRepo.findById(req.getDestinoId()).orElseThrow();

        SolicitudCompra sol = new SolicitudCompra();
        sol.setOrigen(origen);
        sol.setDestino(destino);
        sol.setUsuarioSolicitante(emailUsuario);
        sol.setEstado(EstadoSolicitud.PENDIENTE);

        List<SolicitudCompraDetalle> detalles = req.getItems().stream().map(item -> {
            SolicitudCompraDetalle d = new SolicitudCompraDetalle();
            d.setSolicitud(sol);
            d.setProducto(productoRepo.findById(item.getProductoId()).orElseThrow());
            d.setCantidad(item.getCantidad());
            return d;
        }).toList();

        sol.setDetalles(detalles);
        return solicitudRepo.save(sol);
    }

    @Transactional
    public void aprobar(Integer solicitudId, String emailAdmin) {
        SolicitudCompra sol = solicitudRepo.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (sol.getEstado() != EstadoSolicitud.PENDIENTE)
            throw new RuntimeException("La solicitud ya fue procesada");

        // Convertir solicitud en traslado real
        TrasladoRequest trasladoReq = new TrasladoRequest();
        trasladoReq.setOrigenId(sol.getOrigen().getAlmacenId());
        trasladoReq.setDestinoId(sol.getDestino().getAlmacenId());
        trasladoReq.setObservacion("Aprobación de solicitud #" + solicitudId);
        trasladoReq.setItems(sol.getDetalles().stream().map(d -> {
            pe.cibertec.inkaproductos.dto.ItemTrasladoDTO item =
                    new pe.cibertec.inkaproductos.dto.ItemTrasladoDTO();
            item.setProductoId(d.getProducto().getProductoId());
            item.setCantidad(d.getCantidad());
            return item;
        }).toList());

        trasladoService.procesar(trasladoReq, emailAdmin);
        sol.setEstado(EstadoSolicitud.APROBADA);
    }

    @Transactional
    public void rechazar(Integer solicitudId) {
        SolicitudCompra sol = solicitudRepo.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        if (sol.getEstado() != EstadoSolicitud.PENDIENTE)
            throw new RuntimeException("La solicitud ya fue procesada");
        sol.setEstado(EstadoSolicitud.RECHAZADA);
    }

    public List<SolicitudCompra> pendientes() {
        return solicitudRepo.findByEstado(EstadoSolicitud.PENDIENTE);
    }

    public List<SolicitudCompra> misSolicitudes(String email) {
        return solicitudRepo.findByUsuario(email);
    }
}
