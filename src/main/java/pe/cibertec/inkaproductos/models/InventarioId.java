package pe.cibertec.inkaproductos.models;

import jakarta.persistence.*;

import lombok.Data;
import java.io.Serializable;

@Data
@Embeddable
public class InventarioId implements Serializable {
    @Column(name = "almacen_id")
    private Integer almacenId;

    @Column(name = "producto_id")
    private Integer productoId;
}