package be.company.fca.controller;

import be.company.fca.model.Localite;
import io.swagger.annotations.Api;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour des informations sur les localites et leurs rues")
public class LocaliteController {

    private final String geoServicesBaseUrl = "http://geoservices.wallonie.be/geolocalisation/rest/";

    // TODO : recuperer la province pour avoir un indicateur de la province de la localite

    @RequestMapping(method= RequestMethod.GET, path="/public/localites")
    public List<Localite> getLocalites() throws IOException {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String resultLocalites = restTemplate.getForObject(geoServicesBaseUrl + "getListeLocalites/", String.class);
        ObjectMapper mapper = new ObjectMapper();
        HashMap map = mapper.readValue(resultLocalites,HashMap.class);

        List<Localite> localites = new ArrayList<Localite>();
        List<Map> localitesList = (List<Map>) map.get("localites");
        for (Map localite : localitesList){
            List<Integer> codesPostaux = (List<Integer>) localite.get("cps");
            localites.add(new Localite(codesPostaux.get(0).toString(),(String) localite.get("nom"),(String) localite.get("commune")));
        }

        return localites;
    }

    @RequestMapping(method= RequestMethod.GET, path="/public/localite/{codePostal}/rues")
    public List<String> getRues(@PathVariable("codePostal") String codePostal) throws IOException {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String resultsRues = restTemplate.getForObject(geoServicesBaseUrl + "getListeRuesByCp/" +  codePostal + "/", String.class);
        ObjectMapper mapper = new ObjectMapper();
        HashMap mapRues = mapper.readValue(resultsRues,HashMap.class);

        List<String> rues = new ArrayList<String>();
        List<Map> ruesList = (List<Map>) mapRues.get("rues");
        for (Map rue : ruesList){
            rues.add((String) rue.get("nom"));
        }
        return rues;
    }

}
