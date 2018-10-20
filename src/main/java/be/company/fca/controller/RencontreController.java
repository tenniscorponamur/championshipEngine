package be.company.fca.controller;

import be.company.fca.dto.RencontreDto;
import be.company.fca.model.*;
import be.company.fca.repository.*;
import be.company.fca.service.ClassementService;
import be.company.fca.service.RencontreService;
import be.company.fca.utils.DateUtils;
import be.company.fca.utils.POIUtils;
import be.company.fca.utils.ReportUtils;
import io.swagger.annotations.Api;
import net.sf.jasperreports.engine.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LocaleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.util.*;
import java.util.Set;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des rencontres")
public class RencontreController {

    @Autowired
    DataSource datasource;
    @Autowired
    private RencontreRepository rencontreRepository;
    @Autowired
    private EquipeRepository equipeRepository;
    @Autowired
    private DivisionRepository divisionRepository;
    @Autowired
    private PouleRepository pouleRepository;
    @Autowired
    private ChampionnatRepository championnatRepository;

    @Autowired
    private ClassementService classementService;
    @Autowired
    private RencontreService rencontreService;

    // DTO pour les capitaines d'equipe afin de ne pas recuperer les donnees privees

    @RequestMapping(method = RequestMethod.GET, path = "/public/rencontres")
    public List<RencontreDto> getRencontresByDivisionOrPouleOrEquipe(@RequestParam Long divisionId, @RequestParam(required = false) Long pouleId, @RequestParam(required = false) Long equipeId) {
        List<RencontreDto> rencontresDto = new ArrayList<>();
        List<Rencontre> rencontres = new ArrayList<Rencontre>();

        if (equipeId != null) {
            Equipe equipe = new Equipe();
            equipe.setId(equipeId);
            rencontres = (List<Rencontre>) rencontreRepository.findRencontresByEquipe(equipe);
        } else if (pouleId != null) {
            Poule poule = new Poule();
            poule.setId(pouleId);
            rencontres = (List<Rencontre>) rencontreRepository.findByPoule(poule);
        } else {
            Division division = new Division();
            division.setId(divisionId);
            rencontres = (List<Rencontre>) rencontreRepository.findByDivision(division);
        }

        for (Rencontre rencontre : rencontres) {
            rencontresDto.add(new RencontreDto(rencontre));
        }

        return rencontresDto;
    }

