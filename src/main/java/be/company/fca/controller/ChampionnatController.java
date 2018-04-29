package be.company.fca.controller;

import be.company.fca.model.CategorieChampionnat;
import be.company.fca.model.Championnat;
import be.company.fca.model.TypeChampionnat;
import be.company.fca.repository.ChampionnatRepository;
import be.company.fca.repository.MembreRepository;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

    //
    //    @PreAuthorize("hasAuthority('ADMIN_USER')")
    // TODO : change championnat == private / ADMIN

    @RequestMapping(method= RequestMethod.GET, path="/public/championnat/createChampionnat")
    public Championnat createChampionnat() {
        Championnat championnat = new Championnat();
        championnat.setAnnee(2018);
        championnat.setType(TypeChampionnat.ETE);
        championnat.setCategorie(CategorieChampionnat.DAMES);
        championnatRepository.save(championnat);
        return championnat;
    }

}
