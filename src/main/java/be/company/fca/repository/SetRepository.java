package be.company.fca.repository;

import be.company.fca.model.Match;
import be.company.fca.model.Rencontre;
import be.company.fca.model.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
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
}
