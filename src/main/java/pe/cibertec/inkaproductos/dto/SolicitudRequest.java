package pe.cibertec.inkaproductos.dto;

import lombok.Data;
import java.util.List;

@Data
public class SolicitudRequest {
    private Integer origenId;
    private Integer destinoId;
    private String observacion;
    private List<ItemTrasladoDTO> items;
}
