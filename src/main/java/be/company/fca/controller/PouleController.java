package be.company.fca.controller;

import be.company.fca.model.Championnat;
import be.company.fca.model.Division;
import be.company.fca.model.Poule;
import be.company.fca.repository.ChampionnatRepository;
import be.company.fca.repository.DivisionRepository;
import be.company.fca.repository.PouleRepository;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des poules")
public class PouleController {

    @Autowired
    private PouleRepository pouleRepository;

    @Autowired
    private DivisionRepository divisionRepository;

    @Autowired
    private ChampionnatRepository championnatRepository;

    @RequestMapping(method= RequestMethod.GET, path="/public/poules")
    public Iterable<Poule> getPoulesByDivision(@RequestParam Long divisionId) {
        Division division = new Division();
        division.setId(divisionId);
        return pouleRepository.findByDivision(division);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/poule", method = RequestMethod.PUT)
    public Poule updatePoule(@RequestParam Long divisionId, @RequestBody Poule poule){
        Division division = new Division();
        division.setId(divisionId);
        poule.setDivision(division);
        return pouleRepository.save(poule);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/poule", method = RequestMethod.POST)
    public Poule addPoule(@RequestParam Long divisionId, @RequestBody Poule poule){

        Division division = divisionRepository.findById(divisionId).get();
        poule.setDivision(division);

        // Operation non-permise si le calendrier est valide ou cloture
        if (division.getChampionnat().isCalendrierValide() || division.getChampionnat().isCloture()){
            throw new RuntimeException("Operation not supported - Calendrier valide ou championnat cloture");
        }

        Poule pouleSaved = pouleRepository.save(poule);

        // On signale que le calendrier doit etre rafraichi si la poule a ete sauvee
        championnatRepository.updateCalendrierARafraichir(division.getChampionnat().getId(),true);

        return pouleSaved;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/poule/allerRetour", method = RequestMethod.PUT)
    public void updateAllerRetour(@RequestParam Long pouleId, @RequestParam boolean allerRetour){

        Poule poule = pouleRepository.findById(pouleId).get();

        // Operation non-permise si le calendrier est valide ou cloture
        if (poule.getDivision().getChampionnat().isCalendrierValide() || poule.getDivision().getChampionnat().isCloture()){
            throw new RuntimeException("Operation not supported - Calendrier valide ou championnat cloture");
        }

        pouleRepository.updateAllerRetour(pouleId,allerRetour);


        // On signale que le calendrier doit etre rafraichi si la poule a ete sauvee
        championnatRepository.updateCalendrierARafraichir(poule.getDivision().getChampionnat().getId(),true);

    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/poule", method = RequestMethod.DELETE)
    public void deletePoule(@RequestParam Long id){
        Poule poule = pouleRepository.findById(id).get();

        // Operation non-permise si le calendrier est valide ou cloture
        if (poule.getDivision().getChampionnat().isCalendrierValide() || poule.getDivision().getChampionnat().isCloture()){
            throw new RuntimeException("Operation not supported - Calendrier valide ou championnat cloture");
        }

        pouleRepository.deleteById(id);
        // On signale que le calendrier doit etre rafraichi si la poule a ete supprimee
        championnatRepository.updateCalendrierARafraichir(poule.getDivision().getChampionnat().getId(),true);
    }
}
