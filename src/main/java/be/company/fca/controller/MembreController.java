package be.company.fca.controller;

import be.company.fca.dto.MembreDto;
import be.company.fca.model.Club;
import be.company.fca.model.Genre;
import be.company.fca.model.Membre;
import be.company.fca.repository.MembreRepository;
import be.company.fca.utils.POIUtils;
import be.company.fca.utils.UserUtils;
import io.swagger.annotations.Api;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des membres")
public class MembreController {

    @Autowired
    private MembreRepository membreRepository;

    // DTO pour les membres afin de ne pas recuperer l'adresse si on n'est pas authentifie

    @RequestMapping(method= RequestMethod.GET, path="/public/membres")
    public List<MembreDto> getMembres(Authentication authentication, @RequestParam(required = false) Long clubId) {

        List<MembreDto> membresDto = new ArrayList<MembreDto>();
        List<Membre> membres = new ArrayList<Membre>();

        if (clubId!=null){
            Club club = new Club();
            club.setId(clubId);
            membres = (List<Membre>) membreRepository.findByClub(club);
        }else{
            membres = (List<Membre>) membreRepository.findAll();
        }

        for (Membre membre : membres){
            if (UserUtils.isPrivateInformationsAuthorized(authentication) || membre.isActif()){
                membresDto.add(new MembreDto(membre,UserUtils.isPrivateInformationsAuthorized(authentication)));
            }
        }

        return membresDto;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/membre/{membreId}/infosGenerales", method = RequestMethod.PUT)
    public Membre updateMembreInfosGenerales(@PathVariable("membreId") Long membreId, @RequestBody Membre membre){
        membreRepository.updateInfosGenerales(membreId,
                membre.getGenre(),
                membre.getPrenom(),
                membre.getNom(),
                membre.getDateNaissance());
        return membre;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/membre/{membreId}/clubInfos", method = RequestMethod.PUT)
    public Membre updateClubInfos(@PathVariable("membreId") Long membreId, @RequestBody Membre membre){
        membreRepository.updateClubInfos(membreId,
                membre.getClub(),
                membre.isActif(),
                membre.isCapitaine(),
                membre.isResponsableClub(),
                membre.getDateAffiliationCorpo(),
                membre.getDateDesaffiliationCorpo()
                );
        return membre;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/membre/{membreId}/coordonnees", method = RequestMethod.PUT)
    public Membre updateCoordonnees(@PathVariable("membreId") Long membreId, @RequestBody Membre membre){
        membreRepository.updateCoordonnees(membreId,
                membre.getCodePostal(),
                membre.getLocalite(),
                membre.getRue(),
                membre.getRueNumero(),
                membre.getRueBoite()
        );
        return membre;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/membre/{membreId}/contacts", method = RequestMethod.PUT)
    public Membre updateContacts(@PathVariable("membreId") Long membreId, @RequestBody Membre membre){
        membreRepository.updateContacts(membreId,
                membre.getTelephone(),
                membre.getGsm(),
                membre.getMail()
        );
        return membre;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/membre/{membreId}/infosAft", method = RequestMethod.PUT)
    public Membre updateInfosAft(@PathVariable("membreId") Long membreId, @RequestBody Membre membre){
        membreRepository.updateInfosAft(membreId,
                membre.getNumeroAft(),
                membre.getNumeroClubAft(),
                membre.getDateAffiliationAft(),
                membre.isOnlyCorpo()
                );
        return membre;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/membre", method = RequestMethod.POST)
    public Membre addMembre(@RequestBody Membre membre){
        Membre newMembre = new Membre();
        newMembre.setGenre(membre.getGenre());
        newMembre.setPrenom(membre.getPrenom());
        newMembre.setNom(membre.getNom());
        newMembre.setDateNaissance(membre.getDateNaissance());
        membreRepository.save(newMembre);
        return newMembre;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(path="/private/membre/{membreId}/deletable", method= RequestMethod.GET)
    public boolean isDeletable(@PathVariable("membreId") Long membreId){

        // TODO : faire des counts pour savoir si le membre n'a pas de reference
        // --> pas en tant que capitaine d'equipe, pas de match
        //
        return false;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/membre", method = RequestMethod.DELETE)
    public void deleteMembre(@RequestParam Long membreId){

    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/membres/import", method = RequestMethod.POST)
    public void importData(@RequestBody byte[] content) throws Exception {
        FileOutputStream fileOutputStream = new FileOutputStream("D:/testFichier.xlsx");
        fileOutputStream.write(Base64.getDecoder().decode(content));
        fileOutputStream.close();
    }


//    @RequestMapping(method= RequestMethod.GET, path="/public/membre/createDb")
//    public Iterable<Membre> createMembreDb() throws IOException, InvalidFormatException {
//
//        Workbook wb = POIUtils.createWorkbook(new FileInputStream("/Users/fabricecalay/Downloads/membres.xls"));
//        Sheet sheet = POIUtils.getSheet(wb,0,false);
//
//        for (int i=1;i<sheet.getPhysicalNumberOfRows();i++){
//            String numero = POIUtils.readAsString(sheet,i,0);
//            String nom = POIUtils.readAsString(sheet,i,1);
//            String prenom = POIUtils.readAsString(sheet,i,2);
//            Object dateNaissanceObj = POIUtils.readDate(sheet,i,3);
//            String genre = POIUtils.readAsString(sheet,i,4);
//
//            if (!StringUtils.isEmpty(numero)){
//
//                //TODO : il y a des doublons dans la base initiale --> traiter lors de l'insertion
//
//                Membre membre = new Membre();
//                membre.setNumero(numero);
//                membre.setPrenom(prenom);
//                membre.setNom(nom);
//                if (dateNaissanceObj!=null && dateNaissanceObj instanceof Date){
//                    Date dateNaissance = (Date) dateNaissanceObj;
//                    Calendar gc = new GregorianCalendar();
//                    gc.setTime(dateNaissance);
//                    // Gestion bug de l'export Excel
//                    if (gc.get(Calendar.YEAR) > 2020){
//                        gc.add(Calendar.YEAR,-100);
//                    }
//                    membre.setDateNaissance(gc.getTime());
//                }
//                if ("F".equals(genre)){
//                    membre.setGenre(Genre.FEMME);
//                }else{
//                    membre.setGenre(Genre.HOMME);
//                }
//                try{
//                    membreRepository.save(membre);
//                }catch (Exception e){
//                    System.err.println("Doublon : " + numero);
//                    e.printStackTrace();
//                }
//
//            }
//        }
//
//
//        return membreRepository.findAll();
//    }

}
