package be.company.fca.controller;

import be.company.fca.model.ClassementAFT;
import be.company.fca.repository.ClassementAFTRepository;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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


    @RequestMapping(value = "/testAFT/{numAft}", method = RequestMethod.GET)
    public String testAFT(@PathVariable("numAft") String numAft){

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //String requestJson = "{text:\"6065450\"}";
        String requestJson = "{text:" + numAft + "}";

        HttpEntity<String> request = new HttpEntity<>(requestJson,headers);
        String result = restTemplate.postForObject("http://www.aftnet.be/MyAFT/Players/GetPlayersAutocomplete", request, String.class);

        return result;
    }


    public static void main(String[] args){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestJson = "{text:\"6065450\"}";

        HttpEntity<String> request = new HttpEntity<>(requestJson,headers);
        String result = restTemplate.postForObject("http://www.aftnet.be/MyAFT/Players/GetPlayersAutocomplete", request, String.class);

        System.err.println(result);

    }
}
