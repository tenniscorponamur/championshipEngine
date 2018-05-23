package be.company.fca.controller;

import be.company.fca.model.*;
import be.company.fca.repository.DivisionRepository;
import be.company.fca.repository.EquipeRepository;
import be.company.fca.repository.PouleRepository;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des rencontres")
public class RencontreController {

    @Autowired
    private EquipeRepository equipeRepository;
    @Autowired
    private DivisionRepository divisionRepository;
    @Autowired
    private PouleRepository pouleRepository;


    @RequestMapping(method= RequestMethod.POST, path="/private/rencontres/createCalendrier")
    public Iterable<Rencontre> createCalendrier(@RequestParam Long championnatId) {

        List<Rencontre> rencontres = new ArrayList<Rencontre>();

        Championnat championnat = new Championnat();
        championnat.setId(championnatId);
        Iterable<Division> divisionList = divisionRepository.findByChampionnat(championnat);
        for (Division division : divisionList){
            Iterable<Poule> pouleList = pouleRepository.findByDivision(division);
            for (Poule poule : pouleList){

                rencontres.addAll(generateCalendar(poule));

            }
        }


        return rencontres;
    }



    private List<Rencontre> generateCalendar(Poule poule){

        List<Rencontre> rencontres = new ArrayList<>();

        List<Equipe> equipes = (List<Equipe>) equipeRepository.findByPoule(poule);

        if (equipes.size()<2){
            return rencontres;
        }

        int nbJournees = getNbJournees(equipes);
        int nbRencontresParJournee = equipes.size() / 2;
        System.err.println("Nb rencontres par journee : " + nbRencontresParJournee);
        System.err.println("Nombre de matchs à jouer :" + nbJournees*nbRencontresParJournee);

        // Decoupe en journees

        for (int i=0;i<nbJournees;i++){

            // S'il s'agit d'un nombre pair d'equipes, on va boucler sur toutes les equipes sauf la premiere
            List<Equipe> equipesReduites = new ArrayList<>(equipes.subList(1,equipes.size()));

            for (int j=0;j<nbRencontresParJournee;j++){

                Rencontre rencontre = new Rencontre();
                rencontre.setPoule(poule);
                rencontre.setDivision(poule.getDivision());
                rencontre.setNumeroJournee(i);

                // Pour un nombre d'equipes impair, permutation circulaire parmi toutes les equipes --> l'une sera bye via la procedure
                if (nombreEquipesImpair(equipes)){
                    rencontre.setEquipeVisites(equipes.get((0+j+i)%equipes.size()));
                    rencontre.setEquipeVisiteurs(equipes.get( (equipes.size() - 1 - j + i)%equipes.size() ));
                }else{
                    // Pour un nombre d'equipes pair,
                    // On va garder la premiere equipe fixe et faire tourner les autres
                    if (j==0){
                        rencontre.setEquipeVisites(equipes.get(0));
                        rencontre.setEquipeVisiteurs(equipesReduites.get( (equipesReduites.size() - 1 - j + i) % equipesReduites.size() ));
                    }else{
                        rencontre.setEquipeVisites(equipesReduites.get( (equipesReduites.size()-1 + i + j) % equipesReduites.size()));
                        rencontre.setEquipeVisiteurs(equipesReduites.get( (equipesReduites.size() - 1 - j + i) % equipesReduites.size() ));
                    }
                }
                System.err.println(rencontre);
                rencontres.add(rencontre);

            }

        }

        return rencontres;

        // TODO : Aller-retour == dupliquer les rencontres en inversant visites-visiteurs

    }

    private boolean nombreEquipesImpair(List<Equipe> equipes){
        return equipes.size()%2!=0;
    }

    private int getNbJournees(List<Equipe> equipes){
        if (nombreEquipesImpair(equipes)){
            return equipes.size();
        }else{
            return equipes.size()-1;
        }
    }

}
