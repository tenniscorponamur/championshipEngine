package be.company.fca.repository;

import be.company.fca.model.Match;
import be.company.fca.model.Rencontre;
import org.springframework.data.repository.CrudRepository;

public interface MatchRepository extends CrudRepository<Match,Long> {

    /**
     * Permet de recuperer les matchs d'une rencontre
     * @param rencontre Rencontre
     * @return Matchs d'une rencontre
     */
    Iterable<Match> findByRencontre(Rencontre rencontre);
}
