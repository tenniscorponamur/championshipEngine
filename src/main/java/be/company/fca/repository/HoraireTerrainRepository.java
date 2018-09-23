package be.company.fca.repository;

import be.company.fca.model.HoraireTerrain;
import be.company.fca.model.Terrain;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HoraireTerrainRepository  extends CrudRepository<HoraireTerrain, Long> {

    /**
     * Permet de retrouver les horaires d'un terrain
     * @param terrain
     * @return
     */
    public List<HoraireTerrain> findByTerrain(Terrain terrain);

}
