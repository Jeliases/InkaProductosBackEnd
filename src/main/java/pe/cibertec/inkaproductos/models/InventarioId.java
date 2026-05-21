package pe.cibertec.inkaproductos.models;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class InventarioId implements Serializable {
    @Column(name = "almacen_id")
    private Integer almacenId;

    @Column(name = "producto_id")
    private Integer productoId;
}