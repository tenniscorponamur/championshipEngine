package be.company.fca.controller;

import be.company.fca.model.Club;
import be.company.fca.model.HoraireTerrain;
import be.company.fca.model.Terrain;
import be.company.fca.model.TypeChampionnat;
import be.company.fca.repository.*;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@RestController
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des terrains")
public class TerrainController {

    @Autowired
    private TerrainRepository terrainRepository;

    @Autowired
    private HoraireTerrainRepository horaireTerrainRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private EquipeRepository equipeRepository;

    @Autowired
    private RencontreRepository rencontreRepository;

    @RequestMapping(path="/public/terrains", method= RequestMethod.GET)
    Iterable<Terrain> getAllTerrains() {
        return terrainRepository.findAll();
    }

    @RequestMapping(path="/public/terrain", method= RequestMethod.GET)
    Terrain getTerrain(@RequestParam Long id) {
        return terrainRepository.findOne(id);
    }

    @RequestMapping(path="/public/terrain/{terrainId}/horaires", method= RequestMethod.GET)
    List<HoraireTerrain> getHorairesTerrain(@PathVariable("terrainId") Long terrainId) {
        Terrain terrain = new Terrain();
        terrain.setId(terrainId);
        return horaireTerrainRepository.findByTerrain(terrain);
    }

    @RequestMapping(path="/public/horairesTerrain", method= RequestMethod.GET)
    List<HoraireTerrain> getHorairesTerrainByTypeChampionnat(@RequestParam("typeChampionnat") TypeChampionnat typeChampionnat){
        return horaireTerrainRepository.findByTypeChampionnat(typeChampionnat);
}

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/terrain", method = RequestMethod.PUT)
    public Terrain updateTerrain(@RequestBody Terrain terrain){
        return terrainRepository.save(terrain);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/terrain", method = RequestMethod.POST)
    public Terrain addTerrain(@RequestBody Terrain terrain){
        return terrainRepository.save(terrain);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(path="/private/terrain/{terrainId}/deletable", method= RequestMethod.GET)
    public boolean isDeletable(@PathVariable("terrainId") Long terrainId){
        Terrain terrain = new Terrain();
        terrain.setId(terrainId);

        // Faire des counts pour savoir si le terrain n'a pas de reference
        // --> pas de club, pas d'equipe, pas de rencontre

        long count = clubRepository.countByTerrain(terrain);
        if (count==0){
            count = equipeRepository.countByTerrain(terrain);
            if (count==0){
                count = rencontreRepository.countByTerrain(terrain);
                return count==0;
            }
        }
        return false;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/terrain", method = RequestMethod.DELETE)
    public void deleteTerrain(@RequestParam Long terrainId){
        if (isDeletable(terrainId)){
            terrainRepository.delete(terrainId);
        }
    }


    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/terrain/{terrainId}/horaire", method = RequestMethod.PUT)
    public HoraireTerrain updateHoraireTerrain(@PathVariable("terrainId") Long terrainId, @RequestBody HoraireTerrain horaireTerrain){
        Terrain terrain = new Terrain();
        terrain.setId(terrainId);
        horaireTerrain.setTerrain(terrain);
        return horaireTerrainRepository.save(horaireTerrain);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/terrain/{terrainId}/horaire", method = RequestMethod.POST)
    public HoraireTerrain addHoraireTerrain(@PathVariable("terrainId") Long terrainId,@RequestBody HoraireTerrain horaireTerrain){
        Terrain terrain = new Terrain();
        terrain.setId(terrainId);
        horaireTerrain.setTerrain(terrain);
        return horaireTerrainRepository.save(horaireTerrain);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/terrain/{terrainId}/horaire", method = RequestMethod.DELETE)
    public void deleteHoraireTerrain(@PathVariable("terrainId") Long terrainId, @RequestParam Long horaireTerrainId){
        horaireTerrainRepository.delete(horaireTerrainId);
    }
}
