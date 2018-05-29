package be.company.fca.service;

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
     * Permet de supprimer toutes les rencontres d'un championnat
     * @param championnatId 
     */
    public void deleteByChampionnat(Long championnatId);
}
