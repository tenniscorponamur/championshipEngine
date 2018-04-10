package be.company.fca.controller;

import be.company.fca.model.Club;
import be.company.fca.repository.ClubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
public class ClubController {

    @Autowired
    private ClubRepository clubRepository;

    @RequestMapping(path="/public/clubs", method= RequestMethod.GET)
    Iterable<Club> getAllClubs() {
        return clubRepository.findAll();
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
//
//    @RequestMapping(method= RequestMethod.GET, path="/public/newClub")
//    Club newClub() {
//        Club club = new Club();
//        club.setName("TC WALLONIE");
//        clubRepository.save(club);
//        return club;
//    }
}
