package pe.cibertec.inkaproductos.models;


import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
@Entity
@Table(name = "solicitud_compra_detalle")
public class SolicitudCompraDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer detalleId;

    @ManyToOne
    @JoinColumn(name = "solicitud_id")
    @JsonIgnore // Evita recursividad infinita en el JSON REST [cite: 3089]
    private SolicitudCompra solicitud;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private Double cantidad;


}