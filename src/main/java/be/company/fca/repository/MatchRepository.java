package be.company.fca.repository;

import be.company.fca.model.Match;
import be.company.fca.model.Membre;
import be.company.fca.model.Rencontre;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

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

    @Query(value = "select * from match inner join rencontre on match.rencontre_fk = rencontre.id " +
            "    where to_char(rencontre.dateheurerencontre,'YYYY-MM-DD') > to_char(cast(:startDate AS date),'YYYY-MM-DD') " +
            "    and to_char(rencontre.dateheurerencontre,'YYYY-MM-DD') <= to_char(cast(:endDate AS date),'YYYY-MM-DD') " +
            "    and (joueurvisites1_fk = :membreId or joueurvisites2_fk = :membreId or joueurvisiteurs1_fk = :membreId or joueurvisiteurs2_fk = :membreId)", nativeQuery = true)
    Iterable<Match> findByMembreBetweenDates(@Param("membreId") Long membreId,@Param("startDate") Date startDate, @Param("endDate") Date endDate);

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
