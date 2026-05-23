package pe.cibertec.inkaproductos.models;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data @NoArgsConstructor @AllArgsConstructor @Embeddable
public class InventarioId implements Serializable {
    private Integer almacenId;
    private Integer productoId;
}
