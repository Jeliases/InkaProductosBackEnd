package pe.cibertec.inkaproductos.models;
import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "rol")
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rolId;

    @Column(unique = true, nullable = false)
    private String nombre;
}
