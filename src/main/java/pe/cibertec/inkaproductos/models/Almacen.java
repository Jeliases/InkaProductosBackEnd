package pe.cibertec.inkaproductos.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Data @Entity @Table(name = "almacen")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Almacen {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer almacenId;
    private String nombre;
    private String ciudad;
    private String direccion;
    private boolean activo = true;
}
