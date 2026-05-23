package pe.cibertec.inkaproductos.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data @AllArgsConstructor
public class StockEvent {
    private Integer almacenId;
    private Integer productoId;
    private BigDecimal cantidad;
    private String tipo;
}
