package pe.cibertec.inkaproductos.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "mensaje_ti")
public class MensajeTi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer mensajeId;

    @Column(name = "emisor_email", nullable = false)
    private String emisor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_solicitud")
    private TipoSolicitudTi tipo;

    @Enumerated(EnumType.STRING)
    private PrioridadTi prioridad;

    private String asunto;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "fecha_envio", insertable = false, updatable = false)
    private LocalDateTime fechaEnvio;

    @Column(name = "fecha_atencion")
    private LocalDateTime fechaAtencion;

    private String estado; // PENDIENTE, EN_PROCESO, ATENDIDO

    @Version
    private Integer version;
}

enum TipoSolicitudTi {
    NUEVO_PRODUCTO, ELIMINAR_USUARIO, EDITAR_STOCK, OTROS
}

enum PrioridadTi {
    BAJA, MEDIA, ALTA, URGENTE
}