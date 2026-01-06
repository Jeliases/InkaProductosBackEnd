package pe.cibertec.inkaproductos.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Formula; // <--- IMPORTANTE

@Data
@Entity
@Table(name = "producto")
public class Producto {
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

    // Esta fórmula suma las cantidades de la tabla inventario para este ID de producto
    @Formula("(SELECT SUM(i.cantidad) FROM inventario i WHERE i.producto_id = producto_id)")
    private Double stock;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
}