package be.company.fca.controller;

import be.company.fca.model.CategorieChampionnat;
import be.company.fca.model.Championnat;
import be.company.fca.model.Club;
import be.company.fca.model.TypeChampionnat;
import be.company.fca.repository.ChampionnatRepository;
import be.company.fca.repository.MembreRepository;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des championnats")
public class ChampionnatController {

    @Autowired
    private ChampionnatRepository championnatRepository;

    @RequestMapping(method= RequestMethod.GET, path="/public/championnats")
    public Iterable<Championnat> getChampionnats() {
        return championnatRepository.findAll();
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat", method = RequestMethod.PUT)
    public Championnat updateChampionnat(@RequestBody Championnat championnat){
        return championnatRepository.save(championnat);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat", method = RequestMethod.POST)
    public Championnat addChampionnat(@RequestBody Championnat championnat){
        return championnatRepository.save(championnat);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat", method = RequestMethod.DELETE)
    public void deleteChampionnat(@RequestParam Long id){
        championnatRepository.delete(id);
    }

//    @RequestMapping(method= RequestMethod.GET, path="/public/championnat/createChampionnat")
//    public Championnat createChampionnat() {
//        Championnat championnat = new Championnat();
//        championnat.setAnnee(2018);
//        championnat.setType(TypeChampionnat.ETE);
//        championnat.setCategorie(CategorieChampionnat.DAMES);
//        championnatRepository.save(championnat);
//        return championnat;
//    }

}
