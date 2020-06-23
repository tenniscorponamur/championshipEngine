package be.company.fca.controller;

import be.company.fca.model.*;
import be.company.fca.repository.*;
import be.company.fca.service.RencontreService;
import be.company.fca.utils.DateUtils;
import be.company.fca.utils.ReportUtils;
import io.swagger.annotations.Api;
import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des championnats")
public class ChampionnatController {

    @Autowired
    DataSource datasource;

    @Autowired
    private ChampionnatRepository championnatRepository;

    @Autowired
    private DivisionRepository divisionRepository;

    @Autowired
    private RencontreRepository rencontreRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private SetRepository setRepository;

    @Autowired
    private RencontreService rencontreService;

    @RequestMapping(method= RequestMethod.GET, path="/public/championnats")
    public Iterable<Championnat> getChampionnats() {
        return championnatRepository.findAll();
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat", method = RequestMethod.PUT)
    public Championnat updateChampionnat(@RequestBody Championnat championnat){
        try{
            // On va tenter de sauvegarder et la DB va nous signaler si on essaye de creer un doublon
            championnatRepository.updateInfosGenerales(championnat.getId(), championnat.getAnnee(),championnat.getType(),championnat.getCategorie());
            return championnat;
        }catch (Exception e){
            return null;
        }
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat", method = RequestMethod.POST)
    public Championnat addChampionnat(@RequestBody Championnat championnat){
        try{
            // On va tenter de sauvegarder et la DB va nous signaler si on essaye de creer un doublon
            return championnatRepository.save(championnat);
        }catch (Exception e){
            return null;
        }
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat", method = RequestMethod.DELETE)
    public void deleteChampionnat(@RequestParam Long id){
        if (isCalendrierDeletable(id)){
            Championnat championnat = new Championnat();
            championnat.setId(id);
            divisionRepository.deleteByChampionnat(championnat);
            championnatRepository.deleteById(id);
        }
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat/ordre", method = RequestMethod.PUT)
    public boolean updateOrdreChampionnat(@RequestParam Long championnatId,@RequestBody(required = false) Long ordre){
        championnatRepository.updateOrdre(championnatId, ordre);
        return true;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat/calendrierARafraichir", method = RequestMethod.PUT)
    public boolean setCalendrierARafraichir(@RequestParam Long championnatId,@RequestBody boolean aRafraichir){
        championnatRepository.updateCalendrierARafraichir(championnatId,aRafraichir);
        return true;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat/calendrierValide", method = RequestMethod.PUT)
    public boolean setCalendrierValide(@RequestParam Long championnatId,@RequestBody boolean validite){
        if (validite&&isCalendrierValidable(championnatId)){
            championnatRepository.updateCalendrierValide(championnatId,validite);
            return true;
        }else if (!validite && isCalendrierInvalidable(championnatId)){
            championnatRepository.updateCalendrierValide(championnatId,validite);
            return true;
        }
        return false;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat/cloture", method = RequestMethod.PUT)
    public boolean setCloture(@RequestParam Long championnatId,@RequestBody boolean cloture){
        if (cloture && isCloturable(championnatId)){
            championnatRepository.updateChampionnatCloture(championnatId,cloture);
            return true;
        }
        return false;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat/isCriteriumEditable", method = RequestMethod.GET)
    public boolean isCriteriumEditable(@RequestParam String annee){
        List<Championnat> championnats = championnatRepository.findByTypeAndAnnee(TypeChampionnat.CRITERIUM,annee);
        // On considere le criterium comme editable si tous les championnats y relatifs ne sont pas encore clotures
        boolean criteriumEditable = true;
        for (Championnat championnat : championnats){
            criteriumEditable = criteriumEditable && !championnat.isCloture();
        }
        return criteriumEditable;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat/isCalendrierARafraichir", method = RequestMethod.GET)
    public boolean isCalendrierARafraichir(@RequestParam Long championnatId){
        Championnat championnat = championnatRepository.findById(championnatId).get();
        return championnat.isCalendrierARafraichir();
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat/isCalendrierValidable", method = RequestMethod.GET)
    public boolean isCalendrierValidable(@RequestParam Long championnatId){
        Championnat championnat = championnatRepository.findById(championnatId).get();
        if (!championnat.isCalendrierARafraichir() && !championnat.isCalendrierValide()){
            // On peut valider le calendrier si des rencontres existent pour ce championnat
            Long countRencontres = rencontreRepository.countByChampionnat(championnatId);
            return countRencontres > 0;
        }
        return false;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat/isCalendrierInvalidable", method = RequestMethod.GET)
    public boolean isCalendrierInvalidable(@RequestParam Long championnatId){
        // On peut invalider la calendrier si celui-ci est validé
        // et qu'aucun resultat n'a ete encode (ce dernier critere est plutot une precaution
        // car cela devrait egalement fonctionner si un resultat a ete encode)
        Championnat championnat = championnatRepository.findById(championnatId).get();
        Long setCount = setRepository.countByChampionnatId(championnatId);
        return championnat.isCalendrierValide() && setCount==0;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat/isCalendrierDeletable", method = RequestMethod.GET)
    public boolean isCalendrierDeletable(@RequestParam Long championnatId){
        // Le calendrier peut être supprimé si le calendrier n'est pas marqué comme validé
        // et que des rencontres existent (bien que non-indispensable)
        Championnat championnat = championnatRepository.findById(championnatId).get();
        return !championnat.isCalendrierValide();
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/championnat/isCloturable", method = RequestMethod.GET)
    public boolean isCloturable(@RequestParam Long championnatId){
        Championnat championnat = championnatRepository.findById(championnatId).get();
        // Le calendrier peut etre cloture si toutes les rencontres ont ete disputees et validees (interseries comprises) et que le calendrier est valide
        if (championnat.isCalendrierValide() && !championnat.isCloture()){
            Long countRencontresInvalidees = rencontreRepository.countNonValideesByChampionnat(championnatId);
            return countRencontresInvalidees == 0;
        }
        return false;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(path="/private/championnat/listeCapitaines", method= RequestMethod.GET)
    ResponseEntity<byte[]> getListeCapitaines(@RequestParam Long championnatId) throws Exception {
        JasperReport jasperReport = JasperCompileManager.compileReport(ReportUtils.getListeCapitainesTemplate());
        Connection conn = datasource.getConnection();
        Map params = new HashMap();
        params.put("championnatId", championnatId);
        JasperPrint jprint = JasperFillManager.fillReport(jasperReport, params, conn);
        byte[] pdfFile =  JasperExportManager.exportReportToPdf(jprint);
        conn.close();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(pdfFile, headers, HttpStatus.OK);
        return response;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(path="/private/championnat/tableauCriterium", method= RequestMethod.GET)
    ResponseEntity<byte[]> getTableauCriterium(@RequestParam @DateTimeFormat(pattern="yyyyMMdd") Date date) throws Exception {
        TimeZone timeZone = TimeZone.getTimeZone(DateUtils.getTimeZone());
        JasperReport jasperReport = JasperCompileManager.compileReport(ReportUtils.getTableauCriteriumTemplate());
        Connection conn = datasource.getConnection();
        Map params = new HashMap();
        params.put("REPORT_TIME_ZONE", timeZone);
        params.put("date", date);
        JasperPrint jprint = JasperFillManager.fillReport(jasperReport, params, conn);
        byte[] pdfFile =  JasperExportManager.exportReportToPdf(jprint);
        conn.close();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(pdfFile, headers, HttpStatus.OK);
        return response;
    }


    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(path="/private/championnat/tableauCriteriumWithPlayers", method= RequestMethod.GET)
    ResponseEntity<byte[]> getTableauCriteriumWithPlayers(@RequestParam @DateTimeFormat(pattern="yyyyMMdd") Date date) throws Exception {

        // sur base des rencontres prevues le jour concerne
        // Charger les joueurs prevus dans la composition si aucun n'est precise pour l'equipe dans la rencontre

        List<Rencontre> rencontres = rencontreRepository.getRencontresByDate(date);
        for (Rencontre rencontre : rencontres){
            rencontreService.getAndFillMatchs(rencontre);
        }

        TimeZone timeZone = TimeZone.getTimeZone(DateUtils.getTimeZone());
        JasperReport jasperReport = JasperCompileManager.compileReport(ReportUtils.getTableauCriteriumWithPlayersTemplate());
        Connection conn = datasource.getConnection();
        Map params = new HashMap();
        params.put("REPORT_TIME_ZONE", timeZone);
        params.put("date", date);
        JasperPrint jprint = JasperFillManager.fillReport(jasperReport, params, conn);
        byte[] pdfFile =  JasperExportManager.exportReportToPdf(jprint);
        conn.close();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(pdfFile, headers, HttpStatus.OK);
        return response;
    }


//    @RequestMapping(method= RequestMethod.GET, path="/public/championnat/createChampionnat")
//    public Championnat createChampionnat() {
//        Championnat championnat = new Championnat();
//        championnat.setAnnee(2018);
//        championnat.setType(TypeChampionnat.ETE);
//        championnat.setCategorie(CategorieChampionnat.DAMES);
//        championnatRepository.save(championnat);
//        return championnat;
//    }

}
