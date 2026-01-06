package pe.cibertec.inkaproductos.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "movimiento_detalle")
public class MovimientoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "movimiento_id")
    @JsonBackReference
    private Movimiento movimiento;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private Double cantidad;
}
