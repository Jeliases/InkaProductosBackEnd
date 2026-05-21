package pe.cibertec.inkaproductos.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Formula;

@Data
@Entity
@Table(name = "producto")
public class Producto {

    // CORRECCIÓN: Quitamos @ManyToOne de aquí, el ID debe ser solo ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productoId;

    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false)
    private String descripcion;

    private String nombre;

    @Column(name = "precio_lista")
    private Double precioLista;

    private Integer activo;

    // NUEVO: Relación obligatoria con Unidad de Medida (para la Nueva BD)
    @ManyToOne
    @JoinColumn(name = "uom_id", nullable = false) // Debe coincidir con la columna en SQL
    private UnidadMedida unidadMedida;

    @Formula("(SELECT SUM(i.cantidad) FROM inventario i WHERE i.producto_id = producto_id)")
    private Double stock;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
}