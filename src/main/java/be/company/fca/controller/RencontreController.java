package be.company.fca.controller;

import be.company.fca.model.*;
import be.company.fca.repository.DivisionRepository;
import be.company.fca.repository.EquipeRepository;
import be.company.fca.repository.PouleRepository;
import be.company.fca.repository.RencontreRepository;
import be.company.fca.service.RencontreService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    private RencontreService rencontreService;

    @RequestMapping(method= RequestMethod.GET, path="/public/rencontres")
    public Iterable<Rencontre> getRencontresByDivisionOrPoule(@RequestParam Long divisionId,@RequestParam(required = false) Long pouleId, @RequestParam(required = false) Long equipeId) {
        if (equipeId!=null){
            Equipe equipe = new Equipe();
            equipe.setId(equipeId);
            return rencontreRepository.findRencontresByEquipe(equipe);
        }else if (pouleId!=null){
            Poule poule = new Poule();
            poule.setId(pouleId);
            return rencontreRepository.findByPoule(poule);
        }else{
            Division division = new Division();
            division.setId(divisionId);
            return rencontreRepository.findByDivision(division);
        }
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

        return rencontreService.saveRencontres(rencontres);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(method= RequestMethod.DELETE, path="/private/rencontres/calendrier")
    public void deleteCalendrier(@RequestParam Long championnatId) {
        rencontreService.deleteByChampionnat(championnatId);
    }

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
