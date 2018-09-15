package be.company.fca.controller;

import be.company.fca.dto.MatchDto;
import be.company.fca.model.*;
import be.company.fca.repository.MatchRepository;
import be.company.fca.repository.RencontreRepository;
import be.company.fca.repository.TerrainRepository;
import be.company.fca.service.MatchService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des matchs d'une rencontre")
public class MatchController {

    @Autowired
    private RencontreRepository rencontreRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchService matchService;


    // DTO pour les membres afin de ne pas recuperer les donnees privees
    // Attention a la rencontre --> rencontreDto

    /**
     * Permet de recuperer les matchs pour une rencontre
     * Si les matchs n'existent pas, ils sont créés à la volée
     *
     * Cette création dépend du type de championnat et de la catégorie
     *
     * @param rencontreId
     * @return
     */
    @RequestMapping(method= RequestMethod.GET, path="/public/rencontre/{rencontreId}/matchs")
    public List<MatchDto> getMatchsByRencontre(@PathVariable("rencontreId") Long rencontreId) {

        List<MatchDto> matchsDto = new ArrayList<>();
        List<Match> matchs = new ArrayList<Match>();

        Rencontre rencontre = rencontreRepository.findOne(rencontreId);

        matchs = (List<Match>) matchRepository.findByRencontre(rencontre);

        if (matchs.isEmpty()){
            matchs = matchService.createMatchs(rencontre);
        }

        for (Match match : matchs){
            matchsDto.add(new MatchDto(match));
        }

        return matchsDto;

    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/match", method = RequestMethod.PUT)
    public Match updateMatch(@RequestBody Match match){
        return matchRepository.save(match);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/match/sets", method = RequestMethod.PUT)
    public Match updateMatchAndSets(@RequestParam Long matchId, @RequestBody List<Set> sets){
        return matchService.updateMatchAndSets(matchId,sets);
    }

//    @PreAuthorize("hasAuthority('ADMIN_USER')")
//    @RequestMapping(value = "/private/match", method = RequestMethod.POST)
//    public Match addMatch(@RequestBody Match match){
//        return matchRepository.save(match);
//    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/match", method = RequestMethod.DELETE)
    public void deleteMatch(@RequestParam Long id){
        matchRepository.delete(id);
    }
}
