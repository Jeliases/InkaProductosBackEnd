package pe.cibertec.inkaproductos.dto;

import lombok.Data;
import java.util.List;

@Data
public class TransaccionDTO {

    private Integer origenId;
    private Integer destinoId;

    private String  usuarioEmail;
    private boolean esAdmin;

    // Opcional: el frontend puede enviarlos o se generan automáticamente
    private String referencia;   // ej: "GR-2026-0123", "OC-2026-042"
    private String observacion;  // ej: "Traslado por demanda regional"

    private List<ItemCarritoDTO> items;
}