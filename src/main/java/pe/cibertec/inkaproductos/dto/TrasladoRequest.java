package pe.cibertec.inkaproductos.dto;

import lombok.Data;
import java.util.List;

@Data
public class TrasladoRequest {
    private Integer origenId;
    private Integer destinoId;
    private String referencia;
    private String observacion;
    private List<ItemTrasladoDTO> items;
}
