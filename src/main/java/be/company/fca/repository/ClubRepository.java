package be.company.fca.repository;

import be.company.fca.model.Club;
import be.company.fca.model.Terrain;
import org.springframework.data.repository.CrudRepository;

public interface ClubRepository extends CrudRepository<Club, Long> {

    /**
     * Permet de compter le nombre de clubs par terrain
     * @param terrain
     * @return Nombre de clubs par terrain
     */
    long countByTerrain(Terrain terrain);

}
