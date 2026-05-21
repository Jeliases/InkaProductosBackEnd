package pe.cibertec.inkaproductos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {

    private Integer productoId;
    private String  sku;
    private String  nombre;
    private String  descripcion;
    private Double  precioLista;
    private Integer activo;

    private Integer categoriaId;
    private String  categoriaNombre;

    // Stock total en todos los almacenes (para la vista de inventario general)
    private Double stock;

    // Stock específico del almacén filtrado (para el carrito — validación correcta)
    private Double stockAlmacen;
}