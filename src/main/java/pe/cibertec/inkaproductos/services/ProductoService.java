package pe.cibertec.inkaproductos.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cibertec.inkaproductos.dto.ProductoDTO;
import pe.cibertec.inkaproductos.dto.ProductoRequestDTO;
import pe.cibertec.inkaproductos.models.*;
import pe.cibertec.inkaproductos.repositories.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepo;
    private final InventarioRepository inventarioRepo;
    private final CategoriaRepository categoriaRepo;
    private final UnidadMedidaRepository uomRepo;
    private final AuditoriaProductoRepository auditoriaRepo;
    private final AlmacenRepository almacenRepo; // Añadido para validar almacenes
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<ProductoDTO> listar(Integer categoriaId, Integer almacenId, Boolean activo) {
        boolean filterActivo = (activo == null) ? true : activo;
        List<Producto> productos = productoRepo.findWithFilters(categoriaId, filterActivo);
        Map<Integer, BigDecimal> stockTotal = inventarioRepo.stockTotalPorProducto()
                .stream()
                .collect(Collectors.toMap(r -> (Integer) r[0], r -> (BigDecimal) r[1]));

        if (almacenId == null || almacenId == 0) {
            return productos.stream()
                    .map(p -> toDTO(p, stockTotal.getOrDefault(p.getProductoId(), BigDecimal.ZERO), null))
                    .toList();
        } else {
            return inventarioRepo.findByAlmacen(almacenId, categoriaId).stream()
                    .filter(inv -> inv.getProducto().isActivo() == filterActivo)
                    .map(inv -> toDTO(
                            inv.getProducto(),
                            stockTotal.getOrDefault(inv.getProducto().getProductoId(), BigDecimal.ZERO),
                            inv.getCantidad()))
                    .toList();
        }
    }

    @Transactional(readOnly = true)
    public ProductoDTO findById(Integer id) {
        Producto p = productoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        return toDTO(p, null, null);
    }

    @Transactional
    public ProductoDTO crear(ProductoRequestDTO request, String emailUsuario) {
        productoRepo.findBySku(request.getSku()).ifPresent(p -> {
            throw new IllegalArgumentException("El SKU '" + request.getSku() + "' ya existe.");
        });

        Categoria categoria = categoriaRepo.findById(request.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        UnidadMedida uom = uomRepo.findById(request.getUomId())
                .orElseThrow(() -> new RuntimeException("Unidad de Medida no encontrada"));

        Producto p = new Producto();
        p.setSku(request.getSku());
        p.setNombre(request.getNombre());
        p.setDescripcion(request.getDescripcion());
        p.setPrecioLista(request.getPrecioLista());
        p.setCategoria(categoria);
        p.setUnidadMedida(uom);
        p.setActivo(true);

        Producto nuevoProducto = productoRepo.save(p);

        BigDecimal stockTotal = BigDecimal.ZERO;
        if (request.getStockInicial() != null && !request.getStockInicial().isEmpty()) {
            for (Map.Entry<Integer, BigDecimal> entry : request.getStockInicial().entrySet()) {
                Integer almacenId = entry.getKey();
                BigDecimal cantidad = entry.getValue();

                Almacen almacen = almacenRepo.findById(almacenId)
                        .orElseThrow(() -> new RuntimeException("El almacén con ID " + almacenId + " no existe."));

                Inventario inventario = new Inventario();
                inventario.setId(new InventarioId(almacenId, nuevoProducto.getProductoId()));
                inventario.setProducto(nuevoProducto);
                inventario.setAlmacen(almacen);
                inventario.setCantidad(cantidad);
                inventarioRepo.save(inventario);

                stockTotal = stockTotal.add(cantidad);
            }
        }

        auditar(nuevoProducto, emailUsuario, "CREAR", null, request);
        return toDTO(nuevoProducto, stockTotal, null);
    }

    @Transactional
    public ProductoDTO actualizar(Integer id, ProductoRequestDTO request, String emailUsuario) {
        Producto p = productoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (request.getSku() != null && !request.getSku().equals(p.getSku())) {
            throw new IllegalArgumentException("El SKU no puede ser modificado.");
        }

        Map<String, Object> antes = objectMapper.convertValue(toDTO(p, null, null), Map.class);

        p.setNombre(request.getNombre());
        p.setDescripcion(request.getDescripcion());
        p.setPrecioLista(request.getPrecioLista());
        p.setCategoria(categoriaRepo.findById(request.getCategoriaId()).orElseThrow());
        p.setUnidadMedida(uomRepo.findById(request.getUomId()).orElseThrow());

        Producto productoActualizado = productoRepo.save(p);
        auditar(productoActualizado, emailUsuario, "EDITAR", antes, request);
        return toDTO(productoActualizado, null, null);
    }

    @Transactional
    public void desactivar(Integer id, String emailUsuario) {
        Producto p = productoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (!p.isActivo()) {
            throw new IllegalStateException("El producto ya está inactivo.");
        }

        p.setActivo(false);
        productoRepo.save(p);
        auditar(p, emailUsuario, "DESACTIVAR", Map.of("activo", true), Map.of("activo", false));
    }

    private void auditar(Producto producto, String email, String accion, Object antes, Object despues) {
        try {
            AuditoriaProducto audit = new AuditoriaProducto();
            audit.setProducto(producto);
            audit.setUsuarioEmail(email);
            audit.setAccion(accion);

            Map<String, Object> detalles = Map.of(
                    "antes", antes != null ? antes : "N/A",
                    "despues", despues != null ? despues : "N/A"
            );
            audit.setDetallesCambio(objectMapper.writeValueAsString(detalles));

            auditoriaRepo.save(audit);
        } catch (Exception e) {
            System.err.println("Error al auditar la acción: " + e.getMessage());
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
        dto.setActivo(p.isActivo());
        dto.setStockTotal(stockTotal);
        dto.setStockAlmacen(stockAlmacen);
        return dto;
    }
}