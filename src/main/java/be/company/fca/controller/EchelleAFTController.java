package be.company.fca.controller;

import be.company.fca.model.EchelleAFT;
import io.swagger.annotations.Api;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour les échelles AFT")
public class EchelleAFTController {

    @RequestMapping(path="/public/echellesAFT", method= RequestMethod.GET)
    Iterable<EchelleAFT> getAllEchellesAFT() {
        return EchelleAFT.getAllEchellesAFT();
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/officialAFT/{numAft}", method = RequestMethod.GET)
    public String getOfficialAFT(@PathVariable("numAft") String numAft){

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //String requestJson = "{text:\"6065450\"}";
        String requestJson = "{text:" + numAft + "}";

        HttpEntity<String> request = new HttpEntity<>(requestJson,headers);
        String result = restTemplate.postForObject("http://www.aftnet.be/MyAFT/Players/GetPlayersAutocomplete", request, String.class);

        String classementSimple = null;
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(result);
            classementSimple = jsonNode.findValue("ClasmtSimple").asText().trim();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classementSimple;
//
        // Pour les tests
//        return "[{\"Nom\":\"CALAY\",\"Prenom\":\"Fabrice\",\"NumFed\":\"6065450\",\"ClasmtSimple\":\"NC\",\"DateNaisText\":\"02/11/1982\"}]";
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
