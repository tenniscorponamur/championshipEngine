package be.company.fca.repository;

import be.company.fca.model.Division;
import be.company.fca.model.Equipe;
import be.company.fca.model.Poule;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PouleRepository extends CrudRepository<Poule,Long>{

    /**
     * Permet de recuperer les poules d'une division
     * @param division Division
     * @return Poules d'une division
     */
    Iterable<Poule> findByDivision(Division division);

    /**
     * Permet de supprimer les poules d'une division
     * @param division Division
     */
    void deleteByDivision(Division division);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Poule poule " +
            " set poule.allerRetour =:allerRetour " +
            " where poule.id =:pouleId")
    void updateAllerRetour(@Param("pouleId") Long pouleId,
                    @Param("allerRetour") boolean allerRetour);
}
