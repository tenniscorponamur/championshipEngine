package be.company.fca.repository;

import be.company.fca.model.*;
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
     * Permet de compter le nombre de rencontres par terrain
     * @param terrain
     * @return Nombre de rencontres par terrain
     */
    long countByTerrain(Terrain terrain);

    /**
     * Permet de recuperer le nombre de rencontres non-validees d'une division
     * @param divisionId Identifiant de la division
     * @return Nombre de rencontres non-validees d'une division
     */
    @Query(value = "select count(*) from rencontre " +
            " where rencontre.valide = '0' and rencontre.division_fk = :divisionId", nativeQuery = true)
    Long countNonValideesByDivision(@Param("divisionId") Long divisionId);

    /**
     * Permet de recuperer le nombre de rencontres d'un chamionnat
     * @param championnatId Identifiant du championnat
     * @return Nombre de rencontres d'un championnat
     */
    @Query(value = "select count(*) from rencontre inner join division on rencontre.division_fk = division.id  where division.championnat_fk = :championnatId ", nativeQuery = true)
    Long countByChampionnat(@Param("championnatId") Long championnatId);

    /**
     * Permet de recuperer le nombre de rencontres non-validees d'un chamionnat
     * @param championnatId Identifiant du championnat
     * @return Nombre de rencontres non-validees d'un championnat
     */
    @Query(value = "select count(*) from rencontre inner join division on rencontre.division_fk = division.id  where division.championnat_fk = :championnatId " +
            " and rencontre.valide = '0' ", nativeQuery = true)
    Long countNonValideesByChampionnat(@Param("championnatId") Long championnatId);

    /**
     * Permet de recuperer les rencontres d'un championnat
     * @param championnat Championnat
     * @return Rencontres d'un championnat
     */
    @Query("select distinct rencontre from Rencontre rencontre " +
            " where rencontre.division.championnat = :championnat")
    Iterable<Rencontre> findRencontresByChampionnat(@Param("championnat") Championnat championnat);

    /**
     * Permet de recuperer le nombre de rencontres dans une division qui oppose deux equipes donnees
     * @param divisionId Division
     * @param equipe1Id Identifiant de la premiere equipe
     * @param equipe2Id Identifiant de la seconde equipe
     * @return Nombre de rencontres dans une division qui oppose deux equipes donnees
     */
    @Query(value = "select count(*) from rencontre " +
            " where rencontre.division_fk = :divisionId and ((rencontre.visites_fk = :equipe1Id and rencontre.visiteurs_fk = :equipe2Id) or (rencontre.visites_fk = :equipe2Id and rencontre.visiteurs_fk = :equipe1Id))", nativeQuery = true)
    Long countByDivisionAndEquipes(@Param("divisionId") Long divisionId, @Param("equipe1Id") Long equipe1Id, @Param("equipe2Id") Long equipe2Id);

    /**
     * Permet de recuperer les rencontres validées d'une equipe
     * @param equipe Equipe
     * @return Rencontres validées d'une equipe
     */
    @Query("select distinct rencontre from Rencontre rencontre " +
            " where rencontre.equipeVisites =:equipe or rencontre.equipeVisiteurs = :equipe")
    Iterable<Rencontre> findRencontresByEquipe(@Param("equipe") Equipe equipe);

    /**
     * Permet de supprimer une renconttre
     * @param rencontreId Identifiant de la rencontre
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "delete from rencontre " +
            " where id = :rencontreId", nativeQuery = true)
    void deleteById(@Param("rencontreId") Long rencontreId);

    /**
     * Permet de supprimer toutes les rencontres d'un championnat
     * @param championnatId Identifiant du championnat
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "delete from rencontre " +
            " where division_fk in (select id from division where championnat_fk = :championnatId)", nativeQuery = true)
    void deleteByChampionnatId(@Param("championnatId") Long championnatId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Rencontre rencontre " +
            " set rencontre.valide =:valide" +
            " where rencontre.id =:rencontreId")
    void updateValiditeRencontre(@Param("rencontreId") Long rencontreId,
                              @Param("valide") boolean valide);

}
