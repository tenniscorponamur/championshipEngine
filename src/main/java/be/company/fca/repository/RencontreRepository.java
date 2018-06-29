package be.company.fca.repository;

import be.company.fca.model.Division;
import be.company.fca.model.Rencontre;
import be.company.fca.model.Poule;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RencontreRepository extends CrudRepository<Rencontre,Long> {

    /**
     * Permet de recuperer les rencontres d'une division
     * @param division Division
     * @return Rencontres d'une division
     */
    Iterable<Rencontre> findByDivision(Division division);

    /**
     * Permet de recuperer les rencontres d'une poule
     * @param poule Poule
     * @return Rencontres d'une poule
     */
    Iterable<Rencontre> findByPoule(Poule poule);

    /**
     * Permet de supprimer les rencontres d'une division
     * @param division Division
     * @return Rencontres d'une division
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    Iterable<Rencontre> deleteByDivision(Division division);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Rencontre rencontre " +
            " set rencontre.valide =:valide" +
            " where rencontre.id =:rencontreId")
    void updateValiditeRencontre(@Param("rencontreId") Long rencontreId,
                              @Param("valide") boolean valide);

}
