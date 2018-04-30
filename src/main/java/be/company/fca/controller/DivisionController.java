package be.company.fca.controller;

import be.company.fca.model.CategorieChampionnat;
import be.company.fca.model.Championnat;
import be.company.fca.model.Division;
import be.company.fca.model.TypeChampionnat;
import be.company.fca.repository.ChampionnatRepository;
import be.company.fca.repository.DivisionRepository;
import be.company.fca.service.DivisionService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des divisions")
public class DivisionController {

    @Autowired
    private ChampionnatRepository championnatRepository;

    @Autowired
    private DivisionRepository divisionRepository;

    @Autowired
    private DivisionService divisionService;

    @RequestMapping(method= RequestMethod.GET, path="/public/divisions")
    public Iterable<Division> getDivisionsByChampionnat(@RequestParam Long championnatId) {
        Championnat championnat = new Championnat();
        championnat.setId(championnatId);
        return divisionRepository.findByChampionnat(championnat);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/division", method = RequestMethod.PUT)
    public Division updateDivision(@RequestParam Long championnatId, @RequestBody Division division){
        Championnat championnat = new Championnat();
        championnat.setId(championnatId);
        division.setChampionnat(championnat);
        return divisionRepository.save(division);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/division", method = RequestMethod.POST)
    public Division addDivision(@RequestParam Long championnatId, @RequestBody Division division){

        //TODO : tester si le numero n'est pas deja present --> devrait etre une contrainte en DB mais probleme pour la renumerotation...

        //TODO : methode de modificaiton d'un championnat interdite a partir d'un certain stade du championnat (avoir un etat)

        Championnat championnat = new Championnat();
        championnat.setId(championnatId);
        division.setChampionnat(championnat);
        return divisionRepository.save(division);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/division", method = RequestMethod.DELETE)
    public void deleteDivision(@RequestParam Long id){
        divisionRepository.delete(id);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/divisions", method = RequestMethod.PUT)
    public List<Division> saveDivisionsInChampionship(@RequestParam Long championnatId, @RequestBody List<Division> divisionList){
        return divisionService.saveDivisionsInChampionship(championnatId,divisionList);
    }

    @RequestMapping(method= RequestMethod.GET, path="/public/division/createDivision")
    public Division createDivision() {
        //Iterable<Championnat> championnats = championnatRepository.findAll();
        //Championnat championnat = championnats.iterator().next();
        Division division = new Division();
        Championnat championnat = new Championnat();
        championnat.setId(1102L);
        division.setChampionnat(championnat);
        division.setNumero(2);
        division.setPointsMinimum(0);
        division.setPointsMaximum(100);
        divisionRepository.save(division);
        return division;
    }
}
