package pe.cibertec.inkaproductos.models;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "inventario")
public class Inventario {
    @EmbeddedId
    private InventarioId id;

    @ManyToOne
    @MapsId("almacenId")
    @JoinColumn(name = "almacen_id")
    private Almacen almacen;

    @ManyToOne
    @MapsId("productoId")
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private Double cantidad;

}