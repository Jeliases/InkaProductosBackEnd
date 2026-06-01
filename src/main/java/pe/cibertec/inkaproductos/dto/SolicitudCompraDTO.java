package pe.cibertec.inkaproductos.dto;

import lombok.Data;
import pe.cibertec.inkaproductos.models.SolicitudCompra;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class SolicitudCompraDTO {
    private Integer solicitudId;
    private LocalDateTime fechaSolicitud;
    private String origenNombre;
    private String destinoNombre;
    private String usuarioSolicitante;
    private String estado;
    private List<SolicitudCompraDetalleDTO> detalles;

    public static SolicitudCompraDTO from(SolicitudCompra solicitud) {
        SolicitudCompraDTO dto = new SolicitudCompraDTO();
        dto.setSolicitudId(solicitud.getSolicitudId());
        dto.setFechaSolicitud(solicitud.getFechaSolicitud());
        dto.setOrigenNombre(solicitud.getOrigen() != null ? solicitud.getOrigen().getNombre() : null);
        dto.setDestinoNombre(solicitud.getDestino() != null ? solicitud.getDestino().getNombre() : null);
        dto.setUsuarioSolicitante(solicitud.getUsuarioSolicitante());
        dto.setEstado(solicitud.getEstado().name());
        if (solicitud.getDetalles() != null) {
            dto.setDetalles(solicitud.getDetalles().stream()
                    .map(SolicitudCompraDetalleDTO::from)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}