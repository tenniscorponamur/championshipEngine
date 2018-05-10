package be.company.fca.repository;

import be.company.fca.model.Championnat;
import be.company.fca.model.Division;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface DivisionRepository extends CrudRepository<Division, Long> {

    /**
     * Permet de recuperer les divisions d'un championnat
     * @param championnat Championnat
     * @return Divisions d'un championnat
     */
    Iterable<Division> findByChampionnat(Championnat championnat);

    /**
     * Permet de supprimer les divisions d'un championnat
     * @param championnat Championnat
     * @return Divisions d'un championnat
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    Iterable<Division> deleteByChampionnat(Championnat championnat);

}
