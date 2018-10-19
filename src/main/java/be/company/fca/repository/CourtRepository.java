package be.company.fca.repository;

import be.company.fca.model.Court;
import be.company.fca.model.Terrain;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CourtRepository extends CrudRepository<Court, Long> {

    /**
     * Permet de retrouver les courts d'un terrain
     * @param terrain
     * @return
     */
    public List<Court> findByTerrain(Terrain terrain);

}
