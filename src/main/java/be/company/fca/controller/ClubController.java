package be.company.fca.controller;

import be.company.fca.model.Club;
import be.company.fca.repository.ClubRepository;
import be.company.fca.repository.EquipeRepository;
import be.company.fca.repository.MembreRepository;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des clubs")
public class ClubController {

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private MembreRepository membreRepository;

    @Autowired
    private EquipeRepository equipeRepository;

    @RequestMapping(path="/public/clubs", method= RequestMethod.GET)
    Iterable<Club> getAllClubs() {
        return clubRepository.findAll();
    }

    @RequestMapping(path="/public/club", method= RequestMethod.GET)
    Club getClub(@RequestParam Long id) {
        return clubRepository.findOne(id);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/club", method = RequestMethod.PUT)
    public Club updateClub(@RequestBody Club club){
        return clubRepository.save(club);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/club", method = RequestMethod.POST)
    public Club addClub(@RequestBody Club club){
        return clubRepository.save(club);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(path="/private/club/{clubId}/deletable", method= RequestMethod.GET)
    public boolean isDeletable(@PathVariable("clubId") Long clubId){
        Club club = new Club();
        club.setId(clubId);

        // Faire des counts pour savoir si le club n'a pas de reference
        // --> pas d'equipe, pas de membre

        long count = equipeRepository.countByClub(club);
        if (count==0){
            count = membreRepository.countByClub(club);
            return count==0;
        }
        return false;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/club", method = RequestMethod.DELETE)
    public void deleteClub(@RequestParam Long clubId){
        if (isDeletable(clubId)){
            clubRepository.delete(clubId);
        }
    }
}
