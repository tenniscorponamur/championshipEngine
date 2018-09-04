package be.company.fca.repository;

import be.company.fca.model.Match;
import be.company.fca.model.Rencontre;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface MatchRepository extends CrudRepository<Match,Long> {

    /**
     * Permet de recuperer les matchs d'une rencontre
     * @param rencontre Rencontre
     * @return Matchs d'une rencontre
     */
    Iterable<Match> findByRencontre(Rencontre rencontre);

    @Transactional
    @Modifying(clearAutomatically = true)
    Iterable<Match> deleteByRencontre(Rencontre rencontre);

    /**
     * Permet de supprimer tous les matchs d'un championnat
     * @param championnatId Identifiant du championnat
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "delete from match " +
            " where rencontre_fk in (select rencontre.id from rencontre inner join division on rencontre.division_fk = division.id  where division.championnat_fk = :championnatId)", nativeQuery = true)
    void deleteByChampionnatId(@Param("championnatId") Long championnatId);
}
