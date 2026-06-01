package pe.cibertec.inkaproductos.dto;

import lombok.Data;
import pe.cibertec.inkaproductos.models.SolicitudCompraDetalle;

@Data
public class SolicitudCompraDetalleDTO {
    private Integer detalleId;
    private String productoNombre;
    private String productoSku;
    private int cantidad;

    public static SolicitudCompraDetalleDTO from(SolicitudCompraDetalle detalle) {
        SolicitudCompraDetalleDTO dto = new SolicitudCompraDetalleDTO();
        dto.setDetalleId(detalle.getDetalleId());
        dto.setProductoNombre(detalle.getProducto().getNombre());
        dto.setProductoSku(detalle.getProducto().getSku());
        dto.setCantidad(detalle.getCantidad());
        return dto;
    }
}