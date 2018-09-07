package be.company.fca.controller;

import be.company.fca.model.*;
import be.company.fca.model.Set;
import be.company.fca.repository.*;
import be.company.fca.service.ClassementService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des classements")
public class ClassementController {

    @Autowired
    private ClassementService classementService;

    @RequestMapping(method= RequestMethod.GET, path="/public/classements")
    public Collection<Classement> getClassementByChampionnat(@RequestParam Long championnatId) {
        return classementService.getClassementByChampionnat(championnatId);
    }

}
