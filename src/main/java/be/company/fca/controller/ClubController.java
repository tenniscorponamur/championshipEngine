package be.company.fca.controller;

import be.company.fca.model.Club;
import be.company.fca.model.Genre;
import be.company.fca.model.Membre;
import be.company.fca.repository.ClubRepository;
import be.company.fca.repository.EquipeRepository;
import be.company.fca.repository.MembreRepository;
import be.company.fca.utils.DateUtils;
import be.company.fca.utils.POIUtils;
import be.company.fca.utils.TemplateUtils;
import io.swagger.annotations.Api;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.LocaleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.TimeZone;

@RestController
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des clubs")
public class ClubController {

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private MembreRepository membreRepository;

    @Autowired
    private EquipeRepository equipeRepository;

    @RequestMapping(path="/public/clubs", method= RequestMethod.GET)
    Iterable<Club> getAllClubs() {
        return clubRepository.findAll();
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(path="/private/clubs/export", method= RequestMethod.GET)
    ResponseEntity<byte[]> getSituationClubs() throws IOException, InvalidFormatException {

        TimeZone timeZone = TimeZone.getTimeZone(DateUtils.getTimeZone());
        LocaleUtil.setUserTimeZone(timeZone);

        Workbook wb = POIUtils.createWorkbook(true);
        Sheet sheet = wb.createSheet("Situation_clubs");

        POIUtils.write(sheet, 0, 0, "Club", null, null);
        POIUtils.write(sheet, 0, 1, "Adresse", null, null);
        POIUtils.write(sheet, 0, 2, "Hommes (<25)", null, null);
        POIUtils.write(sheet, 0, 3, "Femmes (<25)", null, null);
        POIUtils.write(sheet, 0, 4, "Hommes (<35)", null, null);
        POIUtils.write(sheet, 0, 5, "Femmes (<35)", null, null);
        POIUtils.write(sheet, 0, 6, "Hommes (>=35)", null, null);
        POIUtils.write(sheet, 0, 7, "Femmes (>=35)", null, null);
        POIUtils.write(sheet, 0, 8, "Totaux", null, null);

        int rowId = 1;
        List<Club> clubs = (List<Club>) clubRepository.findAll();
        for (Club club : clubs){
            if (club.isActif()){
                POIUtils.write(sheet, rowId, 0, club.getNumero() + " - " + club.getNom(), null, null);
                POIUtils.write(sheet, rowId, 1, club.getAdresse(), null, null);

                int nbHommesMoins25 = 0;
                int nbFemmesMoins25 = 0;
                int nbHommesMoins35 = 0;
                int nbFemmesMoins35 = 0;
                int nbHommes35 = 0;
                int nbFemmes35 = 0;
                int total = 0;
                List<Membre> membres = (List<Membre>) membreRepository.findByClub(club);
                for (Membre membre : membres){
                    if (membre.isActif()){
                        if (membre.getDateNaissance()!=null){
                            int age = DateUtils.getYearsDifference(membre.getDateNaissance());
                            if (Genre.HOMME.equals(membre.getGenre())){
                                if (age<25){
                                    nbHommesMoins25++;
                                }else if (age<35){
                                    nbHommesMoins35++;
                                }else{
                                    nbHommes35++;
                                }
                            }else{
                                if (age<25){
                                    nbFemmesMoins25++;
                                }else if (age<35){
                                    nbFemmesMoins35++;
                                }else{
                                    nbFemmes35++;
                                }
                            }
                        }
                        total++;
                    }
                }

                POIUtils.write(sheet, rowId, 2, nbHommesMoins25, null, null);
                POIUtils.write(sheet, rowId, 3, nbFemmesMoins25, null, null);
                POIUtils.write(sheet, rowId, 4, nbHommesMoins35, null, null);
                POIUtils.write(sheet, rowId, 5, nbFemmesMoins35, null, null);
                POIUtils.write(sheet, rowId, 6, nbHommes35, null, null);
                POIUtils.write(sheet, rowId, 7, nbFemmes35, null, null);
                POIUtils.write(sheet, rowId, 8, total, null, null);

                rowId++;
            }
        }

        // Auto-resize des colonnes
        for (int i = 0; i < 9; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb.write(os);
        wb.close();
        os.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(os.toByteArray(), headers, HttpStatus.OK);
        return response;
    }

        @RequestMapping(path="/public/club", method= RequestMethod.GET)
    Club getClub(@RequestParam Long id) {
        return clubRepository.findById(id).get();
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(path="/private/club/{clubId}/export", method= RequestMethod.GET)
    ResponseEntity<byte[]> getClubInfos(@PathVariable("clubId") Long clubId) throws IOException, InvalidFormatException {

        Club club = clubRepository.findById(clubId).get();

        TimeZone timeZone = TimeZone.getTimeZone(DateUtils.getTimeZone());
        LocaleUtil.setUserTimeZone(timeZone);

        Workbook wb = POIUtils.createWorkbook(TemplateUtils.getTemplateLigue());

        CreationHelper createHelper = wb.getCreationHelper();
        CellStyle dateCellStyle = wb.createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));

        Sheet sheet = wb.getSheetAt(0);

        POIUtils.write(sheet,8,2,club.getNom(),null,null);
        POIUtils.write(sheet,9,2,club.getAdresse(),null,null);

        List<Membre> responsables = (List<Membre>) membreRepository.findByClubAndResponsableClubAndActif(club,true,true);
        if (responsables.size()>0){
            Membre responsable = responsables.get(0);
            String nomPrenomResponsable = responsable.getNom() + " " + responsable.getPrenom();
            String adresseResponsable = (responsable.getRue()==null?"":responsable.getRue()) + " " + (responsable.getRueNumero()==null?"":responsable.getRueNumero())
                    + "," + (responsable.getCodePostal()==null?"":responsable.getCodePostal()) + " " + (responsable.getLocalite()==null?"":responsable.getLocalite());

            POIUtils.write(sheet,15,2,nomPrenomResponsable,null,null);
            POIUtils.write(sheet,16,2,adresseResponsable,null,null);

            POIUtils.write(sheet,21,2,nomPrenomResponsable,null,null);
            POIUtils.write(sheet,22,2,adresseResponsable,null,null);

            POIUtils.write(sheet,26,2,nomPrenomResponsable,null,null);
            POIUtils.write(sheet,27,2,adresseResponsable,null,null);

            POIUtils.write(sheet,43,2,nomPrenomResponsable,null,null);
            POIUtils.write(sheet,44,2,adresseResponsable,null,null);
        }
        Long nbMembresClubs = membreRepository.countByClubAndActif(club,true);

        POIUtils.write(sheet,47,2,club.getDateCreation(),dateCellStyle,null);
        POIUtils.write(sheet,50,2,nbMembresClubs,null,null);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb.write(os);
        wb.close();
        os.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(os.toByteArray(), headers, HttpStatus.OK);
        return response;

    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/club", method = RequestMethod.PUT)
    public Club updateClub(@RequestBody Club club){
        return clubRepository.save(club);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/club", method = RequestMethod.POST)
    public Club addClub(@RequestBody Club club){
        return clubRepository.save(club);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(path="/private/club/{clubId}/deletable", method= RequestMethod.GET)
    public boolean isClubDeletable(@PathVariable("clubId") Long clubId){
        Club club = new Club();
        club.setId(clubId);

        // Faire des counts pour savoir si le club n'a pas de reference
        // --> pas d'equipe, pas de membre

        long count = equipeRepository.countByClub(club);
        if (count==0){
            count = membreRepository.countByClub(club);
            return count==0;
        }
        return false;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/club", method = RequestMethod.DELETE)
    public void deleteClub(@RequestParam Long clubId){
        if (isClubDeletable(clubId)){
            clubRepository.deleteById(clubId);
        }
    }
}
