package be.company.fca.controller;

import be.company.fca.model.Match;
import be.company.fca.model.Rencontre;
import be.company.fca.model.Terrain;
import be.company.fca.model.TypeMatch;
import be.company.fca.repository.MatchRepository;
import be.company.fca.repository.RencontreRepository;
import be.company.fca.repository.TerrainRepository;
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

    @RequestMapping(method= RequestMethod.GET, path="/public/matchs")
    public Iterable<Match> getMatchsByRencontre(@RequestParam Long rencontreId) {

        Rencontre rencontre = new Rencontre();
        rencontre.setId(rencontreId);
        List<Match> matchs = (List<Match>) matchRepository.findByRencontre(rencontre);

        if (matchs.isEmpty()){
            matchs = new ArrayList<Match>();

            // 4 matchs simples

            for (int i=0;i<4;i++){
                Match match = new Match();
                match.setRencontre(rencontre);
                match.setType(TypeMatch.SIMPLE);
                match.setOrdre(i+1);

                match = matchRepository.save(match);

                matchs.add(match);
            }

            // 2 matchs doubles

            for (int i=0;i<2;i++){
                Match match = new Match();
                match.setRencontre(rencontre);
                match.setType(TypeMatch.DOUBLE);
                match.setOrdre(i+1);

                match = matchRepository.save(match);

                matchs.add(match);
            }
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
