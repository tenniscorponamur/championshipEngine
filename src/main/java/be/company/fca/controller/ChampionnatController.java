package be.company.fca.controller;

import be.company.fca.model.*;
import be.company.fca.repository.*;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des championnats")
public class ChampionnatController {

    @Autowired
    private ChampionnatRepository championnatRepository;

    @Autowired
    private DivisionRepository divisionRepository;

    @Autowired
    private RencontreRepository rencontreRepository;

    @Autowired
    private SetRepository setRepository;

    @RequestMapping(method= RequestMethod.GET, path="/public/championnats")
    public Iterable<Championnat> getChampionnats() {
        return championnatRepository.findAll();
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat", method = RequestMethod.PUT)
    public Championnat updateChampionnat(@RequestBody Championnat championnat){
        championnatRepository.updateInfosGenerales(championnat.getId(), championnat.getAnnee(),championnat.getType(),championnat.getCategorie());
        return championnat;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat", method = RequestMethod.POST)
    public Championnat addChampionnat(@RequestBody Championnat championnat){
        return championnatRepository.save(championnat);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat", method = RequestMethod.DELETE)
    public void deleteChampionnat(@RequestParam Long id){
        Championnat championnat = new Championnat();
        championnat.setId(id);
        //TODO : idealement transactionnel...
        divisionRepository.deleteByChampionnat(championnat);
        championnatRepository.delete(id);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat/calendrierARafraichir", method = RequestMethod.PUT)
    public boolean setCalendrierARafraichir(@RequestParam Long championnatId,@RequestBody boolean aRafraichir){
        championnatRepository.updateCalendrierARafraichir(championnatId,aRafraichir);
        return true;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat/calendrierValide", method = RequestMethod.PUT)
    public boolean setCalendrierValide(@RequestParam Long championnatId,@RequestBody boolean validite){
        if (validite&&isCalendrierValidable(championnatId)){
            championnatRepository.updateCalendrierValide(championnatId,validite);
            return true;
        }else if (!validite && isCalendrierInvalidable(championnatId)){
            championnatRepository.updateCalendrierValide(championnatId,validite);
            return true;
        }
        return false;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat/cloture", method = RequestMethod.PUT)
    public boolean setCloture(@RequestParam Long championnatId,@RequestBody boolean cloture){
        if (cloture && isCloturable(championnatId)){
            championnatRepository.updateChampionnatCloture(championnatId,cloture);
            return true;
        }
        return false;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat/isCalendrierARafraichir", method = RequestMethod.GET)
    public boolean isCalendrierARafraichir(@RequestParam Long championnatId){
        Championnat championnat = championnatRepository.findOne(championnatId);
        return championnat.isCalendrierARafraichir();
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat/isCalendrierValidable", method = RequestMethod.GET)
    public boolean isCalendrierValidable(@RequestParam Long championnatId){
        Championnat championnat = championnatRepository.findOne(championnatId);
        if (!championnat.isCalendrierARafraichir() && !championnat.isCalendrierValide()){
            // On peut valider le calendrier si des rencontres existent pour ce championnat et qu'il a ete valide
            List<Rencontre> rencontres = (List<Rencontre>) rencontreRepository.findRencontresByChampionnat(championnat);
            return !rencontres.isEmpty();
        }
        return false;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat/isCalendrierInvalidable", method = RequestMethod.GET)
    public boolean isCalendrierInvalidable(@RequestParam Long championnatId){
        // On peut invalider la calendrier si celui-ci est validé
        // et qu'aucun resultat n'a ete encode (ce dernier critere est plutot une precaution
        // car cela devrait egalement fonctionner si un resultat a ete encode)
        Championnat championnat = championnatRepository.findOne(championnatId);
        Long setCount = setRepository.countByChampionnatId(championnatId);
        return championnat.isCalendrierValide() && setCount==0;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat/isCalendrierDeletable", method = RequestMethod.GET)
    public boolean isCalendrierDeletable(@RequestParam Long championnatId){
        // Le calendrier peut être supprimé si le calendrier n'est pas marqué comme validé
        // et que des rencontres existent (bien que non-indispensable)
        Championnat championnat = championnatRepository.findOne(championnatId);
        return !championnat.isCalendrierValide();
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat/isCloturable", method = RequestMethod.GET)
    public boolean isCloturable(@RequestParam Long championnatId){
        // TODO : Le calendrier peut etre cloture si toutes les rencontres ont ete disputees et validees (interseries comprises)
        return false;
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
