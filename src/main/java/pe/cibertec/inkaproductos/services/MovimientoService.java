package pe.cibertec.inkaproductos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cibertec.inkaproductos.dto.ItemCarritoDTO;
import pe.cibertec.inkaproductos.models.*;
import pe.cibertec.inkaproductos.repositories.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoService {

    private final MovimientoStockRepository kardexRepo;
    private final MovimientoRepository movRepo;
    private final MovimientoDetalleRepository detalleRepo; // ¡Faltaba esto!
    private final ProductoRepository productoRepo;
    private final AlmacenRepository almacenRepo;

    @Transactional
    public void registrarMovimientoEnKardex(
            Producto prod, Almacen origen, Almacen destino,
            Double stockAnt, Double cant, Double stockNue,
            String user, TipoOperacion tipo) {

        MovimientoStock ms = new MovimientoStock();
        ms.setProducto(prod);
        ms.setOrigen(origen);
        ms.setDestino(destino);
        ms.setStockAnterior(stockAnt);
        ms.setCantidadMovida(cant);
        ms.setStockNuevo(stockNue);
        ms.setUsuario(user);
        ms.setTipoOperacion(tipo);

        kardexRepo.save(ms);
    }

    public List<Movimiento> listarHistorial() {
        return movRepo.findAll();
    }

    public List<MovimientoDetalle> listarDetalles(Integer movId) {
        return detalleRepo.findByMovimientoId(movId);
    }
}