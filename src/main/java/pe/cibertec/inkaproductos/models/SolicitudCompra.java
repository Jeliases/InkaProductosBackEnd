package pe.cibertec.inkaproductos.models;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "solicitud_compra")
public class SolicitudCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer solicitudId;

    @Column(name = "fecha_solicitud", insertable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    @ManyToOne
    @JoinColumn(name = "almacen_origen_id")
    private Almacen origen;

    @ManyToOne
    @JoinColumn(name = "almacen_destino_id")
    private Almacen destino;

    private String usuarioSolicitante;

    private String estado;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL)
    private List<SolicitudCompraDetalle> detalles;


}