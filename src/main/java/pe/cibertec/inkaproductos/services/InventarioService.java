package pe.cibertec.inkaproductos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.cibertec.inkaproductos.models.Inventario;
import pe.cibertec.inkaproductos.repositories.InventarioRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioRepository inventarioRepository;

    public List<Inventario> listarPorAlmacen(Integer idAlmacen) {
        return inventarioRepository.listarPorAlmacen(idAlmacen);
    }

    public List<Inventario> filtrar(Integer almacenId, Integer categoriaId) {
        return inventarioRepository.filtrarInventario(almacenId, categoriaId);
    }
}