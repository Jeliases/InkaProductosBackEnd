package pe.cibertec.inkaproductos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.cibertec.inkaproductos.dto.ItemCarritoDTO;
import pe.cibertec.inkaproductos.dto.ProductoDTO;
import pe.cibertec.inkaproductos.dto.TransaccionDTO;
import pe.cibertec.inkaproductos.models.*;
import pe.cibertec.inkaproductos.repositories.*;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepo;
    private final MovimientoService movimientoService;

    // ===============================
    // LISTAR
    // ===============================
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    // ===============================
    // CRUD
    // ===============================
    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    public void eliminar(Integer id) {
        productoRepository.deleteById(id);
    }

    // ===============================
    // FILTRAR PARA ANGULAR
    // ===============================
    public List<ProductoDTO> filtrarProductos(Integer categoriaId, Integer almacenId) {

        Integer cat = (categoriaId != null && categoriaId > 0) ? categoriaId : null;
        Integer alm = (almacenId != null && almacenId > 0) ? almacenId : null;

        List<Producto> productos;

        if (alm == null) {
            productos = productoRepository.filtrarProductos(cat);
        } else {
            var inventarios = inventarioRepo.filtrarInventario(alm, cat);

            productos = inventarios.stream()
                    .map(inv -> inv.getProducto())
                    .distinct()
                    .toList();
        }

        return productos.stream()
                .map(p -> new ProductoDTO(
                        p.getProductoId(),
                        p.getSku(),
                        p.getNombre(),
                        p.getDescripcion(),
                        p.getPrecioLista(),
                        p.getActivo(),
                        p.getCategoria() != null ? p.getCategoria().getCategoriaId() : null,
                        p.getCategoria() != null ? p.getCategoria().getNombre() : null,
                        obtenerStockTotal(p)
                ))
                .toList();
    }

    // ===============================
    // CALCULAR STOCK TOTAL
    // ===============================
    private Double obtenerStockTotal(Producto p) {
        return inventarioRepo.findByProducto(p)
                .stream()
                .mapToDouble(Inventario::getCantidad)
                .sum();
    }

    @Transactional
    public void ajustarStock(Integer almacenId, Integer productoId, Double delta) {

        Inventario inv = inventarioRepo.buscarProductoEnAlmacen(almacenId, productoId)
                .orElseGet(() -> {
                    Inventario nuevo = new Inventario();

                    InventarioId id = new InventarioId();
                    id.setAlmacenId(almacenId);
                    id.setProductoId(productoId);

                    Almacen a = new Almacen();
                    a.setAlmacenId(almacenId);

                    Producto p = new Producto();
                    p.setProductoId(productoId);

                    nuevo.setId(id);
                    nuevo.setAlmacen(a);
                    nuevo.setProducto(p);
                    nuevo.setCantidad(0.0);

                    return inventarioRepo.save(nuevo);
                });

        double nuevaCantidad = inv.getCantidad() + delta;

        if (nuevaCantidad < 0)
            throw new RuntimeException("Stock insuficiente en almacén " + almacenId);

        inv.setCantidad(nuevaCantidad);
        inventarioRepo.save(inv);
    }

    // ============================================
    // MOVER STOCK (wrapper)
    // ============================================
    public void moverStock(Integer origenId, Integer destinoId, Integer productoId, Double cantidad) {
        ajustarStock(origenId, productoId, -cantidad);
        ajustarStock(destinoId, productoId, cantidad);
    }

    @Transactional
    public void procesarTransaccion(TransaccionDTO dto) {

        if (!dto.isEsAdmin()) {
            throw new RuntimeException("Solo el administrador puede realizar movimientos directos.");
        }

        // Primero mover stock REAL
        for (ItemCarritoDTO item : dto.getItems()) {
            moverStock(
                    dto.getOrigenId(),
                    dto.getDestinoId(),
                    item.getProductoId(),
                    item.getCantidad()
            );
        }

        // Luego registrar el movimiento en historial
        movimientoService.registrarMovimiento(
                dto.getUsuarioEmail(),
                dto.getOrigenId(),
                dto.getDestinoId(),
                dto.getItems()
        );
    }

}