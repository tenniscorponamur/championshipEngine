package be.company.fca.repository;

import be.company.fca.model.Match;
import be.company.fca.model.Rencontre;
import be.company.fca.model.Set;
import org.springframework.data.repository.CrudRepository;

public interface SetRepository extends CrudRepository<Set,Long> {

    /**
     * Permet de recuperer les sets d'un match
     * @param match Match
     * @return Sets d'un match
     */
    Iterable<Set> findByMatch(Match match);
}
