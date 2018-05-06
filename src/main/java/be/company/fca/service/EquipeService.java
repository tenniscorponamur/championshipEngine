package be.company.fca.service;

import be.company.fca.model.Equipe;

import java.util.List;

public interface EquipeService {

    /**
     * Permet de sauvegarder les noms des equipes
     * @param equipeList
     * @return
     */
    public List<Equipe> updateEquipeNames(List<Equipe> equipeList);
}
