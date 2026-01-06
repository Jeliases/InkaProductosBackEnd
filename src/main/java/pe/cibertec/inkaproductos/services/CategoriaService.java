package pe.cibertec.inkaproductos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.cibertec.inkaproductos.models.Categoria;
import pe.cibertec.inkaproductos.repositories.CategoriaRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;


    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }


    public Categoria obtenerPorId(Integer id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));
    }
}