package pe.cibertec.inkaproductos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.cibertec.inkaproductos.models.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {}
