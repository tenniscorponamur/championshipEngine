package be.company.fca.controller;

import be.company.fca.model.Club;
import be.company.fca.repository.ClubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api")
public class ClubController {

    @Autowired
    private ClubRepository clubRepository;

    @RequestMapping(method= RequestMethod.GET, path="/public/club")
    Iterable<Club> getAllClubs() {
        return clubRepository.findAll();
    }


    @RequestMapping(method= RequestMethod.GET, path="/public/newClub")
    Club newClub() {
        Club club = new Club();
        club.setName("TC WALLONIE");
        clubRepository.save(club);
        return club;
    }
}
