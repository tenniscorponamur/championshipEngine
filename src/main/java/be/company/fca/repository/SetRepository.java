package be.company.fca.repository;

import be.company.fca.model.Match;
import be.company.fca.model.Rencontre;
import be.company.fca.model.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SetRepository extends CrudRepository<Set,Long> {

    /**
     * Permet de recuperer les sets d'un match
     * @param match Match
     * @return Sets d'un match
     */
    Iterable<Set> findByMatch(Match match);

    /**
     * Permet de supprimer les sets d'un match
     * @param match Match
     * @return Sets d'un match
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    Iterable<Set> deleteByMatch(Match match);

    /**
     * Permet de supprimer tous les sets d'une rencontre
     * @param rencontreId Identifiant d'une rencontre
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "delete from set " +
            " where match_fk in (select match.id from match where match.rencontre_fk = :rencontreId)", nativeQuery = true)
    void deleteByRencontreId(@Param("rencontreId") Long rencontreId);

    /**
     * Permet de supprimer toutes les sets d'un championnat
     * @param championnatId Identifiant du championnat
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "delete from set " +
            " where match_fk in (select match.id from match inner join rencontre on match.rencontre_fk = rencontre.id inner join division on rencontre.division_fk = division.id  where division.championnat_fk = :championnatId)", nativeQuery = true)
    void deleteByChampionnatId(@Param("championnatId") Long championnatId);
}
