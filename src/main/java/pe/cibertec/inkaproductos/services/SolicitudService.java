package pe.cibertec.inkaproductos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cibertec.inkaproductos.dto.SolicitudCompraDTO;
import pe.cibertec.inkaproductos.dto.SolicitudRequest;
import pe.cibertec.inkaproductos.dto.TrasladoRequest;
import pe.cibertec.inkaproductos.models.*;
import pe.cibertec.inkaproductos.repositories.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final SolicitudRepository    solicitudRepo;
    private final AlmacenRepository      almacenRepo;
    private final ProductoRepository     productoRepo;
    private final TrasladoService        trasladoService;

    @Transactional
    public SolicitudCompraDTO crear(SolicitudRequest req, String emailUsuario) {

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
            d.setCantidad(item.getCantidad().intValue()); // Convertir BigDecimal a int
            return d;
        }).toList();

        sol.setDetalles(detalles);
        SolicitudCompra savedSol = solicitudRepo.save(sol);
        
        return SolicitudCompraDTO.from(savedSol);
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
            item.setCantidad(BigDecimal.valueOf(d.getCantidad())); // Convertir int a BigDecimal
            return item;
        }).toList());

        trasladoService.procesar(trasladoReq, emailAdmin);

        sol.setEstado(EstadoSolicitud.APROBADA);
        // ¡ESTA ES LA LÍNEA MÁGICA QUE FALTABA!
        solicitudRepo.save(sol);
    }

    @Transactional
    public void rechazar(Integer solicitudId) {
        SolicitudCompra sol = solicitudRepo.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (sol.getEstado() != EstadoSolicitud.PENDIENTE)
            throw new RuntimeException("La solicitud ya fue procesada");

        sol.setEstado(EstadoSolicitud.RECHAZADA);
        // ¡ESTA ES LA LÍNEA MÁGICA QUE FALTABA!
        solicitudRepo.save(sol);
    }

    @Transactional(readOnly = true)
    public List<SolicitudCompraDTO> pendientes() {
        return solicitudRepo.findByEstado(EstadoSolicitud.PENDIENTE).stream()
                .map(SolicitudCompraDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SolicitudCompraDTO> misSolicitudes(String email) {
        return solicitudRepo.findByUsuario(email).stream()
                .map(SolicitudCompraDTO::from)
                .collect(Collectors.toList());
    }
}