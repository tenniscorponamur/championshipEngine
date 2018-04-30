package be.company.fca.controller;

import be.company.fca.model.CategorieChampionnat;
import be.company.fca.model.Championnat;
import be.company.fca.model.Division;
import be.company.fca.model.TypeChampionnat;
import be.company.fca.repository.ChampionnatRepository;
import be.company.fca.repository.DivisionRepository;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des divisions")
public class DivisionController {

    @Autowired
    private ChampionnatRepository championnatRepository;

    @Autowired
    private DivisionRepository divisionRepository;

    @RequestMapping(method= RequestMethod.GET, path="/public/divisions")
    public Iterable<Division> getDivisionsByChampionnat(@RequestParam Long championnatId) {
        Championnat championnat = new Championnat();
        championnat.setId(championnatId);
        return divisionRepository.findByChampionnat(championnat);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/division", method = RequestMethod.PUT)
    public Division updateDivision(@RequestParam Long championnatId, @RequestBody Division division){
        return divisionRepository.save(division);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/division", method = RequestMethod.POST)
    public Division addDivision(@RequestParam Long championnatId, @RequestBody Division division){
        return divisionRepository.save(division);
    }

    //TODO : liste de division avec nouveaux numeros
    // Changer tous les numeros en leur opposé
    // Sauvegarder les nouvelles divisions avec les nouveaux numeros
    // Supprimer les divisions qui ne sont plus presentes dans la liste recue
    // Tous les numeros auront ete changes, plus de negatif apres avoir parcouru l'ensemble
    // Gerer l'aspect transactionnel des operations pour faire tout en une fois


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
