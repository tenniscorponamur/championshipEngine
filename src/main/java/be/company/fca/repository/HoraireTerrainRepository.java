package be.company.fca.repository;

import be.company.fca.model.HoraireTerrain;
import be.company.fca.model.Terrain;
import be.company.fca.model.TypeChampionnat;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HoraireTerrainRepository  extends CrudRepository<HoraireTerrain, Long> {

    /**
     * Permet de retrouver les horaires d'un terrain
     * @param terrain
     * @return
     */
    public List<HoraireTerrain> findByTerrain(Terrain terrain);

    /**
     * Permet de retrouver les horaires de terrain par type de championnat
     * @param typeChampionnat
     * @return
     */
    public List<HoraireTerrain> findByTypeChampionnat(TypeChampionnat typeChampionnat);

}
