package pe.cibertec.inkaproductos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cibertec.inkaproductos.models.MensajeTi;
import pe.cibertec.inkaproductos.repositories.MensajeTiRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MensajeTiService {

    private final MensajeTiRepository mensajeTiRepository;

    public List<MensajeTi> obtenerPendientes() {
        return mensajeTiRepository.findByEstado("PENDIENTE");
    }

    @Transactional
    public MensajeTi atenderTicket(Integer id) {
        MensajeTi ticket = mensajeTiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        ticket.setEstado("ATENDIDO");
        ticket.setFechaAtencion(LocalDateTime.now()); // Registro automático de atención
        return mensajeTiRepository.save(ticket);
    }

    public MensajeTi enviarSolicitud(MensajeTi mensaje) {
        mensaje.setEstado("PENDIENTE");
        return mensajeTiRepository.save(mensaje);
    }
}