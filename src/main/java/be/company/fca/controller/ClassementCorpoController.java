package be.company.fca.controller;

import be.company.fca.model.ClassementCorpo;
import be.company.fca.repository.ClassementCorpoRepository;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des classements corpo des membres")
public class ClassementCorpoController {

    @Autowired
    private ClassementCorpoRepository classementCorpoRepository;

    @RequestMapping(path="/public/membre/{membreId}/classementsCorpo", method= RequestMethod.GET)
    Iterable<ClassementCorpo> getClassementsCorpoByMembre(@PathVariable("membreId") Long membreId) {
        return classementCorpoRepository.findByMembreFk(membreId);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/membre/{membreId}/classementCorpo", method = RequestMethod.POST)
    public ClassementCorpo addClassementCorpo(@PathVariable("membreId") Long membreId, @RequestBody ClassementCorpo classementCorpo){
        return classementCorpoRepository.save(classementCorpo);
    }

}
