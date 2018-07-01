package be.company.fca.controller;

import be.company.fca.model.*;
import be.company.fca.model.Set;
import be.company.fca.repository.*;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des classements")
public class ClassementController {


    @Autowired
    private EquipeRepository equipeRepository;

    @Autowired
    private RencontreRepository rencontreRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private SetRepository setRepository;

    @Autowired
    private DivisionRepository divisionRepository;

    @Autowired
    private PouleRepository pouleRepository;

    @RequestMapping(method= RequestMethod.GET, path="/public/classements")
    public Collection<Classement> getClassementByChampionnat(@RequestParam Long championnatId) {

        Championnat championnat = new Championnat();
        championnat.setId(championnatId);
        List<Division> divisions = (List<Division>) divisionRepository.findByChampionnat(championnat);

        Map<Poule,Classement> classements = new HashMap<Poule,Classement>();

        for (Division division : divisions){

            List<Poule> poules = (List<Poule>) pouleRepository.findByDivision(division);

            for (Poule poule : poules) {

                Classement classement = new Classement();
                classement.setPoule(poule);
                classements.put(poule,classement);
                List<Equipe> equipesPoule = (List<Equipe>) equipeRepository.findByPoule(poule);

                for (Equipe equipe : equipesPoule){
                    classement.getClassementEquipes().add(new ClassementEquipe(equipe));
                }

            }

            List<Rencontre> rencontresDivision = (List<Rencontre>) rencontreRepository.findByDivision(division);

            List<Rencontre> rencontresInterseries = new ArrayList<>();

            for (Rencontre rencontre : rencontresDivision){
                Poule poule = rencontre.getPoule();

                if (poule!=null){
                    Classement classementPoule = classements.get(poule);

                    rencontre.getPointsVisites();
                    rencontre.getPointsVisiteurs();

                    List<Match> matchs = (List<Match>) matchRepository.findByRencontre(rencontre);

                    for (Match match : matchs){
                        List<Set> sets = (List<Set>) setRepository.findByMatch(match);


                    }


                }else{
                    rencontresInterseries.add(rencontre);
                    // Si 4 poules -> 2 interseries --> 3 rencontres -> ordre dans les rencontres et les gagnants !!
                    // On va les garder de cote et boucler dessus par la suite

                    //TODO : marquer l'equipe qui gagne la division suite a la rencontre interseries

                }


                //rencontre.getGagnant/Perdant --> Equipe A/B -> getLignes et ++

            }

        }

        return classements.values();
    }


    //TODO : classement de la division basee sur la rencontre inter-series --> juste placer une etoile a cote du vainqueur de la division + popup pour expliquer ce que ca signifie

    //TODO : classement base sur les rencontres validees

    //TODO: showRencontres --> uniquement rencontres validees

    //TODO : ordonner par division et poule en partant du 1

    // Liste des equipes tries selon les points et les criteres de classement suivants : rencontre directe, sets gagnes , jeux gagnes


}
