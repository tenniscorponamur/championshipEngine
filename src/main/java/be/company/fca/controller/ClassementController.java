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

        Map<Poule,Classement> classementMap = new HashMap<Poule,Classement>();

        for (Division division : divisions){

            List<Poule> poules = (List<Poule>) pouleRepository.findByDivision(division);

            for (Poule poule : poules) {

                Classement classement = new Classement();
                classement.setPoule(poule);
                classementMap.put(poule,classement);
                List<Equipe> equipesPoule = (List<Equipe>) equipeRepository.findByPoule(poule);

                for (Equipe equipe : equipesPoule){
                    classement.getClassementEquipes().add(new ClassementEquipe(equipe));
                }

            }

            List<Rencontre> rencontresDivision = (List<Rencontre>) rencontreRepository.findByDivision(division);

            List<Rencontre> rencontresInterseries = new ArrayList<>();

            for (Rencontre rencontre : rencontresDivision){

                if (rencontre.isValide()){

                    Poule poule = rencontre.getPoule();

                    if (poule!=null){
                        Classement classementPoule = classementMap.get(poule);

                        ClassementEquipe classementEquipeVisites = classementPoule.findByEquipe(rencontre.getEquipeVisites());
                        ClassementEquipe classementEquipeVisiteurs = classementPoule.findByEquipe(rencontre.getEquipeVisiteurs());

                        int pointsVisites = 0;
                        int pointsVisiteurs = 0;

                        if (rencontre.getPointsVisites()!=null && rencontre.getPointsVisiteurs()!=null){
                            if (rencontre.getPointsVisites() > rencontre.getPointsVisiteurs()){
                                pointsVisites = 2;
                                // On conserve la liste des equipes battues
                                classementEquipeVisites.getEquipesBattues().add(rencontre.getEquipeVisiteurs());

                            }else if (rencontre.getPointsVisites() < rencontre.getPointsVisiteurs()){
                                pointsVisiteurs = 2;
                                // On conserve la liste des equipes battues
                                classementEquipeVisiteurs.getEquipesBattues().add(rencontre.getEquipeVisites());
                            }else{
                                pointsVisites = 1;
                                pointsVisiteurs = 1;
                            }
                        }

                        // Ajout du match joue

                        classementEquipeVisites.ajoutMatchJoue();
                        classementEquipeVisiteurs.ajoutMatchJoue();

                        // Ajout des points obtenus

                        classementEquipeVisites.ajoutPoints(pointsVisites);
                        classementEquipeVisiteurs.ajoutPoints(pointsVisiteurs);

                        // Ajout des sets gagnes et perdus

                        classementEquipeVisites.ajoutSetsGagnes(rencontre.getPointsVisites());
                        classementEquipeVisiteurs.ajoutSetsGagnes(rencontre.getPointsVisiteurs());

                        classementEquipeVisites.ajoutSetsPerdus(rencontre.getPointsVisiteurs());
                        classementEquipeVisiteurs.ajoutSetsPerdus(rencontre.getPointsVisites());

                        // Recuperation des jeux gagnes et perdus en analysant les resultats des sets

                        List<Match> matchs = (List<Match>) matchRepository.findByRencontre(rencontre);

                        for (Match match : matchs){
                            List<Set> sets = (List<Set>) setRepository.findByMatch(match);
                            for (Set set : sets){
                                classementEquipeVisites.ajoutJeuxGagnes(set.getJeuxVisites());
                                classementEquipeVisiteurs.ajoutJeuxGagnes(set.getJeuxVisiteurs());

                                classementEquipeVisites.ajoutJeuxPerdus(set.getJeuxVisiteurs());
                                classementEquipeVisiteurs.ajoutJeuxPerdus(set.getJeuxVisites());
                            }

                        }

                    }else{
                        rencontresInterseries.add(rencontre);
                        // Si 4 poules -> 2 interseries --> 3 rencontres -> ordre dans les rencontres et les gagnants !!
                        // On va les garder de cote et boucler dessus par la suite

                        //TODO : marquer l'equipe qui gagne la division suite a la rencontre interseries

                    }
                }

            }

        }

        // Trier les classements selon divisions/poules par numero

        List<Classement> classements = new ArrayList<>();
        for (Classement classement : classementMap.values()){

             // Tri des equipes sur base des points obtenus (et les autres criteres par la suite - rencontre directe - sets gagnes - jeux gagnes

            Collections.sort(classement.getClassementEquipes(), new Comparator<ClassementEquipe>() {
                @Override
                public int compare(ClassementEquipe classementEquipeA, ClassementEquipe classementEquipeB) {

                    // Premier critere, les points

                    int comparePoints = (-1) * Integer.compare(classementEquipeA.getPoints(),classementEquipeB.getPoints());

                    if (comparePoints!=0){
                        return comparePoints;
                    }else{

                        // Second critere, les confrontations directes

                        boolean equipeAwinsAgainstEquipeB = classementEquipeA.getEquipesBattues().contains(classementEquipeB.getEquipe());
                        boolean equipeBwinsAgainstEquipeA = classementEquipeB.getEquipesBattues().contains(classementEquipeA.getEquipe());

                        if (equipeAwinsAgainstEquipeB && !equipeBwinsAgainstEquipeA){
                            return -1;
                        } else if (!equipeAwinsAgainstEquipeB && equipeBwinsAgainstEquipeA){
                            return 1;
                        } else {

                            // Troisieme critere : les sets gagnes

                            int compareSetsGagnes = (-1) * Integer.compare(classementEquipeA.getSetsGagnes(),classementEquipeB.getSetsGagnes());

                            if (compareSetsGagnes!=0) {
                                return compareSetsGagnes;
                            }else{

                                // Quatrieme critere : les sets perdus

                                int compareSetsPerdus = Integer.compare(classementEquipeA.getSetsPerdus(),classementEquipeB.getSetsPerdus());

                                if (compareSetsPerdus!=0) {
                                    return compareSetsPerdus;
                                }else{

                                    // Cinquieme critere : les jeux perdus

                                    return Integer.compare(classementEquipeA.getJeuxPerdus(),classementEquipeB.getJeuxPerdus());

                                }
                            }
                        }
                    }
                }
            });

            classements.add(classement);
        }

        Collections.sort(classements, new Comparator<Classement>() {
            @Override
            public int compare(Classement classementA, Classement classementB) {
                int compareDivision = classementA.getPoule().getDivision().getNumero().compareTo(classementB.getPoule().getDivision().getNumero());
                if (compareDivision!=0){
                    return compareDivision;
                }else{
                    int comparePoule = classementA.getPoule().getNumero().compareTo(classementB.getPoule().getNumero());
                    return comparePoule;
                }
            }
        });

        return classements;
    }

    //TODO : classement de la division basee sur la rencontre inter-series --> juste placer une etoile a cote du vainqueur de la division + popup pour expliquer ce que ca signifie

    //TODO : showRencontres dans ecran ne doit afficher que les rencontres validees


    /**
     * Extrait du reglement :
     * --------------------
     *
     * Article 8 : Classement des équipes.

     8.1. Le classement final s'établit par ordre décroissant des points obtenus. En cas d'égalité dans les points, l'ordre de classement est déterminé comme suit :
     Si une équipe lauréate a gagné une rencontre par forfait, cette dernière est retirée du championnat parmi toutes les équipes entrant en compétition pour le départage
     a) si 2 équipes à égalité : le résultat de la rencontre entre les 2 équipes.
     b) Si plus de 2 équipes à égalité :seul les résultats entre ces équipes sont pris en considération pour déterminer le classement définitif en observant les critères suivants
     1) le plus grand nombre de points
     2) le plus grand nombre de victoires (en simples et en doubles)
     3) le plus petit nombre de sets perdus.( en simple et en doubles)
     4) le plus petit nombre de jeux perdus (en simple et en doubles)
     Si après ces critères, 2 équipes se trouvent toujours à égalité, le départage se fait suivant la règle du point 8.1.a
     En cas d’égalité totale après tous ces critères, le tirage au sort donnera le vainqueur final.

     8.2. Les équipes premières des différentes séries d'une division se rencontrent pour se départager. La commission sportive fixe la date, l'heure et l'endroit des rencontres inter séries et en détermine les règles particulières.

     8.3. Est déclarée championne de sa division l'équipe qui s'est classée première de sa division ou qui remporte la rencontre inter séries selon l'article 8.2.

     */

}