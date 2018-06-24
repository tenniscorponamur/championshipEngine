package be.company.fca.controller;

import be.company.fca.model.Match;
import be.company.fca.model.Rencontre;
import be.company.fca.model.Terrain;
import be.company.fca.repository.MatchRepository;
import be.company.fca.repository.RencontreRepository;
import be.company.fca.repository.TerrainRepository;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des matchs d'une rencontre")
public class MatchController {

    @Autowired
    private MatchRepository matchRepository;

    @RequestMapping(method= RequestMethod.GET, path="/public/matchs")
    public Iterable<Match> getMatchsByRencontre(@RequestParam Long rencontreId) {
        Rencontre rencontre = new Rencontre();
        rencontre.setId(rencontreId);
        return matchRepository.findByRencontre(rencontre);
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
    @RequestMapping(value = "/private/match", method = RequestMethod.POST)
    public Match addMatch(@RequestBody Match match){
        return matchRepository.save(match);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/match", method = RequestMethod.DELETE)
    public void deleteMatch(@RequestParam Long id){
        matchRepository.delete(id);
    }
}
