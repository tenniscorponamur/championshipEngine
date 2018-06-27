package be.company.fca.controller;

import be.company.fca.model.Match;
import be.company.fca.model.Set;
import be.company.fca.repository.SetRepository;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des sets d'un match")
public class SetController {

    @Autowired
    private SetRepository setRepository;

    @RequestMapping(method= RequestMethod.GET, path="/public/sets")
    public Iterable<Set> getSetsByMatch(@RequestParam Long matchId) {
        Match match = new Match();
        match.setId(matchId);
        return setRepository.findByMatch(match);
    }
//
//    @RequestMapping(path="/public/set", method= RequestMethod.GET)
//    Set getSet(@RequestParam Long id) {
//        return setRepository.findOne(id);
//    }
//
//    @PreAuthorize("hasAuthority('ADMIN_USER')")
//    @RequestMapping(value = "/private/set", method = RequestMethod.PUT)
//    public Set updateSet(@RequestBody Set set){
//        return setRepository.save(set);
//    }
//
//    @PreAuthorize("hasAuthority('ADMIN_USER')")
//    @RequestMapping(value = "/private/set", method = RequestMethod.POST)
//    public Set addSet(@RequestBody Set set){
//        return setRepository.save(set);
//    }
//
//    @PreAuthorize("hasAuthority('ADMIN_USER')")
//    @RequestMapping(value = "/private/set", method = RequestMethod.DELETE)
//    public void deleteSet(@RequestParam Long id){
//        setRepository.delete(id);
//    }
}
