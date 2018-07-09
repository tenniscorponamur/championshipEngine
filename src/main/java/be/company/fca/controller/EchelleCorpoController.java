package be.company.fca.controller;

import be.company.fca.model.EchelleCorpo;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour les échelles Corpo")
public class EchelleCorpoController {

    @RequestMapping(path="/public/echellesCorpo", method= RequestMethod.GET)
    Iterable<EchelleCorpo> getAllEchellesCorpo() {
        return EchelleCorpo.getAllEchellesCorpo();
    }
}
