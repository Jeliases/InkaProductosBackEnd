package pe.cibertec.inkaproductos.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Data @Entity @Table(name = "unidad_medida")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UnidadMedida {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer uomId;
    private String codigo;
    private String descripcion;
}
