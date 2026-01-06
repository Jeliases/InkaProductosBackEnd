package pe.cibertec.inkaproductos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cibertec.inkaproductos.dto.ItemCarritoDTO;
import pe.cibertec.inkaproductos.dto.TransaccionDTO;
import pe.cibertec.inkaproductos.models.*;
import pe.cibertec.inkaproductos.repositories.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoService {

    private final MovimientoRepository movRepo;
    private final MovimientoDetalleRepository detalleRepo;
    private final AlmacenRepository almacenRepo;
    private final ProductoRepository productoRepo;

    // ===========================
    // REGISTRAR MOVIMIENTO REAL
    // ===========================
    @Transactional
    public Movimiento registrarMovimiento(
            String usuario,
            Integer origenId,
            Integer destinoId,
            List<ItemCarritoDTO> items) {

        Movimiento mov = new Movimiento();
        mov.setUsuario(usuario);
        mov.setFecha(LocalDateTime.now());
        mov.setOrigen(almacenRepo.findById(origenId).orElseThrow());
        mov.setDestino(almacenRepo.findById(destinoId).orElseThrow());
        mov.setEstado(Movimiento.EstadoMovimiento.APROBADO);

        mov = movRepo.save(mov);

        for (ItemCarritoDTO item : items) {

            MovimientoDetalle md = new MovimientoDetalle();
            md.setMovimiento(mov);
            md.setProducto(productoRepo.findById(item.getProductoId()).orElseThrow());
            md.setCantidad(item.getCantidad());

            detalleRepo.save(md);
        }

        return mov;
    }

    // ===========================
    // HISTORIAL COMPLETO
    // ===========================
    public List<Movimiento> listarHistorial() {
        return movRepo.findAll();
    }

    // ===========================
    // DETALLES DE MOVIMIENTO
    // ===========================
    public List<MovimientoDetalle> listarDetalles(Integer movId) {
        return detalleRepo.findByMovimientoId(movId);
    }




}
