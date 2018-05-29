package be.company.fca.controller;

import be.company.fca.model.Championnat;
import be.company.fca.model.Division;
import be.company.fca.model.Poule;
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
        Division division = new Division();
        division.setId(divisionId);
        poule.setDivision(division);
        return pouleRepository.save(poule);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/poule/allerRetour", method = RequestMethod.PUT)
    public void updateAllerRetour(@RequestParam Long pouleId, @RequestParam boolean allerRetour){
        pouleRepository.updateAllerRetour(pouleId,allerRetour);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/poule", method = RequestMethod.DELETE)
    public void deletePoule(@RequestParam Long id){
        pouleRepository.delete(id);
    }
}
