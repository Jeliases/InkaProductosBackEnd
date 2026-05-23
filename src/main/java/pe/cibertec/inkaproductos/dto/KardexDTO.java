package pe.cibertec.inkaproductos.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class KardexDTO {
    private Long movimientoId;
    private LocalDateTime fecha;
    private String tipoOperacion;
    private String sku;
    private String producto;
    private String almacenOrigen;
    private String almacenDestino;
    private BigDecimal stockAnterior;
    private BigDecimal cantidadMovida;
    private BigDecimal stockNuevo;
    private String usuario;
    private String referencia;
    private String observacion;
}
