package be.company.fca.service;

import be.company.fca.model.Match;
import be.company.fca.model.Rencontre;
import org.springframework.transaction.annotation.Transactional;

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
     * Permet de recuperer ou de creer les matchs d'une rencontre s'ils n'existent pas encore
     * @param rencontre
     * @return
     */
    public List<Match> getOrCreateMatchs(Rencontre rencontre);

    /**
     * Permet de recuperer et de creer et remplir les matchs d'une rencontre s'ils n'existent pas encore
     * @param rencontre
     * @return
     */
    public List<Match> getAndFillMatchs(Rencontre rencontre);

    /**
     * Permet de supprimer toutes les rencontres d'un championnat
     * @param championnatId 
     */
    public void deleteByChampionnat(Long championnatId);

    /**
     * Permet de supprimer une rencontre
     * @param rencontreId
     */
    public void deleteById(Long rencontreId);
}
