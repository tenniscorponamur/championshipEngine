package be.company.fca.repository;

import be.company.fca.model.*;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface EquipeRepository extends CrudRepository<Equipe,Long>{

    /**
     * Permet de recuperer les equipes d'une division
     * @param division Division
     * @return Equipes d'une division
     */
    Iterable<Equipe> findByDivision(Division division);

    /**
     * Permet de recuperer les equipes d'une poule
     * @param poule Poule
     * @return Equipes d'une poule
     */
    Iterable<Equipe> findByPoule(Poule poule);

    /**
     * Permet de compter le nombre d'equipes par club
     * @param club
     * @return Nombre d'equipes par club
     */
    long countByClub(Club club);

    /**
     * Permet de compter le nombre d'equipes par terrain
     * @param terrain
     * @return Nombre d'equipes par terrain
     */
    long countByTerrain(Terrain terrain);

    /**
     * Permet de compter le nombre d'equipes par membre (capitaine)
     * @param membre
     * @return Nombre d'equipes par membre (capitaine)
     */
    long countByCapitaine(Membre membre);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Equipe equipe " +
            " set equipe.codeAlphabetique =:codeAlphabetique " +
            " where equipe.id =:equipeId")
    void updateName(@Param("equipeId") Long equipeId,
                         @Param("codeAlphabetique") String codeAlphabetique);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Equipe equipe " +
            " set equipe.poule =:poule " +
            " where equipe.id =:equipeId")
    void updatePoule(@Param("equipeId") Long equipeId,
                    @Param("poule") Poule poule);

}
