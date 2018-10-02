package be.company.fca.controller;

import be.company.fca.dto.MembreDto;
import be.company.fca.model.*;
import be.company.fca.repository.*;
import be.company.fca.utils.POIUtils;
import be.company.fca.utils.ReportUtils;
import be.company.fca.utils.TemplateUtils;
import be.company.fca.utils.UserUtils;
import io.swagger.annotations.Api;
import net.sf.jasperreports.engine.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.hibernate.sql.Template;
import org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.io.*;
import java.security.Principal;
import java.sql.Connection;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des membres")
public class MembreController {

    @Autowired
    DataSource datasource;

    @Autowired
    private MembreRepository membreRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private EquipeRepository equipeRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private ClassementCorpoRepository classementCorpoRepository;

    @Autowired
    private ClassementAFTRepository classementAFTRepository;

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
    @RequestMapping(value = "/private/membre/{membreId}/anonymisation", method = RequestMethod.PUT)
    public Membre anonymisation(@PathVariable("membreId") Long membreId){

        // Anonymisation -> nom/prenom = ***
        // suppression de l'adresse, telephone, mail, ...

        Membre membre = membreRepository.findOne(membreId);
        membre.setNom("***");
        membre.setPrenom("***");
        membre.setDateNaissance(null);
        membre.setNumeroAft(null);
        membre.setDateAffiliationAft(null);
        membre.setDateAffiliationCorpo(null);
        membre.setDateDesaffiliationCorpo(null);
        membre.setCodePostal(null);
        membre.setLocalite(null);
        membre.setRue(null);
        membre.setRueNumero(null);
        membre.setRueBoite(null);
        membre.setTelephone(null);
        membre.setGsm(null);
        membre.setMail(null);
        membre.setActif(false);
        membreRepository.save(membre);
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

        Membre membre = new Membre();
        membre.setId(membreId);

        // Faire des counts pour savoir si le membre n'a pas de reference
        // --> pas en tant que capitaine d'equipe, pas de match

        long count = equipeRepository.countByCapitaine(membre);
        if (count==0){
            count = matchRepository.countByMembreId(membreId);
            return count==0;
        }
        return false;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/membre", method = RequestMethod.DELETE)
    public void deleteMembre(@RequestParam Long membreId){
        if (isDeletable(membreId)){
            classementCorpoRepository.deleteByMembreFk(membreId);
            classementAFTRepository.deleteByMembreFk(membreId);
            membreRepository.delete(membreId);
        }
    }

    @RequestMapping(path="/public/membres/listeForce", method= RequestMethod.GET)
    ResponseEntity<byte[]> getListeForceMembres() throws Exception {
        JasperReport jasperReport = JasperCompileManager.compileReport(ReportUtils.getListeForceTemplate());
        Connection conn = datasource.getConnection();
        JasperPrint jprint = JasperFillManager.fillReport(jasperReport, new HashMap(), conn);
        byte[] pdfFile =  JasperExportManager.exportReportToPdf(jprint);
        conn.close();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(pdfFile, headers, HttpStatus.OK);
        return response;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(path="/private/membres/import/template", method= RequestMethod.GET)
    ResponseEntity<byte[]> getTemplateImportMembres() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        byte[] excelFile = IOUtils.toByteArray(TemplateUtils.getTemplateImportMembres());
        ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(excelFile, headers, HttpStatus.OK);
        return response;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/membres/import", method = RequestMethod.POST)
    public void importData(@RequestBody byte[] content) throws Exception {
//        FileOutputStream fileOutputStream = new FileOutputStream("D:/testFichier.xlsx");
//        fileOutputStream.write(Base64.getDecoder().decode(content));
//        fileOutputStream.close();

        if (content!=null){

            Workbook wb = POIUtils.createWorkbook(new ByteArrayInputStream(Base64.getDecoder().decode(content)));
            Sheet sheet = POIUtils.getSheet(wb,0,false);

            for (int i=1;i<sheet.getPhysicalNumberOfRows();i++){
                String numeroAft = POIUtils.readAsString(sheet,i,0);
                String nom = POIUtils.readAsString(sheet,i,1);
                String prenom = POIUtils.readAsString(sheet,i,2);
                Object dateNaissanceObj = POIUtils.readDate(sheet,i,3);
                String genre = POIUtils.readAsString(sheet,i,4);
                String rue = POIUtils.readAsString(sheet,i,5);
                String codePostal = POIUtils.readAsString(sheet,i,6);
                String localite = POIUtils.readAsString(sheet,i,7);
                String telephone = POIUtils.readAsString(sheet,i,8);
                String gsm = POIUtils.readAsString(sheet,i,9);
                String mail = POIUtils.readAsString(sheet,i,10);
                String responsableClub = POIUtils.readAsString(sheet,i,11);
                String codeClassementAft = POIUtils.readAsString(sheet,i,12);
                String pointsAft = POIUtils.readAsString(sheet,i,13);
                String pointsCorpo = POIUtils.readAsString(sheet,i,14);
                String numeroClubCorpo = POIUtils.readAsString(sheet,i,15);
                String numeroClubAft = POIUtils.readAsString(sheet,i,16);

                if (!StringUtils.isEmpty(numeroAft)){

                    try{

                        // Considerés comme obligatoires : Nom, prenom, numeroAft (ce dernier pour gerer les doublons)

                        // Si le membre existe deja (en se basant sur le numero Aft) --> update
                        Membre membre = membreRepository.findByNumeroAft(numeroAft);
                        if (membre==null){
                            membre = new Membre();
                        }

                        membre.setNumeroAft(numeroAft);
                        membre.setPrenom(prenom);
                        membre.setNom(nom);
                        if (dateNaissanceObj!=null && dateNaissanceObj instanceof Date){
                            Date dateNaissance = (Date) dateNaissanceObj;
                            Calendar gc = new GregorianCalendar();
                            gc.setTime(dateNaissance);
                            // Gestion bug de l'export Excel
                            if (gc.get(Calendar.YEAR) > 2020){
                                gc.add(Calendar.YEAR,-100);
                            }
                            membre.setDateNaissance(gc.getTime());
                        }
                        if ("F".equals(genre)){
                            membre.setGenre(Genre.FEMME);
                        }else{
                            membre.setGenre(Genre.HOMME);
                        }

                        membre.setRue(rue);
                        membre.setCodePostal(codePostal);
                        membre.setLocalite(localite);
                        membre.setTelephone(telephone);
                        membre.setGsm(gsm);
                        membre.setMail(mail);
                        membre.setResponsableClub("1".equals(responsableClub));
                        membre.setNumeroClubAft(numeroClubAft);

                        if ("9999".equals(numeroClubCorpo)){
                            membre.setActif(false);
                        }else{
                            // Recuperer le club du membre
                            Club club = clubRepository.findByNumero(numeroClubCorpo);
                            membre.setClub(club);
                        }

                        // Garnir le classement AFT et Corpo actuel du membre : attention pour la mise a jour --> risque de creer des doublons --> a zapper s'il y a deja un classement connu

                        membre = membreRepository.save(membre);

                        if (membre.getClassementCorpoActuel()==null) {
                            if (!StringUtils.isEmpty(pointsCorpo)) {
                                ClassementCorpo classementCorpo = new ClassementCorpo();
                                classementCorpo.setMembreFk(membre.getId());
                                classementCorpo.setDateClassement(new Date());
                                classementCorpo.setPoints(Integer.valueOf(pointsCorpo));
                                classementCorpoRepository.save(classementCorpo);
                                membreRepository.updateClassementCorpo(membre.getId(), classementCorpo);
                            }
                        }

                        if (membre.getClassementAFTActuel()==null){
                            if (!StringUtils.isEmpty(pointsAft)){
                                ClassementAFT classementAFT = new ClassementAFT();
                                classementAFT.setMembreFk(membre.getId());
                                classementAFT.setDateClassement(new Date());
                                classementAFT.setCodeClassement(codeClassementAft);
                                classementAFT.setPoints(Integer.valueOf(pointsAft));
                                classementAFTRepository.save(classementAFT);
                                membreRepository.updateClassementAFT(membre.getId(),classementAFT);
                            }

                        }

                    }catch(Exception e){
                        e.printStackTrace();
                    }

                }
            }
        }

    }

    // FrontEnd
    // Faire les appels deletable/delete/actif a partir de l'UI
    // Ajouter un bouton Anonymisation
    // Ajouter un bouton pour charger un fichier dans le système (popup de demande de fichier --> plus joli)


//    public static void main(String[] args) throws Exception {
//
//        Workbook wb = POIUtils.createWorkbook(new FileInputStream("D:/membres.xls"));
//        Sheet sheet = POIUtils.getSheet(wb,0,false);
//
//        // TODO : si le membre existe deja (en se basant sur le numero Aft) --> update
//
//        List<Membre> membres = new ArrayList<>();
//
//        for (int i=1;i<sheet.getPhysicalNumberOfRows();i++){
//            String numeroAft = POIUtils.readAsString(sheet,i,0);
//            String nom = POIUtils.readAsString(sheet,i,1);
//            String prenom = POIUtils.readAsString(sheet,i,2);
//            Object dateNaissanceObj = POIUtils.readDate(sheet,i,3);
//            String genre = POIUtils.readAsString(sheet,i,4);
//            String rue = POIUtils.readAsString(sheet,i,6);
//            String codePostal = POIUtils.readAsString(sheet,i,7);
//            String localite = POIUtils.readAsString(sheet,i,8);
//            String telephone = POIUtils.readAsString(sheet,i,9);
//            String gsm = POIUtils.readAsString(sheet,i,12);
//            String mail = POIUtils.readAsString(sheet,i,14);
//            String responsableClub = POIUtils.readAsString(sheet,i,16);
//            String classementAft = POIUtils.readAsString(sheet,i,18);
//            String pointsAft = POIUtils.readAsString(sheet,i,19);
//            String pointsCorpo = POIUtils.readAsString(sheet,i,20);
//            String numeroClubCorpo = POIUtils.readAsString(sheet,i,22);
//            String numeroClubAft = POIUtils.readAsString(sheet,i,23);
//
//            if (!StringUtils.isEmpty(numeroAft)){
//
//                //TODO : il y a des doublons dans la base initiale --> traiter lors de l'insertion via un test d'update
//
//                // Considerés comme obligatoires : Nom, prenom, numeroAft (ce dernier pour gerer les doublons)
//
//                Membre membre = new Membre();
//                membre.setNumeroAft(numeroAft);
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
//
//                membre.setRue(rue);
//                membre.setCodePostal(codePostal);
//                membre.setLocalite(localite);
//                membre.setTelephone(telephone);
//                membre.setGsm(gsm);
//                membre.setMail(mail);
//                membre.setResponsableClub("1".equals(responsableClub));
//                membre.setNumeroClubAft(numeroClubAft);
//
//                if ("9999".equals(numeroClubCorpo)){
//                    membre.setActif(false);
//                }else{
//                   // TODO : recuperer le club du membre
//                }
//
//                //TODO : garnir le classement AFT et Corpo actuel du membre : attention pour la mise a jour --> risque de creer des doublons --> a zapper si le membre existe
//
//                membres.add(membre);
//
//            }
//        }
//
//        System.err.println("Nombre de membres : " + membres.size());
//
//    }



}
