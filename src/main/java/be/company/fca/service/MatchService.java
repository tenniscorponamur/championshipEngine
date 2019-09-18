package be.company.fca.service;

import be.company.fca.model.Match;
import be.company.fca.model.Rencontre;
import be.company.fca.model.Set;

import java.util.List;

public interface MatchService {

    /**
     * Permet de sauvegarder un match et ses sets
     * @param matchId identifiant du match
     * @param sets
     * @return
     */
    public Match updateMatchAndSets(Long matchId,boolean setUnique, List<Set> sets);

    /**
     * Permet de connaitre le nombre de jeux autorises dans un set
     * lors d'un match
     * Le nombre de jeux maximal a ete adapte au fil des reglements
     * et selon les types de championnat, d'où l'existence de cette methode
     * @param match
     * @return
     */
    public Integer getNbJeuxMax(Match match);

    /**
     * Permet de creer les matchs "vides" d'une rencontre
     * @param rencontre Rencontre
     * @return matchs "vides" pour une rencontre
     */
    public List<Match> createMatchs(Rencontre rencontre);
}
