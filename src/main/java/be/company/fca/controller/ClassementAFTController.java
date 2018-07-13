package be.company.fca.controller;

import be.company.fca.model.ClassementAFT;
import be.company.fca.repository.ClassementAFTRepository;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des classements AFT des membres")
public class ClassementAFTController {

    @Autowired
    private ClassementAFTRepository classementAFTRepository;

    @RequestMapping(path="/public/membre/{membreId}/classementsAFT", method= RequestMethod.GET)
    Iterable<ClassementAFT> getClassementsAFTByMembre(@PathVariable("membreId") Long membreId) {
        return classementAFTRepository.findByMembreFk(membreId);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/membre/{membreId}/classementAFT", method = RequestMethod.POST)
    public ClassementAFT addClassementAFT(@PathVariable("membreId") Long membreId, @RequestBody ClassementAFT classementAFT){
        return classementAFTRepository.save(classementAFT);
    }

}
