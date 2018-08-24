package be.company.fca.controller;

import be.company.fca.model.Club;
import be.company.fca.model.Division;
import be.company.fca.model.Equipe;
import be.company.fca.model.Poule;
import be.company.fca.repository.EquipeRepository;
import be.company.fca.service.DivisionService;
import be.company.fca.service.EquipeService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des equipes")
public class EquipeController {

    @Autowired
    private EquipeRepository equipeRepository;

    @Autowired
    private EquipeService equipeService;

    // TODO : DTO pour les capitaines d'equipe

    @RequestMapping(method= RequestMethod.GET, path="/public/equipes")
    public Iterable<Equipe> getEquipesByDivisionOrPoule(@RequestParam Long divisionId,@RequestParam(required = false) Long pouleId) {
        if (pouleId!=null){
            Poule poule = new Poule();
            poule.setId(pouleId);
            return equipeRepository.findByPoule(poule);
        }else{
            Division division = new Division();
            division.setId(divisionId);
            return equipeRepository.findByDivision(division);
        }
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/equipe", method = RequestMethod.PUT)
    public Equipe updateEquipe(@RequestParam Long divisionId, @RequestBody Equipe equipe){
        Division division = new Division();
        division.setId(divisionId);
        equipe.setDivision(division);
        return equipeRepository.save(equipe);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/equipe", method = RequestMethod.POST)
    public Equipe addEquipe(@RequestParam Long divisionId, @RequestBody Equipe equipe){
        Division division = new Division();
        division.setId(divisionId);
        equipe.setDivision(division);
        return equipeRepository.save(equipe);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/equipe", method = RequestMethod.DELETE)
    public void deleteEquipe(@RequestParam Long id){
        equipeRepository.delete(id);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/equipes/names", method = RequestMethod.PUT)
    public List<Equipe> updateEquipeNames(@RequestBody List<Equipe> equipeList){
        return equipeService.updateEquipeNames(equipeList);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/equipe/poule", method = RequestMethod.PUT)
    public Equipe updatePouleEquipe(@RequestParam Long equipeId, @RequestBody Poule poule){
        equipeRepository.updatePoule(equipeId,poule);
        return equipeRepository.findOne(equipeId);
    }

    //1107, 1113, 1116, 1112: division
    //1093 : club
    //1092 : club

//
//    @RequestMapping(method= RequestMethod.GET, path="/public/equipe/createEquipe")
//    public Equipe createEquipe() {
//        Division division = new Division();
//        division.setId(1112L);
//        Club club = new Club();
//        club.setId(1093L);
//        Equipe equipe = new Equipe();
//        equipe.setDivision(division);
//        equipe.setClub(club);
//        return equipeRepository.save(equipe);
//    }
}
