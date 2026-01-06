package pe.cibertec.inkaproductos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {

    private Integer productoId;
    private String sku;
    private String nombre;
    private String descripcion;
    private Double precioLista;
    private Integer activo;

    private Integer categoriaId;
    private String categoriaNombre;

    private Double stock;
}