    /**
     * Permet de recuperer les 5 derniers resultats encodes (rencontres validees) dans le systeme
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = "/public/rencontres/last")
    public List<Rencontre> getLastTenResults(@RequestParam Integer numberOfResults) {
        Pageable pageRequest = new PageRequest(0, numberOfResults);
        return rencontreRepository.getLastResults(pageRequest);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/public/rencontres/next")
    public List<Rencontre> getComingMeetings(@RequestParam Integer numberOfResults) {
        Pageable pageRequest = new PageRequest(0, numberOfResults);
        return rencontreRepository.getNextMeetings(DateUtils.shrinkToDay(new Date()), pageRequest);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/public/rencontres/byDate")
    public List<Rencontre> getComingMeetings(@RequestParam @DateTimeFormat(pattern = "yyyyMMdd") Date date) {
        return rencontreRepository.getRencontresByDate(date);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/rencontre", method = RequestMethod.POST)
    public Rencontre createRencontre(@RequestBody Rencontre rencontre) {
        return rencontreRepository.save(rencontre);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/rencontre", method = RequestMethod.PUT)
    public Rencontre updateRencontre(@RequestBody Rencontre rencontre) {
        return rencontreRepository.save(rencontre);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/rencontre/{rencontreId}/isResultatsModifiables", method = RequestMethod.GET)
    public boolean isResultatsRencontreModifiables(@PathVariable("rencontreId") Long rencontreId) {

        //TODO : tester le fait d'etre capitaine de l'equipe visites ou resp. visites ou admin
        //TODO : factoriser ce test car utile pour d'autres methodes ci-dessous

        Rencontre rencontre = rencontreRepository.findOne(rencontreId);
        if (!rencontre.isResultatsEncodes() && !rencontre.isValide()){
            if (rencontre.getDivision().getChampionnat() != null) {
                if (rencontre.getDivision().getChampionnat().isCalendrierValide() && !rencontre.getDivision().getChampionnat().isCloture()) {
                    return true;
                }
            }
        }
        return false;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/rencontre/{rencontreId}/isResultatsCloturables", method = RequestMethod.GET)
    public boolean isResultatsCloturables(@PathVariable("rencontreId") Long rencontreId) {

        //TODO : tester le fait d'etre capitaine de l'equipe visites ou resp. visites ou admin

        Rencontre rencontre = rencontreRepository.findOne(rencontreId);

        if (!rencontre.isResultatsEncodes() && !rencontre.isValide()) {

            if (rencontre.getDivision().getChampionnat() != null) {

                if (rencontre.getDivision().getChampionnat().isCalendrierValide() && !rencontre.getDivision().getChampionnat().isCloture()) {

                    // La validite change en fonction du type et de la categorie de championnat

                    // Criterium : Simple -> au moins 2 points atteints par l'une des deux equipes
                    // Coupe d'hiver : au moins 8 points atteints au total des deux equipes


                    Championnat championnat = rencontre.getDivision().getChampionnat();
                    if (TypeChampionnat.HIVER.equals(championnat.getType())
                            || TypeChampionnat.ETE.equals(championnat.getType())) {

                        if (rencontre.getPointsVisites() != null && rencontre.getPointsVisiteurs() != null) {

                            // 6 rencontres de 2 points chacune

                            Integer totalPoints = rencontre.getPointsVisites() + rencontre.getPointsVisiteurs();
                            return totalPoints == 12;
                        }

                    } else if (TypeChampionnat.COUPE_HIVER.equals(championnat.getType())) {

                        // 4 rencontres de 2 points

                        if (rencontre.getPointsVisites() != null && rencontre.getPointsVisiteurs() != null) {
                            Integer totalPoints = rencontre.getPointsVisites() + rencontre.getPointsVisiteurs();
                            return totalPoints == 8;
                        }

                    } else if (TypeChampionnat.CRITERIUM.equals(championnat.getType())) {

                        if (rencontre.getPointsVisites() != null && rencontre.getPointsVisiteurs() != null) {
                            Integer totalPoints = rencontre.getPointsVisites() + rencontre.getPointsVisiteurs();
                            return totalPoints == 2;
                        }

                    }
                }
            }
        }

        return false;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/rencontre/{rencontreId}/isPoursuiteEncodagePossible", method = RequestMethod.GET)
    public boolean isPoursuiteEncodagePossible(@PathVariable("rencontreId") Long rencontreId) {

        //TODO : tester le fait d'etre capitaine des equipes ou responsables des clubs ou admin

        Rencontre rencontre = rencontreRepository.findOne(rencontreId);
        if (rencontre.isResultatsEncodes() && !rencontre.isValide()) {
            if (rencontre.getDivision().getChampionnat().isCalendrierValide() && !rencontre.getDivision().getChampionnat().isCloture()){
                return true;
            }
        }
        return false;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/rencontre/{rencontreId}/isValidable", method = RequestMethod.GET)
    public boolean isRencontreValidable(@PathVariable("rencontreId") Long rencontreId) {

        //TODO : tester le fait d'etre capitaine de l'equipe visiteurs ou resp. visiteurs ou admin

        Rencontre rencontre = rencontreRepository.findOne(rencontreId);
        if (rencontre.isResultatsEncodes() && !rencontre.isValide()) {
            if (rencontre.getDivision().getChampionnat().isCalendrierValide() && !rencontre.getDivision().getChampionnat().isCloture()){
                return true;
            }
        }
        return false;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/rencontre/{rencontreId}/resultatsEncodes", method = RequestMethod.PUT)
    public boolean updateResultatsEncodes(@PathVariable("rencontreId") Long rencontreId, @RequestBody boolean resultatsEncodes) {
        if (resultatsEncodes) {

            //TODO : seul le capitaine visites, responsable club visite et admin peuvent signaler la fin de l'encodage --> via le test "is.."

            if (isResultatsCloturables(rencontreId)) {
                rencontreRepository.updateResultatsEncodes(rencontreId, resultatsEncodes);
            } else {
                return false;
            }
        } else {
            if (isPoursuiteEncodagePossible(rencontreId)){

                // TODO : Les capitaines des equipes, responsable de clubs et admin peuvent demander la poursuite de l'encodage --> via le test "is..."

                rencontreRepository.updateResultatsEncodes(rencontreId, resultatsEncodes);
            }
        }

        return resultatsEncodes;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/rencontre/{rencontreId}/validite", method = RequestMethod.PUT)
    public boolean updateValiditeRencontre(@PathVariable("rencontreId") Long rencontreId, @RequestBody boolean validite) {
        if (validite) {

            //TODO : seul le capitaine visiteurs, responsable club visiteur et admin peuvent signaler la fin de l'encodage --> via le test "is.."

            if (isRencontreValidable(rencontreId)) {
                rencontreRepository.updateValiditeRencontre(rencontreId, validite);
            } else {
                return false;
            }
        } else {

            // TODO : only admin peut devalider une rencontre tant que le championnat n'est pas cloture

            Rencontre rencontre = rencontreRepository.findOne(rencontreId);
            if (!rencontre.getDivision().getChampionnat().isCloture()){
                rencontreRepository.updateValiditeRencontre(rencontreId, validite);
            }else{
                return true;
            }
        }

        return validite;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(method = RequestMethod.GET, path = "/private/rencontres/interseries")
    public List<Rencontre> getInterseries(@RequestParam Long championnatId) {

        // Dans un premier temps, on ne va creer des interseries qu'avec des divisions comprenant deux poules

        /*

         TODO : gerer les matchs interseries pour des divisions a plus de deux poules :

         Lister les equipes premieres de classement de poule
         Analyser les confrontations interseries et ne conserver que les gagnants
         les perdants doivent etre conserves afin de ne pas les reprendre pour la suite de la
         competition
         Ce principe fonctionnera avec un nombre pair de poules
         Pour un nombre impair : à analyser

          */

