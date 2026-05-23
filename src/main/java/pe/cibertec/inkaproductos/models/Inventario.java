package pe.cibertec.inkaproductos.models;

import jakarta.persistence.*;
import lombok.Data;
import pe.cibertec.inkaproductos.models.Almacen;
import pe.cibertec.inkaproductos.models.InventarioId;
import pe.cibertec.inkaproductos.models.Producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Entity @Table(name = "inventario")
public class Inventario {
    @EmbeddedId
    private InventarioId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("almacenId")
    @JoinColumn(name = "almacen_id")
    private Almacen almacen;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productoId")
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private BigDecimal cantidad = BigDecimal.ZERO;

    private LocalDateTime actualizado;

    @PrePersist
    @PreUpdate
    public void touch() {
        this.actualizado = LocalDateTime.now();
    }
}