package be.company.fca.controller;

import be.company.fca.dto.EquipeDto;
import be.company.fca.model.*;
import be.company.fca.repository.*;
import be.company.fca.service.DivisionService;
import be.company.fca.service.EquipeService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des equipes")
public class EquipeController {

    @Autowired
    private EquipeRepository equipeRepository;

    @Autowired
    private DivisionRepository divisionRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private SetRepository setRepository;

    @Autowired
    private RencontreRepository rencontreRepository;

    @Autowired
    private ChampionnatRepository championnatRepository;

    @Autowired
    private EquipeService equipeService;

    // DTO pour les capitaines d'equipe afin de ne pas recuperer les donnees privees

    @RequestMapping(method= RequestMethod.GET, path="/public/equipes")
    public List<EquipeDto> getEquipesByDivisionOrPoule(@RequestParam Long divisionId, @RequestParam(required = false) Long pouleId) {
        List<EquipeDto> equipesDto = new ArrayList<>();
        List<Equipe> equipes = new ArrayList<Equipe>();

        if (pouleId!=null){
            Poule poule = new Poule();
            poule.setId(pouleId);
            equipes = (List<Equipe>) equipeRepository.findByPoule(poule);
        }else{
            Division division = new Division();
            division.setId(divisionId);
            equipes = (List<Equipe>) equipeRepository.findByDivision(division);
        }

        for (Equipe equipe : equipes){
            equipesDto.add(new EquipeDto(equipe));
        }

        return equipesDto;
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
        Division division = divisionRepository.findById(divisionId).get();
        equipe.setDivision(division);

        // Operation non-permise si le calendrier est valide ou cloture
        if (division.getChampionnat().isCalendrierValide() || division.getChampionnat().isCloture()){
            throw new RuntimeException("Operation not supported - Calendrier valide ou championnat cloture");
        }

        Equipe equipeSaved = equipeRepository.save(equipe);
        // On signale que le calendrier doit etre rafraichi si l'equipe a ete sauvee
        championnatRepository.updateCalendrierARafraichir(division.getChampionnat().getId(),true);



        return equipeSaved;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/equipe", method = RequestMethod.DELETE)
    public void deleteEquipe(@RequestParam Long id){

        Equipe equipe = equipeRepository.findById(id).get();

        // Operation non-permise si le calendrier est valide ou cloture
        if (equipe.getDivision().getChampionnat().isCalendrierValide() || equipe.getDivision().getChampionnat().isCloture()){
            throw new RuntimeException("Operation not supported - Calendrier valide ou championnat cloture");
        }

        // Supprimer les rencontres associees a cette equipe sinon on ne pourra pas la supprimer :-)
        List<Rencontre> rencontresEquipe = (List<Rencontre>) rencontreRepository.findRencontresByEquipe(equipe);
        for (Rencontre rencontreEquipe : rencontresEquipe){
            setRepository.deleteByRencontreId(rencontreEquipe.getId());
            matchRepository.deleteByRencontreId(rencontreEquipe.getId());
            rencontreRepository.deleteById(rencontreEquipe.getId());
        }

        equipeRepository.deleteById(id);
        // On signale que le calendrier doit etre rafraichi si l'equipe a ete supprimee
        championnatRepository.updateCalendrierARafraichir(equipe.getDivision().getChampionnat().getId(),true);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/equipes/names", method = RequestMethod.PUT)
    public List<Equipe> updateEquipeNames(@RequestBody List<Equipe> equipeList){
        return equipeService.updateEquipeNames(equipeList);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/equipe/poule", method = RequestMethod.PUT)
    public Equipe updatePouleEquipe(@RequestParam Long equipeId, @RequestBody Poule poule){

        Equipe equipe = equipeRepository.findById(equipeId).get();

        // Operation non-permise si le calendrier est valide ou cloture
        if (equipe.getDivision().getChampionnat().isCalendrierValide() || equipe.getDivision().getChampionnat().isCloture()){
            throw new RuntimeException("Operation not supported - Calendrier valide ou championnat cloture");
        }

        equipeRepository.updatePoule(equipeId,poule);

        // On signale que le calendrier doit etre rafraichi si l'equipe a change de poule
        championnatRepository.updateCalendrierARafraichir(equipe.getDivision().getChampionnat().getId(),true);

        return equipe;
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
