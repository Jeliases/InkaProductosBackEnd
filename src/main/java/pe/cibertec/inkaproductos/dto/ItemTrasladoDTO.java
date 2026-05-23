package pe.cibertec.inkaproductos.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ItemTrasladoDTO {
    private Integer productoId;
    private BigDecimal cantidad;
}
