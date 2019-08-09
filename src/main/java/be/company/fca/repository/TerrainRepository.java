package be.company.fca.repository;

import be.company.fca.model.Terrain;
import org.springframework.data.repository.CrudRepository;

public interface TerrainRepository extends CrudRepository<Terrain, Long> {

    /**
     * Permet de recuperer les terrains definis par defaut pour le criterium
     * @param terrainCriteriumParDefaut true si on veut les terrains definis par defaut pour le criterium
     * @return Terrains definis par defaut pour le criterium ou non selon parametre
     */
    Iterable<Terrain> findByTerrainCriteriumParDefaut(boolean terrainCriteriumParDefaut);
}
