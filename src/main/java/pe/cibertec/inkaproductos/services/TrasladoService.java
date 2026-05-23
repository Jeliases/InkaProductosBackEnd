package pe.cibertec.inkaproductos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cibertec.inkaproductos.dto.ItemTrasladoDTO;
import pe.cibertec.inkaproductos.dto.KardexDTO;
import pe.cibertec.inkaproductos.dto.TrasladoRequest;
import pe.cibertec.inkaproductos.events.StockEvent;
import pe.cibertec.inkaproductos.events.StockEventPublisher;
import pe.cibertec.inkaproductos.models.*;
import pe.cibertec.inkaproductos.repositories.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class TrasladoService {

    private final InventarioRepository    inventarioRepo;
    private final MovimientoStockRepository kardexRepo;
    private final AlmacenRepository       almacenRepo;
    private final ProductoRepository      productoRepo;
    private final StockEventPublisher publisher;

    private static final AtomicInteger SEQ = new AtomicInteger(1);

    // ── PROCESAR TRASLADO (ADMIN directo) ───────────────────────
    @Transactional
    public void procesar(TrasladoRequest req, String usuarioEmail) {

        Almacen origen  = almacenRepo.findById(req.getOrigenId())
                .orElseThrow(() -> new RuntimeException("Almacén origen no encontrado"));
        Almacen destino = almacenRepo.findById(req.getDestinoId())
                .orElseThrow(() -> new RuntimeException("Almacén destino no encontrado"));

        if (origen.getAlmacenId().equals(destino.getAlmacenId()))
            throw new RuntimeException("Origen y destino no pueden ser el mismo almacén");

        String ref = generarReferencia(req.getReferencia());
        String obs = req.getObservacion() != null ? req.getObservacion()
                   : "Traslado " + origen.getNombre() + " → " + destino.getNombre();

        for (ItemTrasladoDTO item : req.getItems()) {
            moverItem(origen, destino, item, usuarioEmail, ref, obs);
        }
    }

    private void moverItem(Almacen origen, Almacen destino,
                            ItemTrasladoDTO item, String email,
                            String ref, String obs) {

        Producto producto = productoRepo.findById(item.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto " + item.getProductoId() + " no encontrado"));

        // Leer stock ANTES del movimiento (para kardex)
        BigDecimal stockOrigenAntes = inventarioRepo
                .findByAlmacenAndProducto(origen.getAlmacenId(), item.getProductoId())
                .map(Inventario::getCantidad)
                .orElseThrow(() -> new RuntimeException(
                        "El producto '" + producto.getNombre() + "' no existe en el almacén origen"));

        BigDecimal stockDestinoAntes = inventarioRepo
                .findByAlmacenAndProducto(destino.getAlmacenId(), item.getProductoId())
                .map(Inventario::getCantidad)
                .orElse(BigDecimal.ZERO);

        // Validar stock suficiente
        if (stockOrigenAntes.compareTo(item.getCantidad()) < 0)
            throw new RuntimeException(
                    "Stock insuficiente para '" + producto.getNombre() +
                    "'. Disponible: " + stockOrigenAntes + ", solicitado: " + item.getCantidad());

        // Actualizar origen (resta) — query directa, atómica
        int updOrigen = inventarioRepo.actualizarCantidad(
                origen.getAlmacenId(), item.getProductoId(), item.getCantidad().negate());

        if (updOrigen == 0)
            throw new RuntimeException("No se pudo actualizar stock del origen");

        // Actualizar destino (suma) — si no existe la fila, crearla primero
        int updDestino = inventarioRepo.actualizarCantidad(
                destino.getAlmacenId(), item.getProductoId(), item.getCantidad());

        if (updDestino == 0) {
            // El producto no existía en destino — crear registro
            crearInventario(destino, producto, item.getCantidad());
        }

        // Kardex SALIDA (origen)
        kardexRepo.save(buildKardex(TipoOperacion.TRASLADO, producto, origen, destino,
                stockOrigenAntes, item.getCantidad(),
                stockOrigenAntes.subtract(item.getCantidad()),
                email, ref, "SALIDA — " + obs));

        // Kardex ENTRADA (destino)
        kardexRepo.save(buildKardex(TipoOperacion.TRASLADO, producto, origen, destino,
                stockDestinoAntes, item.getCantidad(),
                stockDestinoAntes.add(item.getCantidad()),
                email, ref, "ENTRADA — " + obs));

        // Emitir eventos SSE para tiempo real
        publisher.publish(new StockEvent(origen.getAlmacenId(), item.getProductoId(),
                stockOrigenAntes.subtract(item.getCantidad()), "TRASLADO"));
        publisher.publish(new StockEvent(destino.getAlmacenId(), item.getProductoId(),
                stockDestinoAntes.add(item.getCantidad()), "TRASLADO"));
    }

    private void crearInventario(Almacen almacen, Producto producto, BigDecimal cantidad) {
        InventarioId id = new InventarioId(almacen.getAlmacenId(), producto.getProductoId());
        Inventario inv = new Inventario();
        inv.setId(id);
        inv.setAlmacen(almacen);
        inv.setProducto(producto);
        inv.setCantidad(cantidad);
        inventarioRepo.save(inv);
    }

    private MovimientoStock buildKardex(TipoOperacion tipo, Producto producto,
                                         Almacen origen, Almacen destino,
                                         BigDecimal anterior, BigDecimal cantidad,
                                         BigDecimal nuevo, String usuario,
                                         String ref, String obs) {
        MovimientoStock ms = new MovimientoStock();
        ms.setTipoOperacion(tipo);
        ms.setProducto(producto);
        ms.setOrigen(origen);
        ms.setDestino(destino);
        ms.setStockAnterior(anterior);
        ms.setCantidadMovida(cantidad);
        ms.setStockNuevo(nuevo);
        ms.setUsuario(usuario);
        ms.setReferencia(ref);
        ms.setObservacion(obs);
        return ms;
    }

    private String generarReferencia(String ref) {
        if (ref != null && !ref.isBlank()) return ref;
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "TRL-" + fecha + "-" + String.format("%03d", SEQ.getAndIncrement());
    }

    // ── KARDEX ──────────────────────────────────────────────────
    public List<KardexDTO> kardexPorProducto(Integer productoId) {
        return kardexRepo.kardexPorProducto(productoId).stream()
                .map(this::toKardexDTO).toList();
    }

    public List<KardexDTO> kardexPorAlmacen(Integer almacenId) {
        return kardexRepo.kardexPorAlmacen(almacenId).stream()
                .map(this::toKardexDTO).toList();
    }

    private KardexDTO toKardexDTO(MovimientoStock ms) {
        KardexDTO dto = new KardexDTO();
        dto.setMovimientoId(ms.getMovimientoId());
        dto.setFecha(ms.getFecha());
        dto.setTipoOperacion(ms.getTipoOperacion().name());
        dto.setSku(ms.getProducto().getSku());
        dto.setProducto(ms.getProducto().getNombre());
        dto.setAlmacenOrigen(ms.getOrigen().getNombre());
        dto.setAlmacenDestino(ms.getDestino().getNombre());
        dto.setStockAnterior(ms.getStockAnterior());
        dto.setCantidadMovida(ms.getCantidadMovida());
        dto.setStockNuevo(ms.getStockNuevo());
        dto.setUsuario(ms.getUsuario());
        dto.setReferencia(ms.getReferencia());
        dto.setObservacion(ms.getObservacion());
        return dto;
    }
}
