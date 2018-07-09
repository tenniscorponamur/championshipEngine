package be.company.fca.controller;

import be.company.fca.model.EchelleAFT;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour les échelles AFT")
public class EchelleAFTController {

    @RequestMapping(path="/public/echellesAFT", method= RequestMethod.GET)
    Iterable<EchelleAFT> getAllEchellesAFT() {
        return EchelleAFT.getAllEchellesAFT();
    }
}
