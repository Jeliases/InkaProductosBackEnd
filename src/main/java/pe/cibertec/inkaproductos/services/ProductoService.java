package pe.cibertec.inkaproductos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.cibertec.inkaproductos.dto.ItemCarritoDTO;
import pe.cibertec.inkaproductos.dto.ProductoDTO;
import pe.cibertec.inkaproductos.dto.TransaccionDTO;
import pe.cibertec.inkaproductos.models.*;
import pe.cibertec.inkaproductos.repositories.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository        productoRepository;
    private final InventarioRepository      inventarioRepo;
    private final MovimientoService         movimientoService;
    private final AlmacenRepository         almacenRepo;
    private final MovimientoStockRepository movimientoStockRepository;

    // Contador simple para generar referencias únicas dentro de la misma ejecución
    private static final AtomicInteger seq = new AtomicInteger(1);

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    public void eliminar(Integer id) {
        productoRepository.deleteById(id);
    }

    // -------------------------------------------------------
    // FILTRAR PRODUCTOS
    // -------------------------------------------------------
    public List<ProductoDTO> filtrarProductos(Integer categoriaId, Integer almacenId) {

        Integer cat = (categoriaId != null && categoriaId > 0) ? categoriaId : null;
        Integer alm = (almacenId  != null && almacenId  > 0) ? almacenId  : null;

        if (alm == null) {
            return productoRepository.filtrarProductos(cat).stream()
                    .map(p -> toDTO(p, null))
                    .toList();
        } else {
            return inventarioRepo.filtrarInventario(alm, cat).stream()
                    .map(inv -> toDTOConStock(inv.getProducto(), inv.getCantidad()))
                    .toList();
        }
    }

    private Double obtenerStockTotal(Producto p) {
        return inventarioRepo.findByProducto(p)
                .stream()
                .mapToDouble(Inventario::getCantidad)
                .sum();
    }

    private ProductoDTO toDTO(Producto p, Double stockAlmacen) {
        Double stockTotal = obtenerStockTotal(p);
        return new ProductoDTO(
                p.getProductoId(), p.getSku(), p.getNombre(), p.getDescripcion(),
                p.getPrecioLista(), p.getActivo(),
                p.getCategoria() != null ? p.getCategoria().getCategoriaId() : null,
                p.getCategoria() != null ? p.getCategoria().getNombre()      : null,
                stockTotal,
                stockAlmacen != null ? stockAlmacen : stockTotal
        );
    }

    private ProductoDTO toDTOConStock(Producto p, Double stockAlmacen) {
        return new ProductoDTO(
                p.getProductoId(), p.getSku(), p.getNombre(), p.getDescripcion(),
                p.getPrecioLista(), p.getActivo(),
                p.getCategoria() != null ? p.getCategoria().getCategoriaId() : null,
                p.getCategoria() != null ? p.getCategoria().getNombre()      : null,
                obtenerStockTotal(p),
                stockAlmacen
        );
    }

    // -------------------------------------------------------
    // AJUSTAR STOCK
    // -------------------------------------------------------
    @Transactional
    public void ajustarStock(Integer almacenId, Integer productoId, Double delta) {

        Inventario inv = inventarioRepo.buscarProductoEnAlmacen(almacenId, productoId)
                .orElseGet(() -> {
                    Inventario nuevo = new Inventario();
                    InventarioId id  = new InventarioId();
                    id.setAlmacenId(almacenId);
                    id.setProductoId(productoId);
                    Almacen a = new Almacen(); a.setAlmacenId(almacenId);
                    Producto p = new Producto(); p.setProductoId(productoId);
                    nuevo.setId(id); nuevo.setAlmacen(a); nuevo.setProducto(p); nuevo.setCantidad(0.0);
                    return inventarioRepo.save(nuevo);
                });

        if (inv.getCantidad() + delta < 0)
            throw new RuntimeException(
                    "Stock insuficiente en almacén " + almacenId +
                            ". Actual: " + inv.getCantidad() + ", intento mover: " + Math.abs(delta));

        inventarioRepo.actualizarCantidad(almacenId, productoId, delta);
    }

    // -------------------------------------------------------
    // MOVER STOCK
    // -------------------------------------------------------
    @Transactional
    public void moverStock(Integer origenId, Integer destinoId, Integer productoId, Double cantidad) {
        ajustarStock(origenId,  productoId, -cantidad);
        ajustarStock(destinoId, productoId,  cantidad);
    }

    // -------------------------------------------------------
    // PROCESAR TRANSACCIÓN
    // -------------------------------------------------------
    @Transactional
    public void procesarTransaccion(TransaccionDTO dto) {

        if (!dto.isEsAdmin())
            throw new RuntimeException("Solo el administrador puede realizar movimientos directos.");

        Almacen origen  = almacenRepo.findById(dto.getOrigenId()) .orElseThrow();
        Almacen destino = almacenRepo.findById(dto.getDestinoId()).orElseThrow();

        // Generar referencia automática si el frontend no envió una
        String fecha      = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String referencia = (dto.getReferencia() != null && !dto.getReferencia().isBlank())
                ? dto.getReferencia()
                : "TRL-" + fecha + "-" + String.format("%03d", seq.getAndIncrement());

        String observacion = (dto.getObservacion() != null && !dto.getObservacion().isBlank())
                ? dto.getObservacion()
                : "Traslado " + origen.getNombre() + " → " + destino.getNombre();

        for (ItemCarritoDTO item : dto.getItems()) {

            Producto producto = productoRepository.findById(item.getProductoId()).orElseThrow();

            Double stockAnteriorOrigen = inventarioRepo
                    .buscarProductoEnAlmacen(dto.getOrigenId(), item.getProductoId())
                    .map(Inventario::getCantidad)
                    .orElseThrow(() -> new RuntimeException(
                            "Producto " + item.getProductoId() + " no existe en almacén origen"));

            Double stockAnteriorDestino = inventarioRepo
                    .buscarProductoEnAlmacen(dto.getDestinoId(), item.getProductoId())
                    .map(Inventario::getCantidad)
                    .orElse(0.0);

            this.moverStock(dto.getOrigenId(), dto.getDestinoId(), item.getProductoId(), item.getCantidad());

            // Kardex — SALIDA origen
            MovimientoStock salida = new MovimientoStock();
            salida.setProducto(producto);
            salida.setOrigen(origen);
            salida.setDestino(destino);
            salida.setTipoOperacion(TipoOperacion.TRASLADO);
            salida.setStockAnterior(stockAnteriorOrigen);
            salida.setCantidadMovida(item.getCantidad());
            salida.setStockNuevo(stockAnteriorOrigen - item.getCantidad());
            salida.setUsuario(dto.getUsuarioEmail());
            salida.setReferencia(referencia);
            salida.setObservacion("SALIDA — " + observacion);
            movimientoStockRepository.save(salida);

            // Kardex — ENTRADA destino
            MovimientoStock entrada = new MovimientoStock();
            entrada.setProducto(producto);
            entrada.setOrigen(origen);
            entrada.setDestino(destino);
            entrada.setTipoOperacion(TipoOperacion.TRASLADO);
            entrada.setStockAnterior(stockAnteriorDestino);
            entrada.setCantidadMovida(item.getCantidad());
            entrada.setStockNuevo(stockAnteriorDestino + item.getCantidad());
            entrada.setUsuario(dto.getUsuarioEmail());
            entrada.setReferencia(referencia);
            entrada.setObservacion("ENTRADA — " + observacion);
            movimientoStockRepository.save(entrada);
        }

        inventarioRepo.flush();
    }
}