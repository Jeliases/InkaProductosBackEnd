package pe.cibertec.inkaproductos.dto;

import lombok.Data;

@Data
public class ItemCarritoDTO {
    private Integer productoId;
    private Double cantidad;
}