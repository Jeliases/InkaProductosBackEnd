package pe.cibertec.inkaproductos.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Entity
@Table(name = "producto")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productoId;
    private String sku;
    private String nombre;
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uom_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private UnidadMedida unidadMedida;

    private BigDecimal precioLista;
    private boolean activo = true;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // <--- ¡ESTA ES LA CLAVE!
    private List<StockInicialDTO> stocksIniciales;

    @Data
    public static class StockInicialDTO {
        private Almacen almacen; // O tu entidad de Almacen
        private Integer cantidad;
    }

}