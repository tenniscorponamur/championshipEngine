package be.company.fca.controller;

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
    private MatchRepository matchRepository;

    @Autowired
    private MatchService matchService;

    // TODO : les urls devraient être basées sur la rencontre
    // --> /public/rencontre/{id}/match

    @RequestMapping(method= RequestMethod.GET, path="/public/rencontre/{rencontreId}/matchs")
    public Iterable<Match> getMatchsByRencontre(@PathVariable("rencontreId") Long rencontreId) {

        Rencontre rencontre = new Rencontre();
        rencontre.setId(rencontreId);
        List<Match> matchs = (List<Match>) matchRepository.findByRencontre(rencontre);

        if (matchs.isEmpty()){
            matchs = matchService.createMatchs(rencontre);
        }

        return matchs;

    }

    @RequestMapping(path="/public/match", method= RequestMethod.GET)
    Match getMatch(@RequestParam Long id) {
        return matchRepository.findOne(id);
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
