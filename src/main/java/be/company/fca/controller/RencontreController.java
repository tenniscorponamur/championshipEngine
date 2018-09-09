package be.company.fca.controller;

import be.company.fca.dto.RencontreDto;
import be.company.fca.model.*;
import be.company.fca.repository.*;
import be.company.fca.service.ClassementService;
import be.company.fca.service.RencontreService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des rencontres")
public class RencontreController {

    @Autowired
    private RencontreRepository rencontreRepository;
    @Autowired
    private EquipeRepository equipeRepository;
    @Autowired
    private DivisionRepository divisionRepository;
    @Autowired
    private PouleRepository pouleRepository;
    @Autowired
    private ChampionnatRepository championnatRepository;

    @Autowired
    private ClassementService classementService;
    @Autowired
    private RencontreService rencontreService;

    // DTO pour les capitaines d'equipe afin de ne pas recuperer les donnees privees

    @RequestMapping(method= RequestMethod.GET, path="/public/rencontres")
    public List<RencontreDto> getRencontresByDivisionOrPoule(@RequestParam Long divisionId,@RequestParam(required = false) Long pouleId, @RequestParam(required = false) Long equipeId) {
        List<RencontreDto> rencontresDto = new ArrayList<>();
        List<Rencontre> rencontres = new ArrayList<Rencontre>();

        if (equipeId!=null){
            Equipe equipe = new Equipe();
            equipe.setId(equipeId);
            rencontres = (List<Rencontre>) rencontreRepository.findRencontresByEquipe(equipe);
        }else if (pouleId!=null){
            Poule poule = new Poule();
            poule.setId(pouleId);
            rencontres = (List<Rencontre>) rencontreRepository.findByPoule(poule);
        }else{
            Division division = new Division();
            division.setId(divisionId);
            rencontres = (List<Rencontre>) rencontreRepository.findByDivision(division);
        }

        for (Rencontre rencontre : rencontres){
            rencontresDto.add(new RencontreDto(rencontre));
        }

        return rencontresDto;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/rencontre", method = RequestMethod.POST)
    public Rencontre createRencontre(@RequestBody Rencontre rencontre){
        return rencontreRepository.save(rencontre);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/rencontre", method = RequestMethod.PUT)
    public Rencontre updateRencontre(@RequestBody Rencontre rencontre){
        return rencontreRepository.save(rencontre);
    }

    @RequestMapping(value = "/public/rencontre/{rencontreId}/isValidable", method = RequestMethod.GET)
    public boolean isRencontreValidable(@PathVariable("rencontreId") Long rencontreId){
        Rencontre rencontre = rencontreRepository.findOne(rencontreId);
        if (rencontre.getPointsVisites()!=null && rencontre.getPointsVisiteurs()!=null){
            Integer totalPoints = rencontre.getPointsVisites() + rencontre.getPointsVisiteurs();
            return totalPoints == 12;
        }
        return false;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/rencontre/{rencontreId}/validite", method = RequestMethod.PUT)
    public boolean updateValiditeRencontre(@PathVariable("rencontreId") Long rencontreId, @RequestBody boolean validite){
        if (validite) {
            if (isRencontreValidable(rencontreId)){
                rencontreRepository.updateValiditeRencontre(rencontreId,validite);
            }else{
                return false;
            }
        }else{
            rencontreRepository.updateValiditeRencontre(rencontreId,validite);
        }

        return validite;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(method= RequestMethod.GET, path="/private/rencontres/interseries")
    public List<Rencontre> getInterseries(@RequestParam Long championnatId){

        // Dans un premier temps, on ne va creer des interseries qu'avec des divisions comprenant deux poules

        /*

         TODO : gerer les matchs interseries pour des divisions a plus de deux poules :

         Lister les equipes premieres de classement de poule
         Analyser les confrontations interseries et ne conserver que les gagnants
         les perdants doivent etre conserves afin de ne pas les reprendre pour la suite de la
         competition
         Ce principe fonctionnera avec un nombre pair de poules
         Pour un nombre impair : à analyser

          */

        /*

            Parcours des divisions
            Pour chaque division, regarder
                s'il y a plusieurs poules
                si l'ensemble des rencontres ont ete validees
            Si c'est le cas, on va prendre les 1er de chaque poule pour proposer la rencontre

         */

        Championnat championnat = championnatRepository.findOne(championnatId);
        if (!championnat.isCalendrierValide() || championnat.isCloture()){
            throw new RuntimeException("Operation not supported - Championnat cloture");
        }
        List<Rencontre> rencontresInterseries = new ArrayList<>();
        List<Division> divisions = (List<Division>) divisionRepository.findByChampionnat(championnat);
        for (Division division : divisions) {

            List<Poule> poules = (List<Poule>) pouleRepository.findByDivision(division);

            // S'il y a deux poules dans la division,

            if (!poules.isEmpty()&&poules.size()==2){

                // On verifie que l'ensemble des rencontres de la division ont bien ete validees

                Long nbRencontresNonValidees = rencontreRepository.countNonValideesByDivision(division.getId());

                if (nbRencontresNonValidees==0){
                    // On recupere les classements pour cette division
                    // Par poule, on prend la premiere equipe
                    Classement classementPoule1 = classementService.getClassementPoule(poules.get(0));
                    Classement classementPoule2 = classementService.getClassementPoule(poules.get(1));

                    if (!classementPoule1.getClassementEquipes().isEmpty() && !classementPoule2.getClassementEquipes().isEmpty()){
                        Equipe equipe1 = classementPoule1.getClassementEquipes().get(0).getEquipe();
                        Equipe equipe2 = classementPoule2.getClassementEquipes().get(0).getEquipe();

                        Rencontre rencontre = new Rencontre();
                        rencontre.setDivision(division);
                        rencontre.setEquipeVisites(equipe1);
                        rencontre.setEquipeVisiteurs(equipe2);

                        if (!isInterserieExists(rencontre)){
                            rencontresInterseries.add(rencontre);
                        }

                    }

                }

            }
        }

        return rencontresInterseries;

    }

    /**
     * Permet de verifier si la rencontre interserie existe deja dans le systeme
     * @param rencontre Rencontre
     * @return
     */
    private boolean isInterserieExists(Rencontre rencontre){
        Long nbSameRencontre = rencontreRepository.countByDivisionAndEquipes(rencontre.getDivision().getId(),rencontre.getEquipeVisites().getId(),rencontre.getEquipeVisiteurs().getId());
        return nbSameRencontre>0;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(method= RequestMethod.POST, path="/private/rencontres/calendrier")
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

        List<Rencontre> rencontresSaved = rencontreService.saveRencontres(rencontres);

        // Calendrier rafraichi --> false
        championnatRepository.updateCalendrierARafraichir(championnatId,false);

        return rencontresSaved;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(method= RequestMethod.PUT, path="/private/rencontres/calendrier")
    public Iterable<Rencontre> refreshCalendrier(@RequestParam Long championnatId){

        // On peut faire un refresh tant que le calendrier n'a pas ete valide
        Championnat championnat = championnatRepository.findOne(championnatId);
        if (championnat.isCalendrierValide()){
            throw new RuntimeException("Operation not supported - Calendrier validé");
        }

        List<Rencontre> anciennesRencontres = new ArrayList<>();
        List<Rencontre> nouvellesRencontres = new ArrayList<>();
        Iterable<Division> divisionList = divisionRepository.findByChampionnat(championnat);
        for (Division division : divisionList){
            Iterable<Poule> pouleList = pouleRepository.findByDivision(division);
            for (Poule poule : pouleList){
                anciennesRencontres.addAll((Collection<? extends Rencontre>) rencontreRepository.findByPoule(poule));
                nouvellesRencontres.addAll(generateCalendar(poule));
            }
        }

        List<Rencontre> rencontresSaved = rencontreService.refreshRencontres(anciennesRencontres,nouvellesRencontres);

        // Calendrier rafraichi --> false
        championnatRepository.updateCalendrierARafraichir(championnatId,false);

        return rencontresSaved;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(method= RequestMethod.DELETE, path="/private/rencontres/calendrier")
    public void deleteCalendrier(@RequestParam Long championnatId) {
        Championnat championnat = championnatRepository.findOne(championnatId);
        if (!championnat.isCalendrierValide()){
            rencontreService.deleteByChampionnat(championnatId);
        }
    }

    /**
     * Permet de generer le calendrier d'une poule
     * @param poule Poule
     * @return Liste des rencontres d'une poule
     */
    private List<Rencontre> generateCalendar(Poule poule){

        List<Rencontre> rencontres = new ArrayList<>();

        List<Equipe> equipes = (List<Equipe>) equipeRepository.findByPoule(poule);

        if (equipes.size()<2){
            return rencontres;
        }

        int nbJournees = getNbJournees(equipes);
        int nbRencontresParJournee = equipes.size() / 2;
        //System.err.println("Nb rencontres par journee : " + nbRencontresParJournee);
        //System.err.println("Nombre de matchs à jouer :" + nbJournees*nbRencontresParJournee);

        // Decoupe en journees

        for (int i=0;i<nbJournees;i++){

            // S'il s'agit d'un nombre pair d'equipes, on va boucler sur toutes les equipes sauf la premiere
            List<Equipe> equipesReduites = new ArrayList<>(equipes.subList(1,equipes.size()));

            for (int j=0;j<nbRencontresParJournee;j++){

                Rencontre rencontre = new Rencontre();
                rencontre.setPoule(poule);
                rencontre.setDivision(poule.getDivision());
                rencontre.setNumeroJournee(i+1);

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

                // Si l'equipe visitee possede un terrain, on le precise pour la rencontre
                if (rencontre.getEquipeVisites().getTerrain()!=null){
                    rencontre.setTerrain(rencontre.getEquipeVisites().getTerrain());
                }

                //System.err.println(rencontre);
                rencontres.add(rencontre);

            }

        }

        // Si la poule est specifee avec matchs aller-retour, on va dupliquer les rencontres
        //  en inversant les equipes visites-visiteurs
        if (poule.isAllerRetour()){
            List<Rencontre> rencontresRetour = new ArrayList<>();

            for (Rencontre rencontreAller : rencontres){
                Rencontre rencontreRetour = new Rencontre();
                rencontreRetour.setPoule(poule);
                rencontreRetour.setDivision(poule.getDivision());
                rencontreRetour.setNumeroJournee(nbJournees + rencontreAller.getNumeroJournee());
                rencontreRetour.setEquipeVisites(rencontreAller.getEquipeVisiteurs());
                rencontreRetour.setEquipeVisiteurs(rencontreAller.getEquipeVisites());

                // Si l'equipe visitee possede un terrain, on le precise pour la rencontre
                if (rencontreRetour.getEquipeVisites().getTerrain()!=null){
                    rencontreRetour.setTerrain(rencontreRetour.getEquipeVisites().getTerrain());
                }

                rencontresRetour.add(rencontreRetour);
            }

            rencontres.addAll(rencontresRetour);
        }

        return rencontres;

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
