package pe.cibertec.inkaproductos.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pe.cibertec.inkaproductos.events.StockEventPublisher;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockEventController {

    private final StockEventPublisher publisher;

    // El frontend se suscribe aquí para recibir actualizaciones en tiempo real
    @GetMapping(value = "/eventos", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter eventos() {
        return publisher.subscribe();
    }
}
