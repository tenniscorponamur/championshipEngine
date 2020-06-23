package be.company.fca.service;

import be.company.fca.model.Match;
import be.company.fca.model.Rencontre;

import java.util.List;

public interface RencontreService {

    /**
     * Permet de sauvegarder plusieurs rencontres en une seule transaction
     * Typiquement pour creer le calendrier d'un championnat
     * @param rencontreList
     * @return
     */
    public List<Rencontre> saveRencontres(List<Rencontre> rencontreList);

    /**
     * Permet de rafraichir les rencontres d'une poule en une seule transaction.
     * Les anciennes rencontres seront supprimées au profit de nouvelles
     * apres recuperation d'informations dans les anciennes
     * @param oldRencontreList Ancienne liste des rencontres
     * @param newRencontreList Nouvelle liste des rencontres
     * @return Liste des rencontres sauvegardees
     */
    public List<Rencontre> refreshRencontres(List<Rencontre> oldRencontreList, List<Rencontre> newRencontreList);

    /**
     * Permet de charger les joueurs/joueuses definis dans la composition des equipes
     * des matchs de la rencontre concerne
     * @param rencontre Rencontre concernee
     * @param matchs Matchs de la rencontre
     *
     */
    public void loadCompositions(Rencontre rencontre, List<Match> matchs);

    /**
     * Permet de supprimer toutes les rencontres d'un championnat
     * @param championnatId 
     */
    public void deleteByChampionnat(Long championnatId);
}
