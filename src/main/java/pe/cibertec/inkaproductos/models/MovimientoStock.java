package pe.cibertec.inkaproductos.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "movimiento_stock")
public class MovimientoStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movimiento_id")
    private Long movimientoId;

    @Column(name = "fecha")
    private LocalDateTime fecha = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacion", nullable = false)
    private TipoOperacion tipoOperacion;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "almacen_origen_id", nullable = false)
    private Almacen origen;

    @ManyToOne
    @JoinColumn(name = "almacen_destino_id", nullable = false)
    private Almacen destino;

    @Column(name = "stock_anterior", nullable = false)
    private Double stockAnterior;

    @Column(name = "cantidad_movida", nullable = false)
    private Double cantidadMovida;

    @Column(name = "stock_nuevo", nullable = false)
    private Double stockNuevo;

    @Column(name = "usuario", nullable = false)
    private String usuario;

    @Column(name = "referencia")
    private String referencia;

    @Column(name = "observacion")
    private String observacion;
}