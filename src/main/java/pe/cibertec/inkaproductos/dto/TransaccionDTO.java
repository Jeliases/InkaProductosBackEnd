package pe.cibertec.inkaproductos.dto;

import lombok.Data;
import java.util.List;

@Data
public class TransaccionDTO {

    private Integer origenId;
    private Integer destinoId;

    private String usuarioEmail;
    private boolean esAdmin;


    private List<ItemCarritoDTO> items;
}