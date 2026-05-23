package pe.cibertec.inkaproductos.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InventarioDTO {
    private Integer productoId;
    private String sku;
    private String nombre;
    private String categoria;
    private String uom;
    private BigDecimal cantidad;
    private LocalDateTime actualizado;
}
