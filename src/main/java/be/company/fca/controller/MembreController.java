package be.company.fca.controller;

import be.company.fca.dto.MembreDto;
import be.company.fca.exceptions.ForbiddenException;
import be.company.fca.model.*;
import be.company.fca.repository.*;
import be.company.fca.service.UserService;
import be.company.fca.utils.*;
import io.swagger.annotations.Api;
import net.sf.jasperreports.engine.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
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
    private MembreEquipeRepository membreEquipeRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private ClassementCorpoRepository classementCorpoRepository;

    @Autowired
    private ClassementAFTRepository classementAFTRepository;

    @Autowired
    private UserService userService;

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



        boolean userConnected = authentication!=null;
        boolean adminConnected = userService.isAdmin(authentication);
        Membre membreConnecte = userService.getMembreFromAuthentication(authentication);

        /* Les informations privees sont retournes si
            - l'utilisateur connecte est admnistrateur
            - l'utilisateur connecte et qu'il s'agit de lui-meme
            - l'utilisateur connecte est responsable du club du membre
            - il y a un utilisateur connecte et que le membre est capitaine ou responsable de club

            Dans un premier temps, les membres inactifs ne seront retournes que pour les administrateurs
        */

        for (Membre membre : membres){
            // Ne recuperer que les vrais membres --> retirer les fictifs
            if (!membre.isFictif()){
                boolean informationsMembreAccessibles = adminConnected || isMe(membreConnecte,membre) || isResponsableMembre(membreConnecte, membre);
                boolean contactsAccessibles = informationsMembreAccessibles || (userConnected && (membre.isResponsableClub() || membre.isCapitaine()));
                if (adminConnected || membre.isActif()){
                    membresDto.add(new MembreDto(membre,informationsMembreAccessibles,contactsAccessibles));
                }
            }
        }

        return membresDto;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(method= RequestMethod.GET, path="/private/membre")
    public MembreDto findMembreByNumeroAft(@RequestParam String numeroAft){
        Membre membre = membreRepository.findByNumeroAft(numeroAft);
        if (membre!=null){
            return new MembreDto(membre,false,false);
        }
        return null;
    }

    /**
     * Permet de savoir si un membre connecte correspond a un autre membre (en se basant sur l'id)
     * @param membreConnecte
     * @param otherMembre
     * @return true si le membreConnecte est l'autre membre (en se basant sur l'id)
     */
    private boolean isMe(Membre membreConnecte, Membre otherMembre){
        return membreConnecte!=null && membreConnecte.getId().equals(otherMembre.getId());
    }

    /**
     * Permet de savoir si un membre est responsable du club d'un autre membre
     * @param membreParent
     * @param otherMembre
     * @return true si le membreParent est responsable du club de l'autre membre
     */
    private boolean isResponsableMembre(Membre membreParent, Membre otherMembre){
        if (membreParent!=null && membreParent.isResponsableClub()){
            if (membreParent.getClub()!=null && membreParent.getClub().equals(otherMembre.getClub())){
                return true;
            }
        }
        return false;
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

    //@PreAuthorize("hasAnyAuthority('ADMIN_USER','RESPONSABLE_CLUB')")
    @RequestMapping(value = "/private/membre/{membreId}/coordonnees", method = RequestMethod.PUT)
    public Membre updateCoordonnees(Authentication authentication, @PathVariable("membreId") Long membreId, @RequestBody Membre membre){
        boolean autorise = privateInformationsEditables(authentication,membreId);

        // Admin et responsable de club autorises
        if (autorise){

            membreRepository.updateCoordonnees(membreId,
                    membre.getCodePostal(),
                    membre.getLocalite(),
                    membre.getRue(),
                    membre.getRueNumero(),
                    membre.getRueBoite()
            );
            return membre;

        }else{
            throw new ForbiddenException();
        }


    }

    //@PreAuthorize("hasAnyAuthority('ADMIN_USER','RESPONSABLE_CLUB')")
    @RequestMapping(value = "/private/membre/{membreId}/contacts", method = RequestMethod.PUT)
    public Membre updateContacts(Authentication authentication, @PathVariable("membreId") Long membreId, @RequestBody Membre membre){
        boolean autorise = privateInformationsEditables(authentication,membreId);

        // Admin et responsable de club autorises
        if (autorise){
            membreRepository.updateContacts(membreId,
                    membre.getTelephone(),
                    membre.getGsm(),
                    membre.getMail()
            );
            return membre;
        }else{
            throw new ForbiddenException();
        }
    }

    //@PreAuthorize("hasAnyAuthority('ADMIN_USER','RESPONSABLE_CLUB')")
    @RequestMapping(value = "/private/membre/{membreId}/infosLimiteesAft", method = RequestMethod.PUT)
    public ClassementAFT updateInfosAft(Authentication authentication, @PathVariable("membreId") Long membreId,
                                  @RequestParam(required = false) String codeClassementAft,
                                  @RequestParam(required = false) String numeroClubAft,
                                  @RequestParam boolean onlyCorpo){
        boolean autorise = privateInformationsEditables(authentication,membreId);

        // Admin et responsable de club autorises
        if (autorise){

            Membre membre = membreRepository.findById(membreId).get();
            membre.setOnlyCorpo(onlyCorpo);
            membre.setNumeroClubAft(numeroClubAft);
            membreRepository.save(membre);

            ClassementAFT newClassementAFT = null;
            if (codeClassementAft!=null) {
                EchelleAFT echelleAFT = EchelleAFT.getEchelleAFTByCode(codeClassementAft);
                if (echelleAFT != null) {
                    newClassementAFT = new ClassementAFT();
                    newClassementAFT.setMembreFk(membreId);
                    newClassementAFT.setDateClassement(new Date());
                    newClassementAFT.setCodeClassement(echelleAFT.getCode());
                    newClassementAFT.setPoints(echelleAFT.getPoints());

                    // sauvegarde du nouveau classmeent (dans la liste du membre et en tant que classmenet actuel)
                    newClassementAFT = classementAFTRepository.save(newClassementAFT);
                    membreRepository.updateClassementAFT(membreId, newClassementAFT);
                }
            }

            return newClassementAFT;
        }else{
            throw new ForbiddenException();
        }
    }

    /**
     * Permet de savoir si un utilisateur connecte peut modifier les informations privees d'un membre
     * Seul un administrateur ou un responsable du club du membre peuvent les modifier
     * @return
     */
    private boolean privateInformationsEditables(Authentication authentication, Long membreId){
        boolean autorise = false;
        boolean adminConnected = userService.isAdmin(authentication);
        if (adminConnected) {
            autorise = true;
        }else{
            Membre membreConnecte = userService.getMembreFromAuthentication(authentication);
            if (membreConnecte!=null) {
                //S'il s'agit de sa propre personne, on autorise
                if (membreConnecte.getId().equals(membreId)) {
                    autorise=true;
                // S'il s'agit du responsable du club, on autorise egalement
                }else if (membreConnecte.isResponsableClub() && membreConnecte.getClub()!=null) {
                    Membre membreExistant = membreRepository.findById(membreId).get();
                    if (membreExistant.getClub() != null) {
                        autorise = membreExistant.getClub().equals(membreConnecte.getClub());
                    }
                }
            }
        }
        return autorise;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/membre/{membreId}/infosAft", method = RequestMethod.PUT)
    public Membre updateInfosAftByAdmin(@PathVariable("membreId") Long membreId, @RequestBody Membre membre){
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

        Membre membre = membreRepository.findById(membreId).get();
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
    @RequestMapping(value = "/private/membre/{membreId}/resetPassword", method = RequestMethod.POST)
    public boolean resetMemberPassword(@PathVariable("membreId") Long membreId){
        Membre membre = membreRepository.findById(membreId).get();
        if (!StringUtils.isEmpty(membre.getMail())){
            String newPassword = PasswordUtils.generatePassword();
            boolean mailSended = MailUtils.sendPasswordMail(membre.getPrenom(),membre.getNom(),membre.getMail(),newPassword);
            if (mailSended){
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                membre.setPassword(encoder.encode(newPassword));
                membreRepository.save(membre);
                return true;
            }
        }
        return false;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/membre/{membreId}/setNewPassword", method = RequestMethod.POST)
    public String setNewMemberPassword(@PathVariable("membreId") Long membreId){
        Membre membre = membreRepository.findById(membreId).get();
        String newPassword = PasswordUtils.generatePassword();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        membre.setPassword(encoder.encode(newPassword));
        membreRepository.save(membre);
        return "{\"password\":\"" + newPassword + "\"}";
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/membre", method = RequestMethod.POST)
    public Membre addMembre(@RequestBody Membre membre){
        Membre newMembre = new Membre();
        newMembre.setAdhesionPolitique(true);
        newMembre.setPassword(PasswordUtils.DEFAULT_MEMBER_PASSWORD);
        newMembre.setGenre(membre.getGenre());
        newMembre.setPrenom(membre.getPrenom());
        newMembre.setNom(membre.getNom());
        newMembre.setDateNaissance(membre.getDateNaissance());
        membreRepository.save(newMembre);
        return newMembre;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(path="/private/membre/{membreId}/deletable", method= RequestMethod.GET)
    public boolean isMembreDeletable(@PathVariable("membreId") Long membreId){

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
        if (isMembreDeletable(membreId)){

            // Supprimer le membre des compos d'equipe auxquelles il appartient
            membreEquipeRepository.deleteByMembreFk(membreId);

            membreRepository.updateClassementCorpo(membreId,null);
            membreRepository.updateClassementAFT(membreId,null);
            classementCorpoRepository.deleteByMembreFk(membreId);
            classementAFTRepository.deleteByMembreFk(membreId);
            membreRepository.deleteById(membreId);
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

    @RequestMapping(path="/public/membres/listeForcePoints", method= RequestMethod.GET)
    ResponseEntity<byte[]> getListeForceMembresParPoints() throws Exception {
        JasperReport jasperReport = JasperCompileManager.compileReport(ReportUtils.getListeForcePointsTemplate());
        Connection conn = datasource.getConnection();
        JasperPrint jprint = JasperFillManager.fillReport(jasperReport, new HashMap(), conn);
        byte[] pdfFile =  JasperExportManager.exportReportToPdf(jprint);
        conn.close();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(pdfFile, headers, HttpStatus.OK);
        return response;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN_USER','RESPONSABLE_CLUB')")
    @RequestMapping(path="/private/membres/exportByClub", method= RequestMethod.GET)
    ResponseEntity<byte[]> getExportMembresByClub(Authentication authentication, @RequestParam Long clubId) throws Exception {
        boolean adminConnected = userService.isAdmin(authentication);

        Club club = null;
        if (adminConnected){
            club = clubRepository.findById(clubId).get();
        }else{
            Membre membreConnecte = userService.getMembreFromAuthentication(authentication);
            club = membreConnecte.getClub();
        }

        if (club!=null) {
            List<Membre> membres = (List<Membre>) membreRepository.findByClub(club);

            Collections.sort(membres, new Comparator<Membre>() {
                @Override
                public int compare(Membre o1, Membre o2) {
                    int compareNom = o1.getNom().compareTo(o2.getNom());
                    if (compareNom != 0) {
                        return compareNom;
                    }
                    return o1.getPrenom().compareTo(o2.getPrenom());
                }
            });

            Workbook wb = POIUtils.createWorkbook(true);
            Sheet sheet = wb.createSheet("Membres_Club_" + club.getNumero() + "_" + new SimpleDateFormat("yyyyMMddhhmm").format(new Date()));

            CreationHelper createHelper = wb.getCreationHelper();
            CellStyle dateCellStyle = wb.createCellStyle();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));

            CellStyle boldStyle = wb.createCellStyle();
            Font boldFont = wb.createFont();
            boldFont.setBold(true);
            boldFont.setColor(IndexedColors.ORANGE.index);
            boldStyle.setFont(boldFont);

            Cell firstCell = POIUtils.write(sheet, 0, 0, "Numéro AFT", null, null);
            POIUtils.write(sheet, 0, 1, "Nom", null, null);
            POIUtils.write(sheet, 0, 2, "Prénom", null, null);
            POIUtils.write(sheet, 0, 3, "Date de naissance", null, null);
            POIUtils.write(sheet, 0, 4, "Genre", null, null);
            POIUtils.write(sheet, 0, 5, "Rue", null, null);
            POIUtils.write(sheet, 0, 6, "Numéro", null, null);
            POIUtils.write(sheet, 0, 7, "Boite", null, null);
            POIUtils.write(sheet, 0, 8, "Code postal", null, null);
            POIUtils.write(sheet, 0, 9, "Localite", null, null);
            POIUtils.write(sheet, 0, 10, "Téléphone", null, null);
            POIUtils.write(sheet, 0, 11, "Gsm", null, null);
            POIUtils.write(sheet, 0, 12, "Mail", null, null);
            POIUtils.write(sheet, 0, 13, "Responsable de club", null, null);
            POIUtils.write(sheet, 0, 14, "Classement AFT", null, null);
            POIUtils.write(sheet, 0, 15, "Points AFT", null, null);
            POIUtils.write(sheet, 0, 16, "Points Corpo", null, null);
            POIUtils.write(sheet, 0, 17, "Numéro Corporation", null, null);
            Cell lastCell = POIUtils.write(sheet, 0, 18, "Numéro club AFT", null, null);


            // Pour faire simple, faire la liste avec les membres actifs pour iterer dessus
            List<Membre> exportedMembres = new ArrayList<>();
            for (Membre membre : membres){
                // On ne recupere pas les membres actifs
                if (membre.isActif()) {
                    exportedMembres.add(membre);
                }
            }

            for (int i = 0; i < exportedMembres.size(); i++) {
                Membre membre = exportedMembres.get(i);

                lastCell = POIUtils.write(sheet, i + 1, 0, membre.getNumeroAft(), null, null);
                lastCell = POIUtils.write(sheet, i + 1, 1, membre.getNom(), null, null);
                lastCell = POIUtils.write(sheet, i + 1, 2, membre.getPrenom(), null, null);
                if (membre.getDateNaissance() != null) {
                    lastCell = POIUtils.write(sheet, i + 1, 3, membre.getDateNaissance(), dateCellStyle, null);
                }
                lastCell = POIUtils.write(sheet, i + 1, 4, membre.getGenre() == Genre.FEMME ? "F" : "M", null, null);

                lastCell = POIUtils.write(sheet, i + 1, 5, membre.getRue(), null, null);
                lastCell = POIUtils.write(sheet, i + 1, 6, membre.getRueNumero(), null, null);
                lastCell = POIUtils.write(sheet, i + 1, 7, membre.getRueBoite(), null, null);
                lastCell = POIUtils.write(sheet, i + 1, 8, membre.getCodePostal(), null, null);
                lastCell = POIUtils.write(sheet, i + 1, 9, membre.getLocalite(), null, null);
                lastCell = POIUtils.write(sheet, i + 1, 10, membre.getTelephone(), null, null);
                lastCell = POIUtils.write(sheet, i + 1, 11, membre.getGsm(), null, null);
                lastCell = POIUtils.write(sheet, i + 1, 12, membre.getMail(), null, null);

                lastCell = POIUtils.write(sheet, i + 1, 13, membre.isResponsableClub() ? "1" : "0", null, null);

                if (membre.getClassementAFTActuel() != null) {
                    lastCell = POIUtils.write(sheet, i + 1, 14, membre.getClassementAFTActuel().getCodeClassement(), null, null);
                    lastCell = POIUtils.write(sheet, i + 1, 15, membre.getClassementAFTActuel().getPoints(), null, null);
                }

                if (membre.getClassementCorpoActuel() != null) {
                    lastCell = POIUtils.write(sheet, i + 1, 16, membre.getClassementCorpoActuel().getPoints(), null, null);
                }

                if (membre.getClub() != null) {
                    lastCell = POIUtils.write(sheet, i + 1, 17, membre.getClub().getNumero(), null, null);
                }
                lastCell = POIUtils.write(sheet, i + 1, 18, membre.getNumeroClubAft(), null, null);
                // Mise en evidence des affilies corpo (club AFT = 6045)
                Cell numClubAFTCell = lastCell;
                if (membre.getNumeroClubAft() != null && "6045".equals(membre.getNumeroClubAft().trim())) {
                    numClubAFTCell.setCellStyle(boldStyle);
                }

            }

            // Freeze de la premiere ligne
            sheet.createFreezePane(0, 1);

            // Auto-resize des colonnes
            for (int i = 0; i < 31; i++) {
                sheet.autoSizeColumn(i);
            }

            // Filtre defini pour la plage de cellules remplies
            sheet.setAutoFilter(new CellRangeAddress(firstCell.getRowIndex(), lastCell.getRowIndex(), firstCell.getColumnIndex(), lastCell.getColumnIndex()));

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            wb.write(os);
            wb.close();
            os.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(os.toByteArray(), headers, HttpStatus.OK);
            return response;

        }

        return null;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(path="/private/membres/export", method= RequestMethod.GET)
    ResponseEntity<byte[]> getExportMembres() throws Exception {

        List<Membre> membres = (List<Membre>) membreRepository.findAll(Sort.by("nom","prenom"));

        Workbook wb = POIUtils.createWorkbook(true);
        Sheet sheet  = wb.createSheet("Membres"+new SimpleDateFormat("yyyyMMddhhmm").format(new Date()));

        CreationHelper createHelper = wb.getCreationHelper();
        CellStyle dateCellStyle = wb.createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));

        CellStyle boldStyle = wb.createCellStyle();
        Font boldFont = wb.createFont();
        boldFont.setBold(true);
        boldFont.setColor(IndexedColors.ORANGE.index);
        boldStyle.setFont(boldFont);

        Cell firstCell = POIUtils.write(sheet,0,0,"Nom",null,null);
        POIUtils.write(sheet,0,1,"Prénom",null,null);
        int dateNaissanceIndex = 2;
        POIUtils.write(sheet,0,dateNaissanceIndex,"Date de naissance",null,null);
        POIUtils.write(sheet,0,3,"Age",null,null);
        POIUtils.write(sheet,0,4,"Genre",null,null);
        POIUtils.write(sheet,0,5,"Actif",null,null);
        POIUtils.write(sheet,0,6,"Numéro AFT",null,null);
        POIUtils.write(sheet,0,7,"Capitaine",null,null);
        POIUtils.write(sheet,0,8,"Responsable de club",null,null);
        POIUtils.write(sheet,0,9,"Numéro Corporation",null,null);
        POIUtils.write(sheet,0,10,"Corporation",null,null);
        POIUtils.write(sheet,0,11,"Date affiliation AFT",null,null);
        POIUtils.write(sheet,0,12,"Numéro club AFT",null,null);
        POIUtils.write(sheet,0,13,"Uniquement Corpo",null,null);
        POIUtils.write(sheet,0,14,"Date affiliation Corpo",null,null);
        POIUtils.write(sheet,0,15,"Date désaffiliation Corpo",null,null);
        POIUtils.write(sheet,0,16,"Points Corpo",null,null);
        POIUtils.write(sheet,0,17,"Classement AFT",null,null);
        POIUtils.write(sheet,0,18,"Points AFT",null,null);
        POIUtils.write(sheet,0,19,"Rue",null,null);
        POIUtils.write(sheet,0,20,"Numéro",null,null);
        POIUtils.write(sheet,0,21,"Boite",null,null);
        POIUtils.write(sheet,0,22,"Code postal",null,null);
        POIUtils.write(sheet,0,23,"Localite",null,null);
        POIUtils.write(sheet,0,24,"Grand NAMUR",null,null);
        POIUtils.write(sheet,0,25,"Province de NAMUR",null,null);
        POIUtils.write(sheet,0,26,"Téléphone",null,null);
        POIUtils.write(sheet,0,27,"Gsm",null,null);
        Cell lastCell =POIUtils.write(sheet,0,28,"Mail",null,null);

        POIUtils.write(sheet,0,29,"Date pivot",null,null);
        Cell datePivotCell = POIUtils.write(sheet,0,30,new Date(),dateCellStyle,null);

        // Pour faire simple, faire la liste avec les membres non-fictifs pour iterer dessus
        List<Membre> exportedMembres = new ArrayList<>();
        for (Membre membre : membres){
            // On ne recupere pas les membres fictifs
            if (!membre.isFictif()) {
                exportedMembres.add(membre);
            }
        }

        for (int i=0;i<exportedMembres.size();i++){
            Membre membre = exportedMembres.get(i);

            lastCell = POIUtils.write(sheet,i+1,0,membre.getNom(),null,null);
            lastCell = POIUtils.write(sheet,i+1,1,membre.getPrenom(),null,null);
            if (membre.getDateNaissance()!=null){
                lastCell = POIUtils.write(sheet,i+1,2,membre.getDateNaissance(),dateCellStyle,null);
                String formula= "DATEDIF(" + CellReference.convertNumToColString(dateNaissanceIndex) + (i+2)+ "," + CellReference.convertNumToColString(datePivotCell.getColumnIndex())+"1" + ",\"y\")";
                lastCell = POIUtils.writeWithFormula(sheet,i+1,3,formula);
            }
            lastCell = POIUtils.write(sheet,i+1,4,membre.getGenre().toString(),null,null);
            lastCell = POIUtils.write(sheet,i+1,5,membre.isActif(),null,null);
            lastCell = POIUtils.write(sheet,i+1,6,membre.getNumeroAft(),null,null);
            lastCell = POIUtils.write(sheet,i+1,7,membre.isCapitaine(),null,null);
            lastCell = POIUtils.write(sheet,i+1,8,membre.isResponsableClub(),null,null);
            if (membre.getClub()!=null){
                lastCell = POIUtils.write(sheet,i+1,9,membre.getClub().getNumero(),null,null);
                lastCell = POIUtils.write(sheet,i+1,10,membre.getClub().getNom(),null,null);
            }
            lastCell = POIUtils.write(sheet,i+1,11,membre.getDateAffiliationAft(),dateCellStyle,null);
            lastCell = POIUtils.write(sheet,i+1,12,membre.getNumeroClubAft(),null,null);
            Cell numClubAFTCell = lastCell;
            if (membre.getNumeroClubAft()!=null && "6045".equals(membre.getNumeroClubAft().trim())){
                numClubAFTCell.setCellStyle(boldStyle);
            }
            lastCell = POIUtils.write(sheet,i+1,13,membre.isOnlyCorpo(),null,null);
            lastCell = POIUtils.write(sheet,i+1,14,membre.getDateAffiliationCorpo(),dateCellStyle,null);
            lastCell = POIUtils.write(sheet,i+1,15,membre.getDateDesaffiliationCorpo(),dateCellStyle,null);
            if (membre.getClassementCorpoActuel()!=null){
                lastCell = POIUtils.write(sheet,i+1,16,membre.getClassementCorpoActuel().getPoints(),null,null);
            }
            if (membre.getClassementAFTActuel()!=null){
                lastCell = POIUtils.write(sheet,i+1,17,membre.getClassementAFTActuel().getCodeClassement(),null,null);
                lastCell = POIUtils.write(sheet,i+1,18,membre.getClassementAFTActuel().getPoints(),null,null);
            }
            lastCell = POIUtils.write(sheet,i+1,19,membre.getRue(),null,null);
            lastCell = POIUtils.write(sheet,i+1,20,membre.getRueNumero(),null,null);
            lastCell = POIUtils.write(sheet,i+1,21,membre.getRueBoite(),null,null);
            lastCell = POIUtils.write(sheet,i+1,22,membre.getCodePostal(),null,null);
            lastCell = POIUtils.write(sheet,i+1,23,membre.getLocalite(),null,null);
            lastCell = POIUtils.write(sheet,i+1,24, LocaliteUtils.isGrandNamur(membre.getCodePostal()),null,null);
            lastCell = POIUtils.write(sheet,i+1,25,LocaliteUtils.isProvinceNamur(membre.getCodePostal()),null,null);
            lastCell = POIUtils.write(sheet,i+1,26,membre.getTelephone(),null,null);
            lastCell = POIUtils.write(sheet,i+1,27,membre.getGsm(),null,null);
            lastCell = POIUtils.write(sheet,i+1,28,membre.getMail(),null,null);

        }

        // Freeze de la premiere ligne
        sheet.createFreezePane(0, 1);

        // Auto-resize des colonnes
        for (int i=0;i<31;i++){
            sheet.autoSizeColumn(i);
        }

        // Filtre defini pour la plage de cellules remplies
        sheet.setAutoFilter(new CellRangeAddress(firstCell.getRowIndex(), lastCell.getRowIndex(), firstCell.getColumnIndex(), lastCell.getColumnIndex()));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb.write(os);
        wb.close();
        os.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(os.toByteArray(), headers, HttpStatus.OK);
        return response;

    }


    @RequestMapping(path="/public/membres/import/template", method= RequestMethod.GET)
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
                String numero = POIUtils.readAsString(sheet,i,6);
                String boite = POIUtils.readAsString(sheet,i,7);
                String codePostal = POIUtils.readAsString(sheet,i,8);
                String localite = POIUtils.readAsString(sheet,i,9);
                String telephone = POIUtils.readAsString(sheet,i,10);
                String gsm = POIUtils.readAsString(sheet,i,11);
                String mail = POIUtils.readAsString(sheet,i,12);
                String responsableClub = POIUtils.readAsString(sheet,i,13);
                String codeClassementAft = POIUtils.readAsString(sheet,i,14);
                String pointsAft = POIUtils.readAsString(sheet,i,15);
                String pointsCorpo = POIUtils.readAsString(sheet,i,16);
                String numeroClubCorpo = POIUtils.readAsString(sheet,i,17);
                String numeroClubAft = POIUtils.readAsString(sheet,i,18);

                if (!StringUtils.isEmpty(numeroAft)){

                    try{

                        // Considerés comme obligatoires : Nom, prenom, numeroAft (ce dernier pour gerer les doublons)

                        // Si le membre existe deja (en se basant sur le numero Aft) --> update
                        Membre membre = membreRepository.findByNumeroAft(numeroAft);
                        if (membre==null){
                            membre = new Membre();
                            membre.setPassword(PasswordUtils.DEFAULT_MEMBER_PASSWORD);
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
                        membre.setRueNumero(numero);
                        membre.setRueBoite(boite);
                        membre.setCodePostal(codePostal);
                        membre.setLocalite(localite);
                        membre.setTelephone(telephone);
                        membre.setGsm(gsm);
                        membre.setMail(mail);
                        membre.setResponsableClub("1".equals(responsableClub));
                        membre.setNumeroClubAft(numeroClubAft);

                        membre.setOnlyCorpo("6045".equals(numeroClubAft));

                        if (membre.getDateAffiliationCorpo()==null){
                            membre.setDateAffiliationCorpo(new Date());
                        }

                        if ("9999".equals(numeroClubCorpo)){
                            membre.setActif(false);
                        }else{

                            if (numeroClubCorpo!=null){

                                // Petite astuce mise en place pour trouver les clubs dont la codification est 05
                                // qui sera represente par 5 dans le fichier Excel

                                boolean testWithPrefixeZero = false;
                                try{
                                    Integer numeroClubCorpoInt = Integer.valueOf(numeroClubCorpo);
                                    if (numeroClubCorpoInt<10){
                                        testWithPrefixeZero = true;
                                    }
                                }catch (Exception e){
                                }

                                // Recuperer le club du membre
                                Club club = clubRepository.findByNumero(numeroClubCorpo);
                                if (testWithPrefixeZero && club==null){
                                    club = clubRepository.findByNumero("0" + numeroClubCorpo);
                                }
                                membre.setClub(club);

                            }
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

                        }else{
                            // On va analyser si le classement connu est identique a celui precise dans le fichier

                            if (!StringUtils.isEmpty(pointsAft)){
                                try{
                                    // S'il est different, on va l'importer
                                    if (!membre.getClassementAFTActuel().getPoints().equals(Integer.valueOf(pointsAft))){
                                        ClassementAFT classementAFT = new ClassementAFT();
                                        classementAFT.setMembreFk(membre.getId());
                                        classementAFT.setDateClassement(new Date());
                                        classementAFT.setCodeClassement(codeClassementAft);
                                        classementAFT.setPoints(Integer.valueOf(pointsAft));
                                        classementAFTRepository.save(classementAFT);
                                        membreRepository.updateClassementAFT(membre.getId(),classementAFT);
                                    }

                                }catch (Exception e){
                                    System.err.println("Impossible de sauvegarder le nouveau classement AFT de " + membre.getNumeroAft());
                                    e.printStackTrace();
                                }
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
