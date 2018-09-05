package be.company.fca.service;

import be.company.fca.model.Championnat;
import be.company.fca.model.Division;

import java.util.List;

public interface DivisionService {

    /**
     * Permet de sauvegarder une liste de divisions dans un championnat
     * Typiquement utilise pour renumeroter les differentes divisions sur base des points maximum des differentes divisions
     * @param championnat
     * @param divisionList
     * @return
     */
    public List<Division> saveDivisionsInChampionship(Championnat championnat, List<Division> divisionList);

}
