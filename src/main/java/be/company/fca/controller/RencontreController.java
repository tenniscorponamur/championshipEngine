package be.company.fca.controller;

import be.company.fca.dto.RencontreDto;
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
    @RequestMapping(method= RequestMethod.GET, path="/private/rencontres/calendrier/isValidable")
    public boolean isCalendrierValidable(@RequestParam Long championnatId){
        // On peut valider le calendrier si des rencontres existent pour ce championnat
        Championnat championnat = new Championnat();
        championnat.setId(championnatId);
        List<Rencontre> rencontres = (List<Rencontre>) rencontreRepository.findRencontresByChampionnat(championnat);
        return !rencontres.isEmpty();
    }

    public boolean isCalendrierInvalidable(@RequestParam Long championnatId){
        // On peut invalider la calendrier si celui-ci est validé et qu'aucun resultat n'a ete encode
        return false;
    }

    public boolean isCalendrierDeletable(@RequestParam Long championnatId){
        // Le calendrier peut être supprimé si le calendrier n'est pas marqué comme validé
        // et que des rencontres existent (bien que non-indispensable)
        return false;
    }

    public boolean isCalendrierCloturable(@RequestParam Long championnatId){
        // Le calendrier peut etre cloture si toutes les rencontres ont ete disputees
        return false;
    }

    public List<Rencontre> createInterseries(@RequestParam Long championnatId){
        // Les rencontres interseries peuvent etre crees si toutes les rencontres des poules concernees sont encodees
        //TODO : pouvoir creer les rencontres interseries par division afin de ne pas bloquer si le reste du championnat n'est pas fini
        return new ArrayList<Rencontre>();
    }

    // TODO :
    /*
        Le refresh est faisable tant que le calendrier n'est pas valide
        Si le calendrier est valide, on peut encoder des resultats
        Le championnat peut etre marque comme cloture si toutes les rencontres ont ete encodees (interseries comprises)
        On peut devalider le calendrier tant qu'aucun resultat n'a ete encode (principe de base mais un contournement sera toujours possible)

        Si une rencontre a deja ete disputee et qu'il faut regenerer certaines rencontres (ajout d'une equipe par exemple), le refresh sera toujours potentiellement faisable

     */

    // TODO : Typiquement, on ne pourra pas supprimer une division ou une equipe tant que le calendrier existera

    // TODO : pour permettre la suppression d'une équipe, il faut envisager de supprimer toutes les rencontres de cette équipe dans le calendrier sinon blocage --> suppression de ces rencontres + refresh si le calendrier existe
    // TODO : pour permettre la suppression d'une division/poule, il faut envisager de supprimer toutes les rencontres de cette division/poule dans le calendrier sinon blocage --> suppression de ces rencontres + refresh si le calendrier existe

    //TODO : si une équipe change de poule, il faut regénérer le calendrier car il n'est plus correct --> par contre, il n'y aura aucun blocage dans l'état actuel --> initier le refresh si le calendrier existe

    //TODO : ajout d'une equipe/division/poule : initier refresh si le calendrier existe


    public Iterable<Rencontre> refreshCalendrier(@RequestParam Long championnatId){


        //TODO : tester si le calendrier a ete genere, si c'est le cas, on va faire un refresh et non une simple creation


        List<Rencontre> anciennesRencontres = new ArrayList<>();
        List<Rencontre> nouvellesRencontres = new ArrayList<>();

        Championnat championnat = new Championnat();
        championnat.setId(championnatId);
        Iterable<Division> divisionList = divisionRepository.findByChampionnat(championnat);
        for (Division division : divisionList){
            Iterable<Poule> pouleList = pouleRepository.findByDivision(division);
            for (Poule poule : pouleList){
                anciennesRencontres.addAll((Collection<? extends Rencontre>) rencontreRepository.findByPoule(poule));
                nouvellesRencontres.addAll(generateCalendar(poule));
            }
        }

        return rencontreService.refreshRencontres(anciennesRencontres,nouvellesRencontres);
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
