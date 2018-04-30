package be.company.fca.repository;

import be.company.fca.model.Championnat;
import be.company.fca.model.Division;
import org.springframework.data.repository.CrudRepository;

public interface DivisionRepository extends CrudRepository<Division, Long> {

    /**
     * Permet de recuperer les divisions d'un championnat
     * @param championnat Championnat
     * @return Divisions d'un championnat
     */
    Iterable<Division> findByChampionnat(Championnat championnat);

}
