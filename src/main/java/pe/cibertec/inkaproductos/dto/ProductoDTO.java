package pe.cibertec.inkaproductos.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductoDTO {
    private Integer productoId;
    private String sku;
    private String nombre;
    private String descripcion;
    private String categoria;
    private String uom;
    private BigDecimal precioLista;
    private boolean activo;             // Nuevo campo para indicar si está activo
    private BigDecimal stockTotal;      // suma en todos los almacenes
    private BigDecimal stockAlmacen;    // stock en el almacén filtrado (null si no se filtra)
}