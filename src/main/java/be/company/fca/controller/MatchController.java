package be.company.fca.controller;

import be.company.fca.dto.MatchDto;
import be.company.fca.model.*;
import be.company.fca.repository.MatchRepository;
import be.company.fca.repository.RencontreRepository;
import be.company.fca.repository.TerrainRepository;
import be.company.fca.service.MatchService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
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

    @RequestMapping(method= RequestMethod.GET, path="/public/matchs/validesByCriteria")
    public List<MatchDto> findValidesByMembreBetweenDates(@RequestParam("membreId") Long membreId,
                                                          @RequestParam @DateTimeFormat(pattern="yyyyMMdd") Date startDate,
                                                          @RequestParam @DateTimeFormat(pattern="yyyyMMdd") Date endDate
                                                          ) {

        List<MatchDto> matchsDto = new ArrayList<>();
        List<Match> matchs = matchRepository.findValidesByMembreBetweenDates(membreId,startDate,endDate);

        for (Match match : matchs){
            matchsDto.add(new MatchDto(match));
        }

        return matchsDto;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/match", method = RequestMethod.DELETE)
    public void deleteMatch(@RequestParam Long id){
        matchRepository.delete(id);
    }
}
