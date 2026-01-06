package pe.cibertec.inkaproductos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.cibertec.inkaproductos.models.Almacen;
import pe.cibertec.inkaproductos.repositories.AlmacenRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlmacenService {

    private final AlmacenRepository almacenRepository;

    public List<Almacen> listarTodos() {
        return almacenRepository.findAll();
    }

    public Almacen obtenerPorId(Integer id) {
        return almacenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado"));
    }
}