        /*

            Parcours des divisions
            Pour chaque division, regarder
                s'il y a plusieurs poules
                si l'ensemble des rencontres ont ete validees
            Si c'est le cas, on va prendre les 1er de chaque poule pour proposer la rencontre

         */

        Championnat championnat = championnatRepository.findOne(championnatId);
        if (!championnat.isCalendrierValide() || championnat.isCloture()) {
            throw new RuntimeException("Operation not supported - Championnat cloture");
        }

        List<Rencontre> rencontresInterseries = new ArrayList<>();

        if (TypeChampionnat.ETE.equals(championnat.getType()) || TypeChampionnat.HIVER.equals(championnat.getType())) {

            List<Division> divisions = (List<Division>) divisionRepository.findByChampionnat(championnat);
            for (Division division : divisions) {

                List<Poule> poules = (List<Poule>) pouleRepository.findByDivision(division);

                // S'il y a deux poules dans la division,

                if (!poules.isEmpty() && poules.size() == 2) {

                    // On verifie que l'ensemble des rencontres de la division ont bien ete validees

                    Long nbRencontresNonValidees = rencontreRepository.countRencontresDePouleNonValideesByDivision(division.getId());

                    if (nbRencontresNonValidees == 0) {
                        // On recupere les classements pour cette division
                        // Par poule, on prend la premiere equipe
                        Classement classementPoule1 = classementService.getClassementPoule(poules.get(0));
                        Classement classementPoule2 = classementService.getClassementPoule(poules.get(1));

                        if (!classementPoule1.getClassementEquipes().isEmpty() && !classementPoule2.getClassementEquipes().isEmpty()) {

                            // Si on a une division a interserie multiple, on va boucler sur les equipes du classement

                            Integer maxInterserie = 1;
                            boolean switchFirsts = false;

                            // Boucler si plusieurs rencontres a des niveaux differents + boolean pour preciser cette caracteristique dans la division

                            // Si interserie avec petite et grande finale, il faut switcher les deux premiers et faire 1-2 et 2-1
                            if (division.isMultiIS()) {
                                maxInterserie = Math.min(classementPoule1.getClassementEquipes().size(), classementPoule2.getClassementEquipes().size());
                                if (division.isWithFinales()) {
                                    switchFirsts = true;
                                }
                            } else {
                                // Si seul l'aspect "finales" est precise, seules les deux premieres rencontres vont etre jouees
                                if (division.isWithFinales()) {
                                    maxInterserie = 2;
                                    switchFirsts = true;
                                }
                            }

                            // On conserve les informations des deux premieres equipes classees pour le cas des petite et grande finale
                            Equipe equipeA = null;
                            Equipe adversaireA = null;
                            Equipe equipeB = null;
                            Equipe adversaireB = null;

                            for (int i = 0; i < maxInterserie; i++) {
                                int realI = i;
                                // On inverse les adversaires pour les deux premieres rencontres (premier contre second)
                                if (switchFirsts) {
                                    if (i == 0) {
                                        realI = 1;
                                    } else if (i == 1) {
                                        realI = 0;
                                    }
                                }
                                Equipe equipe1 = classementPoule1.getClassementEquipes().get(i).getEquipe();
                                Equipe equipe2 = classementPoule2.getClassementEquipes().get(realI).getEquipe();
                                if (i == 0) {
                                    equipeA = equipe1;
                                    adversaireA = equipe2;
                                } else if (i == 1) {
                                    equipeB = equipe1;
                                    adversaireB = equipe2;
                                }
                                Rencontre rencontre = new Rencontre();
                                rencontre.setDivision(division);
                                rencontre.setEquipeVisites(equipe1);
                                rencontre.setEquipeVisiteurs(equipe2);

                                if (!isInterserieExists(rencontre)) {
                                    rencontresInterseries.add(rencontre);
                                }
                            }

                            // Le cas echeant, proposer les petites et grandes finales
                            if (division.isWithFinales()) {
                                // Analyser si les deux rencontres interseries concernes ont ete jouees et validees

                                //rencontreA1B2 jouee et validee;
                                //rencontreB1A2 jouee et validee;

                                // Si oui, se faire rencontrer les vainqueurs d'un cote et les perdants de l'autre
                                Equipe equipeGagnanteA = null;
                                Equipe equipeGagnanteB = null;
                                Equipe equipePerdanteA = null;
                                Equipe equipePerdanteB = null;

                                List<Rencontre> rencontres = rencontreRepository.getRencontresByDivisionAndEquipes(division.getId(), equipeA.getId(), adversaireA.getId());
                                // On est cense n'avoir qu'une seule rencontre opposant ces deux equipes
                                if (rencontres.size() > 0) {
                                    Rencontre interserie = rencontres.get(0);
                                    if (interserie.isValide()) {
                                        equipeGagnanteA = classementService.getGagnantRencontreInterserie(interserie);
                                        if (interserie.getEquipeVisites().equals(equipeGagnanteA)) {
                                            equipePerdanteA = interserie.getEquipeVisiteurs();
                                        } else {
                                            equipePerdanteA = interserie.getEquipeVisites();
                                        }
                                    }
                                }
                                rencontreRepository.getRencontresByDivisionAndEquipes(division.getId(), equipeB.getId(), adversaireB.getId());
                                // On est cense n'avoir qu'une seule rencontre opposant ces deux equipes
                                if (rencontres.size() > 0) {
                                    Rencontre interserie = rencontres.get(0);
                                    if (interserie.isValide()) {
                                        equipeGagnanteB = classementService.getGagnantRencontreInterserie(interserie);
                                        if (interserie.getEquipeVisites().equals(equipeGagnanteB)) {
                                            equipePerdanteB = interserie.getEquipeVisiteurs();
                                        } else {
                                            equipePerdanteB = interserie.getEquipeVisites();
                                        }
                                    }
                                }

                                if (equipeGagnanteA != null && equipePerdanteA != null && equipeGagnanteB != null && equipePerdanteB != null) {

                                    // Creation de la petite finale

                                    Rencontre petiteFinale = new Rencontre();
                                    petiteFinale.setDivision(division);
                                    petiteFinale.setEquipeVisites(equipePerdanteA);
                                    petiteFinale.setEquipeVisiteurs(equipePerdanteB);

                                    if (!isInterserieExists(petiteFinale)) {
                                        rencontresInterseries.add(petiteFinale);
                                    }

                                    // Creation de la grande finale

                                    Rencontre finale = new Rencontre();
                                    finale.setDivision(division);
                                    finale.setEquipeVisites(equipeGagnanteA);
                                    finale.setEquipeVisiteurs(equipeGagnanteB);

                                    if (!isInterserieExists(finale)) {
                                        rencontresInterseries.add(finale);
                                    }

                                }


                            }

                        }

                    }

                }
            }
        }

