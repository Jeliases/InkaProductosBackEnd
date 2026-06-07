package pe.cibertec.inkaproductos.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class ProductoRequestDTO {
    private String sku;
    private String nombre;
    private String descripcion;
    private Integer categoriaId;
    private Integer uomId; // ID de Unidad de Medida
    private BigDecimal precioLista;
    private Map<Integer, BigDecimal> stockInicial; // K: almacenId, V: cantidad
}