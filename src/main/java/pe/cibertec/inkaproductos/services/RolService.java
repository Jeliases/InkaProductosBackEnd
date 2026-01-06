package pe.cibertec.inkaproductos.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.cibertec.inkaproductos.models.Rol;
import pe.cibertec.inkaproductos.repositories.RolRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;

    public List<Rol> listarTodos() {
        return rolRepository.findAll();
    }
}