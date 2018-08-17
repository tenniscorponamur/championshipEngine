package be.company.fca.controller;

import be.company.fca.model.ClassementAFT;
import be.company.fca.model.EchelleAFT;
import be.company.fca.repository.ClassementAFTRepository;
import be.company.fca.repository.MembreRepository;
import be.company.fca.service.ClassementAFTService;
import be.company.fca.service.ClassementCorpoService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des classements AFT des membres")
public class ClassementAFTController {

    @Autowired
    private MembreRepository membreRepository;

    @Autowired
    private ClassementAFTRepository classementAFTRepository;

    @Autowired
    private ClassementAFTService classementAFTService;

    @RequestMapping(path="/public/membre/{membreId}/classementsAFT", method= RequestMethod.GET)
    Iterable<ClassementAFT> getClassementsAFTByMembre(@PathVariable("membreId") Long membreId) {
        return classementAFTRepository.findByMembreFk(membreId);
    }

    /**
     * Permet de sauvegarder les classements AFT obtenus par un membre
     * Procede a la suppression des anciens classements avant la sauvegarde effective des nouveaux
     *
     * @param membreId Identifiant du membre
     * @param classementAFTList Liste des classements obtenus par le membre
     * @return Le classement actuel du membre
     */
    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/membre/{membreId}/classementsAFT", method = RequestMethod.PUT)
    public ClassementAFT saveClassementsAFT(@PathVariable("membreId") Long membreId, @RequestBody List<ClassementAFT> classementAFTList){

        // delete classement actuel membre --> methode specifique pour ne pas toucher d'autres elements du membre
        membreRepository.updateClassementAFT(membreId,null);

        ClassementAFT classementAFTActuel = classementAFTService.saveClassementsAFT(membreId,classementAFTList);

        // save classement actuel

        membreRepository.updateClassementAFT(membreId,classementAFTActuel);

        return classementAFTActuel;
    }

}
