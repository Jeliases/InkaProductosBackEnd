package pe.cibertec.inkaproductos.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "movimiento")
public class Movimiento {

    public enum EstadoMovimiento { APROBADO, RECHAZADO, PENDIENTE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private LocalDateTime fecha;

    private String usuario;

    @ManyToOne
    @JoinColumn(name = "origen_id")
    private Almacen origen;

    @ManyToOne
    @JoinColumn(name = "destino_id")
    private Almacen destino;

    @Enumerated(EnumType.STRING)
    private EstadoMovimiento estado = EstadoMovimiento.APROBADO;

    @OneToMany(mappedBy = "movimiento", cascade = CascadeType.ALL)
    @JsonManagedReference
    @ToString.Exclude
    private List<MovimientoDetalle> detalles;
}
