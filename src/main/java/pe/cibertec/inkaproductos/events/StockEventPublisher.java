package pe.cibertec.inkaproductos.events;

import org.springframework.stereotype.Component;
// SSE removido — el frontend usa polling cada 5s
// Esta clase se mantiene para no romper referencias en TrasladoService
@Component
public class StockEventPublisher {
    public void publish(StockEvent event) {
        // No-op: el frontend refresca con polling
    }
}