        return rencontresInterseries;

    }

    /**
     * Permet de verifier si la rencontre interserie existe deja dans le systeme
     *
     * @param rencontre Rencontre
     * @return
     */
    private boolean isInterserieExists(Rencontre rencontre) {
        Long nbSameRencontre = rencontreRepository.countByDivisionAndEquipes(rencontre.getDivision().getId(), rencontre.getEquipeVisites().getId(), rencontre.getEquipeVisiteurs().getId());
        return nbSameRencontre > 0;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(method = RequestMethod.POST, path = "/private/rencontres/calendrier")
    public Iterable<Rencontre> createCalendrier(@RequestParam Long championnatId) {

        List<Rencontre> rencontres = new ArrayList<Rencontre>();

        Championnat championnat = new Championnat();
        championnat.setId(championnatId);
        Iterable<Division> divisionList = divisionRepository.findByChampionnat(championnat);
        for (Division division : divisionList) {
            Iterable<Poule> pouleList = pouleRepository.findByDivision(division);
            for (Poule poule : pouleList) {
                rencontres.addAll(generateCalendar(poule));

            }
        }

        List<Rencontre> rencontresSaved = rencontreService.saveRencontres(rencontres);

        // Calendrier rafraichi --> false
        championnatRepository.updateCalendrierARafraichir(championnatId, false);

        return rencontresSaved;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(method = RequestMethod.PUT, path = "/private/rencontres/calendrier")
    public Iterable<Rencontre> refreshCalendrier(@RequestParam Long championnatId) {

        // On peut faire un refresh tant que le calendrier n'a pas ete valide
        Championnat championnat = championnatRepository.findOne(championnatId);
        if (championnat.isCalendrierValide()) {
            throw new RuntimeException("Operation not supported - Calendrier validé");
        }

        List<Rencontre> anciennesRencontres = new ArrayList<>();
        List<Rencontre> nouvellesRencontres = new ArrayList<>();
        Iterable<Division> divisionList = divisionRepository.findByChampionnat(championnat);
        for (Division division : divisionList) {
            Iterable<Poule> pouleList = pouleRepository.findByDivision(division);
            for (Poule poule : pouleList) {
                anciennesRencontres.addAll((Collection<? extends Rencontre>) rencontreRepository.findByPoule(poule));
                nouvellesRencontres.addAll(generateCalendar(poule));
            }
        }

        List<Rencontre> rencontresSaved = rencontreService.refreshRencontres(anciennesRencontres, nouvellesRencontres);

        // Calendrier rafraichi --> false
        championnatRepository.updateCalendrierARafraichir(championnatId, false);

        return rencontresSaved;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(method = RequestMethod.DELETE, path = "/private/rencontres/calendrier")
    public void deleteCalendrier(@RequestParam Long championnatId) {
        Championnat championnat = championnatRepository.findOne(championnatId);
        if (!championnat.isCalendrierValide()) {
            rencontreService.deleteByChampionnat(championnatId);
        }
    }

    @RequestMapping(path = "/public/rencontres/calendrier", method = RequestMethod.GET)
    ResponseEntity<byte[]> getCalendrier(@RequestParam Long championnatId, @RequestParam(required = false) boolean excel) throws Exception {

        TimeZone timeZone = TimeZone.getTimeZone("Europe/Paris");

        if (excel) {
            Championnat championnat = championnatRepository.findOne(championnatId);
            List<Division> divisions = (List<Division>) divisionRepository.findByChampionnat(championnat);
            List<Rencontre> rencontres = new ArrayList<>();
            for (Division division : divisions) {
                rencontres.addAll((Collection<? extends Rencontre>) rencontreRepository.findByDivision(division));
            }
            Collections.sort(rencontres, new Comparator<Rencontre>() {
                @Override
                public int compare(Rencontre rencontre1, Rencontre rencontre2) {
                    // Tri : Division, poule nulls last, dateheurerencontre nulls last
                    int compareDivision = rencontre1.getDivision().getNumero().compareTo(rencontre2.getDivision().getNumero());
                    if (compareDivision != 0) {
                        return compareDivision;
                    } else {
                        if (rencontre1.getPoule() == null && rencontre2.getPoule() != null) {
                            return 1;
                        } else if (rencontre1.getPoule() != null && rencontre2.getPoule() == null) {
                            return -1;
                        } else {
                            int comparePoule = 0;
                            if (rencontre1.getPoule() != null && rencontre2.getPoule() != null) {
                                comparePoule = rencontre1.getPoule().getNumero().compareTo(rencontre2.getPoule().getNumero());
                            }
                            if (comparePoule != 0) {
                                return comparePoule;
                            } else {
                                if (rencontre1.getDateHeureRencontre() == null && rencontre2.getDateHeureRencontre() == null) {
                                    return 0;
                                } else if (rencontre1.getDateHeureRencontre() == null && rencontre2.getDateHeureRencontre() != null) {
                                    return 1;
                                } else if (rencontre1.getDateHeureRencontre() != null && rencontre2.getDateHeureRencontre() == null) {
                                    return -1;
                                } else {
                                    return rencontre1.getDateHeureRencontre().compareTo(rencontre2.getDateHeureRencontre());
                                }
                            }
                        }
                    }
                }
            });


            LocaleUtil.setUserTimeZone(timeZone);

            Workbook wb = POIUtils.createWorkbook(true);
            Sheet sheet = wb.createSheet("Calendrier_" + championnat.getType() + "_" + championnat.getCategorie() + "_" + championnat.getAnnee());

            CreationHelper createHelper = wb.getCreationHelper();
            CellStyle dateTimeCellStyle = wb.createCellStyle();
            dateTimeCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy HH:MM"));

            Cell firstCell = POIUtils.write(sheet, 0, 0, "Division", null, null);
            POIUtils.write(sheet, 0, 1, "Poule", null, null);
            POIUtils.write(sheet, 0, 2, "Date/Heure", null, null);
            POIUtils.write(sheet, 0, 3, "Terrain", null, null);
            POIUtils.write(sheet, 0, 4, "Visites", null, null);
            Cell lastCell = POIUtils.write(sheet, 0, 5, "Visiteurs", null, null);

            for (int i = 0; i < rencontres.size(); i++) {
                Rencontre rencontre = rencontres.get(i);

                lastCell = POIUtils.write(sheet, i + 1, 0, rencontre.getDivision().getNumero(), null, null);
                if (rencontre.getPoule() != null) {
                    lastCell = POIUtils.write(sheet, i + 1, 1, rencontre.getPoule().getNumero(), null, null);
                }
                if (rencontre.getDateHeureRencontre() != null) {
                    lastCell = POIUtils.write(sheet, i + 1, 2, rencontre.getDateHeureRencontre(), dateTimeCellStyle, null);
                }
                if (rencontre.getTerrain() != null) {
                    lastCell = POIUtils.write(sheet, i + 1, 3, rencontre.getTerrain().getNom(), null, null);
                }
                lastCell = POIUtils.write(sheet, i + 1, 4, rencontre.getEquipeVisites().getCodeAlphabetique(), null, null);
                lastCell = POIUtils.write(sheet, i + 1, 5, rencontre.getEquipeVisiteurs().getCodeAlphabetique(), null, null);

            }

            // Freeze de la premiere ligne
            sheet.createFreezePane(0, 1);

            // Auto-resize des colonnes
            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            // Filtre defini pour la plage de cellules remplies
            sheet.setAutoFilter(new CellRangeAddress(firstCell.getRowIndex(), lastCell.getRowIndex(), firstCell.getColumnIndex(), lastCell.getColumnIndex()));

            Sheet sheetRecap = wb.createSheet("Recapitulatif");

            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
            CellStyle titleStyle = wb.createCellStyle();
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREY_50_PERCENT.getIndex());
            titleStyle.setFont(titleFont);

            CellStyle clubStyle = wb.createCellStyle();
            clubStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            clubStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex());

            Cell titre = POIUtils.write(sheetRecap, 0, 0, "Nombre de rencontres par club/équipe", null, null);
            titre.setCellStyle(titleStyle);
            Cell titre2 = POIUtils.write(sheetRecap, 0, 1, null, null, null);
            titre2.setCellStyle(titleStyle);

            Map<Club, Map<Equipe, List<Rencontre>>> clubsMap = getClubMapFromRencontres(rencontres);

            List<Club> clubs = new ArrayList(clubsMap.keySet());
            Collections.sort(clubs, new Comparator<Club>() {
                @Override
                public int compare(Club o1, Club o2) {
                    return o1.getNom().compareTo(o2.getNom());
                }
            });

            int rowIndex = 1;

            for (Club club : clubs) {

                Cell clubCell = POIUtils.write(sheetRecap, rowIndex, 0, club.getNom() + " (" + club.getNumero() + ")", null, null);
                clubCell.setCellStyle(clubStyle);

                int nbRencontresClub = 0;
                for (Equipe equipe : clubsMap.get(club).keySet()) {
                    nbRencontresClub += clubsMap.get(club).get(equipe).size();
                }

                Cell clubCell2 = POIUtils.write(sheetRecap, rowIndex, 1, nbRencontresClub, null, null);
                clubCell2.setCellStyle(clubStyle);

                List<Equipe> equipes = new ArrayList<>(clubsMap.get(club).keySet());
                Collections.sort(equipes, new Comparator<Equipe>() {
                    @Override
                    public int compare(Equipe o1, Equipe o2) {
                        return o1.getCodeAlphabetique().compareTo(o2.getCodeAlphabetique());
                    }
                });

                rowIndex++;

                for (Equipe equipe : equipes) {
                    POIUtils.write(sheetRecap, rowIndex, 0, equipe.getCodeAlphabetique(), null, null);
                    POIUtils.write(sheetRecap, rowIndex, 1, clubsMap.get(club).get(equipe).size(), null, null);

                    rowIndex++;
                }

            }

            Cell total = POIUtils.write(sheetRecap, rowIndex, 0, "TOTAL", null, null);
            total.setCellStyle(titleStyle);
            Cell total2 = POIUtils.write(sheetRecap, rowIndex, 1, rencontres.size(), null, null);
            total2.setCellStyle(titleStyle);

            // Auto-resize des colonnes
            for (int i = 0; i < 2; i++) {
                sheetRecap.autoSizeColumn(i);
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            wb.write(os);
            wb.close();
            os.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(os.toByteArray(), headers, HttpStatus.OK);
            return response;

        } else {
            JasperReport jasperReport = JasperCompileManager.compileReport(ReportUtils.getCalendrierTemplate());
            Connection conn = datasource.getConnection();
            Map params = new HashMap();
            params.put("REPORT_TIME_ZONE", timeZone);
            params.put("championnatId", championnatId);
            JasperPrint jprint = JasperFillManager.fillReport(jasperReport, params, conn);
            byte[] pdfFile = JasperExportManager.exportReportToPdf(jprint);
            conn.close();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/pdf"));
            ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(pdfFile, headers, HttpStatus.OK);
            return response;
        }
    }

    /**
     * Permet de recuperer une map des rencontres triees par club et par equipe
     *
     * @param rencontres
     * @return
     */
    private Map<Club, Map<Equipe, List<Rencontre>>> getClubMapFromRencontres(List<Rencontre> rencontres) {
        Map<Club, Map<Equipe, List<Rencontre>>> globalMap = new HashMap<>();
        for (Rencontre rencontre : rencontres) {
            Club clubVisite = rencontre.getEquipeVisites().getClub();
            Map<Equipe, List<Rencontre>> clubVisiteMap = globalMap.get(clubVisite);
            if (clubVisiteMap == null) {
                clubVisiteMap = new HashMap<>();
                globalMap.put(clubVisite, clubVisiteMap);
            }
            List<Rencontre> rencontresVisites = clubVisiteMap.get(rencontre.getEquipeVisites());
            if (rencontresVisites == null) {
                rencontresVisites = new ArrayList<>();
                clubVisiteMap.put(rencontre.getEquipeVisites(), rencontresVisites);
            }
            rencontresVisites.add(rencontre);

            Club clubVisiteur = rencontre.getEquipeVisiteurs().getClub();
            Map<Equipe, List<Rencontre>> clubVisiteurMap = globalMap.get(clubVisiteur);
            if (clubVisiteurMap == null) {
                clubVisiteurMap = new HashMap<>();
                globalMap.put(clubVisiteur, clubVisiteurMap);
            }
            List<Rencontre> rencontresVisiteurs = clubVisiteurMap.get(rencontre.getEquipeVisiteurs());
            if (rencontresVisiteurs == null) {
                rencontresVisiteurs = new ArrayList<>();
                clubVisiteurMap.put(rencontre.getEquipeVisiteurs(), rencontresVisiteurs);
            }
            rencontresVisiteurs.add(rencontre);
        }
        return globalMap;
    }

    /**
     * Permet de generer le calendrier d'une poule
     *
     * @param poule Poule
     * @return Liste des rencontres d'une poule
     */
    private List<Rencontre> generateCalendar(Poule poule) {

        List<Rencontre> rencontres = new ArrayList<>();

        List<Equipe> equipes = (List<Equipe>) equipeRepository.findByPoule(poule);

        if (equipes.size() < 2) {
            return rencontres;
        }

        int nbJournees = getNbJournees(equipes);
        int nbRencontresParJournee = equipes.size() / 2;
        //System.err.println("Nb rencontres par journee : " + nbRencontresParJournee);
        //System.err.println("Nombre de matchs à jouer :" + nbJournees*nbRencontresParJournee);

        // Decoupe en journees

        for (int i = 0; i < nbJournees; i++) {

            // S'il s'agit d'un nombre pair d'equipes, on va boucler sur toutes les equipes sauf la premiere
            List<Equipe> equipesReduites = new ArrayList<>(equipes.subList(1, equipes.size()));

            for (int j = 0; j < nbRencontresParJournee; j++) {

                Rencontre rencontre = new Rencontre();
                rencontre.setPoule(poule);
                rencontre.setDivision(poule.getDivision());
                rencontre.setNumeroJournee(i + 1);

                // Pour un nombre d'equipes impair, permutation circulaire parmi toutes les equipes --> l'une sera bye via la procedure
                if (nombreEquipesImpair(equipes)) {
                    rencontre.setEquipeVisites(equipes.get((0 + j + i) % equipes.size()));
                    rencontre.setEquipeVisiteurs(equipes.get((equipes.size() - 1 - j + i) % equipes.size()));
                } else {
                    // Pour un nombre d'equipes pair,
                    // On va garder la premiere equipe fixe et faire tourner les autres
                    if (j == 0) {
                        rencontre.setEquipeVisites(equipes.get(0));
                        rencontre.setEquipeVisiteurs(equipesReduites.get((equipesReduites.size() - 1 - j + i) % equipesReduites.size()));
                    } else {
                        rencontre.setEquipeVisites(equipesReduites.get((equipesReduites.size() - 1 + i + j) % equipesReduites.size()));
                        rencontre.setEquipeVisiteurs(equipesReduites.get((equipesReduites.size() - 1 - j + i) % equipesReduites.size()));
                    }
                }

                // Si l'equipe visitee possede un terrain, on le precise pour la rencontre
                if (rencontre.getEquipeVisites().getTerrain() != null) {
                    rencontre.setTerrain(rencontre.getEquipeVisites().getTerrain());
                }

                //System.err.println(rencontre);
                rencontres.add(rencontre);

            }

        }

        // Si la poule est specifee avec matchs aller-retour, on va dupliquer les rencontres
        //  en inversant les equipes visites-visiteurs
        if (poule.isAllerRetour()) {
            List<Rencontre> rencontresRetour = new ArrayList<>();

            for (Rencontre rencontreAller : rencontres) {
                Rencontre rencontreRetour = new Rencontre();
                rencontreRetour.setPoule(poule);
                rencontreRetour.setDivision(poule.getDivision());
                rencontreRetour.setNumeroJournee(nbJournees + rencontreAller.getNumeroJournee());
                rencontreRetour.setEquipeVisites(rencontreAller.getEquipeVisiteurs());
                rencontreRetour.setEquipeVisiteurs(rencontreAller.getEquipeVisites());

                // Si l'equipe visitee possede un terrain, on le precise pour la rencontre
                if (rencontreRetour.getEquipeVisites().getTerrain() != null) {
                    rencontreRetour.setTerrain(rencontreRetour.getEquipeVisites().getTerrain());
                }

                rencontresRetour.add(rencontreRetour);
            }

            rencontres.addAll(rencontresRetour);
        }

        return rencontres;

    }

    private boolean nombreEquipesImpair(List<Equipe> equipes) {
        return equipes.size() % 2 != 0;
    }

    private int getNbJournees(List<Equipe> equipes) {
        if (nombreEquipesImpair(equipes)) {
            return equipes.size();
        } else {
            return equipes.size() - 1;
        }
    }

}
