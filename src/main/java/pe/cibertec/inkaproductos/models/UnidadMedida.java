package pe.cibertec.inkaproductos.models;

import jakarta.persistence.*;
import lombok.Data;

@Data @Entity @Table(name = "unidad_medida")
public class UnidadMedida {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer uomId;
    private String codigo;
    private String descripcion;
}
