package pe.cibertec.inkaproductos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cibertec.inkaproductos.dto.ProductoDTO;
import pe.cibertec.inkaproductos.models.Producto;
import pe.cibertec.inkaproductos.repositories.InventarioRepository;
import pe.cibertec.inkaproductos.repositories.ProductoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepo;
    private final InventarioRepository inventarioRepo;

    @Transactional(readOnly = true)
    public List<ProductoDTO> listar(Integer categoriaId, Integer almacenId) {

        // Stock total de todos los almacenes en una sola query
        Map<Integer, BigDecimal> stockTotal = inventarioRepo.stockTotalPorProducto()
                .stream()
                .collect(Collectors.toMap(
                        r -> (Integer) r[0],
                        r -> (BigDecimal) r[1]));

        if (almacenId == null || almacenId == 0) {
            // Sin filtro de almacén — todos los productos
            return productoRepo.findActivos(categoriaId).stream()
                    .map(p -> toDTO(p, stockTotal.getOrDefault(p.getProductoId(), BigDecimal.ZERO), null))
                    .toList();
        } else {
            // Con filtro de almacén — stock específico de ese almacén
            return inventarioRepo.findByAlmacen(almacenId, categoriaId).stream()
                    .map(inv -> toDTO(
                            inv.getProducto(),
                            stockTotal.getOrDefault(inv.getProducto().getProductoId(), BigDecimal.ZERO),
                            inv.getCantidad()))
                    .toList();
        }
    }

    private ProductoDTO toDTO(Producto p, BigDecimal stockTotal, BigDecimal stockAlmacen) {
        ProductoDTO dto = new ProductoDTO();
        dto.setProductoId(p.getProductoId());
        dto.setSku(p.getSku());
        dto.setNombre(p.getNombre());
        dto.setDescripcion(p.getDescripcion());
        dto.setCategoria(p.getCategoria() != null ? p.getCategoria().getNombre() : null);
        dto.setUom(p.getUnidadMedida() != null ? p.getUnidadMedida().getCodigo() : null);
        dto.setPrecioLista(p.getPrecioLista());
        dto.setStockTotal(stockTotal);
        dto.setStockAlmacen(stockAlmacen);
        return dto;
    }
}