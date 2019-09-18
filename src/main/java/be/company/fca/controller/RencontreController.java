package be.company.fca.controller;

import be.company.fca.dto.AutorisationRencontreDto;
import be.company.fca.dto.MatchDto;
import be.company.fca.dto.RencontreDto;
import be.company.fca.exceptions.ForbiddenException;
import be.company.fca.model.*;
import be.company.fca.model.Set;
import be.company.fca.repository.*;
import be.company.fca.service.*;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des rencontres")
public class RencontreController {

    @Autowired
    DataSource datasource;
    @Autowired
    private RencontreRepository rencontreRepository;
    @Autowired
    private AutorisationRencontreRepository autorisationrencontreRepository;
    @Autowired
    private EquipeRepository equipeRepository;
    @Autowired
    private DivisionRepository divisionRepository;
    @Autowired
    private PouleRepository pouleRepository;
    @Autowired
    private ChampionnatRepository championnatRepository;
    @Autowired
    private MembreRepository membreRepository;
    @Autowired
    private TerrainRepository terrainRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchService matchService;

    @Autowired
    private TraceService traceService;

    @Autowired
    private ClassementService classementService;
    @Autowired
    private RencontreService rencontreService;
    @Autowired
    private UserService userService;

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

    @RequestMapping(method = RequestMethod.GET, path = "/public/rencontresByClub")
    public List<RencontreDto> getRencontresByClub(@RequestParam Long championnatId, @RequestParam Long clubId) {
        List<RencontreDto> rencontresDto = new ArrayList<>();

        List<Rencontre> rencontres = (List<Rencontre>) rencontreRepository.findRencontresByChampionnatAndClub(championnatId,clubId);

        for (Rencontre rencontre : rencontres) {
            rencontresDto.add(new RencontreDto(rencontre));
        }

        return rencontresDto;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/private/rencontres/toComplete")
    public List<Rencontre> getRencontresAEncoder(Authentication authentication){
        List<Rencontre> rencontresAEncoder = rencontreRepository.findEncodedAndValidatedBefore(false,false,new Date());

        if (userService.isAdmin(authentication)){
            return rencontresAEncoder;
        }

        // Recuperer les rencontres en tant que responsable de club ou capitaine d'equipe

        Club myClub = null;
        List<Rencontre> myRencontres = new ArrayList<>();

        Membre membre = userService.getMembreFromAuthentication(authentication);
        if (membre!=null){
            if (membre.isResponsableClub()){
                myClub = membre.getClub();
            }

            List<AutorisationRencontre> autorisationRencontres = autorisationrencontreRepository.findByMembre(membre);

            for (Rencontre rencontre : rencontresAEncoder){
                if ( (myClub!=null && rencontre.getEquipeVisites().getClub().equals(myClub)) || membre.equals(rencontre.getEquipeVisites().getCapitaine())){
                    myRencontres.add(rencontre);
                }else{

                    for (AutorisationRencontre autorisationRencontre : autorisationRencontres) {
                        if (autorisationRencontre.getRencontreFk().equals(rencontre.getId()) && TypeAutorisation.ENCODAGE.equals(autorisationRencontre.getType())) {
                            myRencontres.add(rencontre);
                        }
                    }

                }
            }

        }

        return myRencontres;

    }

    @RequestMapping(method = RequestMethod.GET, path = "/private/rencontres/toValidate")
    public List<Rencontre> getRencontresAValider(Authentication authentication){
        List<Rencontre> rencontresAValider = rencontreRepository.findEncodedAndValidatedBefore(true,false,new Date());

        if (userService.isAdmin(authentication)){
            return rencontresAValider;
        }

        // Recuperer les rencontres en tant que responsable de club ou capitaine d'equipe

        Club myClub = null;
        List<Rencontre> myRencontres = new ArrayList<>();

        Membre membre = userService.getMembreFromAuthentication(authentication);
        if (membre!=null){
            if (membre.isResponsableClub()){
                myClub = membre.getClub();
            }

            List<AutorisationRencontre> autorisationRencontres = autorisationrencontreRepository.findByMembre(membre);

            for (Rencontre rencontre : rencontresAValider){
                if ( (myClub!=null && rencontre.getEquipeVisiteurs().getClub().equals(myClub)) || membre.equals(rencontre.getEquipeVisiteurs().getCapitaine())){
                    myRencontres.add(rencontre);
                }else{

                    for (AutorisationRencontre autorisationRencontre : autorisationRencontres) {
                        if (autorisationRencontre.getRencontreFk().equals(rencontre.getId()) && TypeAutorisation.VALIDATION.equals(autorisationRencontre.getType())) {
                            myRencontres.add(rencontre);
                        }
                    }

                }
            }

        }

        return myRencontres;

    }

    /**
     * Permet de recuperer les 5 derniers resultats encodes (rencontres validees) dans le systeme
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, path = "/public/rencontres/last")
    public List<Rencontre> getLastTenResults(@RequestParam Integer numberOfResults) {
        return rencontreRepository.getLastResults(PageRequest.of(0,numberOfResults));
    }

    @RequestMapping(method = RequestMethod.GET, path = "/public/rencontres/next")
    public List<Rencontre> getComingMeetings(@RequestParam Integer numberOfResults) {
        return rencontreRepository.getNextMeetings(DateUtils.shrinkToDay(new Date()), PageRequest.of(0,numberOfResults));
    }

    @RequestMapping(method = RequestMethod.GET, path = "/public/rencontres/criterium")
    public List<Rencontre> getRencontresCriteriumByAnnee(@RequestParam String annee) {
        List<Rencontre> rencontres = new ArrayList<>();
        List<Championnat> championnats = championnatRepository.findByTypeAndAnnee(TypeChampionnat.CRITERIUM,annee);
        for (Championnat championnat : championnats){
            List<Division> divisions = (List<Division>) divisionRepository.findByChampionnat(championnat);
            for (Division division : divisions){
                rencontres.addAll((Collection<? extends Rencontre>) rencontreRepository.findByDivision(division));
            }
        }
        return rencontres;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/public/rencontres/byDate")
    public List<Rencontre> getRencontresByDate(@RequestParam @DateTimeFormat(pattern = "yyyyMMdd") Date date) {
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

    @RequestMapping(value = "/private/rencontre/points", method = RequestMethod.PUT)
    public Rencontre updateRencontrePoints(Authentication authentication, @RequestBody Rencontre rencontre) {

        // Verifier les autorisations des joueurs qui tentent de mettre a jour la rencontre (resultats) --> separer les deux methodes...

        if (isResultatsRencontreModifiables(authentication,rencontre.getId())) {
            rencontreRepository.updatePoints(rencontre.getId(),rencontre.getPointsVisites(),rencontre.getPointsVisiteurs());
            return rencontre;
        }else{
            throw new ForbiddenException();
        }

    }

    @RequestMapping(value = "/private/rencontre/commentairesEncodeur", method = RequestMethod.PUT)
    public Rencontre updateRencontreCommentairesEncodeur(Authentication authentication, @RequestBody Rencontre rencontre) {

        // Verifier les autorisations des joueurs qui tentent de mettre a jour la rencontre (resultats) --> separer les deux methodes...

        if (isResultatsRencontreModifiables(authentication,rencontre.getId())) {
            rencontreRepository.updateCommentairesEncodeur(rencontre.getId(),rencontre.getCommentairesEncodeur());
            return rencontre;
        }else{
            throw new ForbiddenException();
        }

    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/rencontre/forfait", method = RequestMethod.PUT)
    public Rencontre forfaitRencontre(Authentication authentication, @RequestBody Rencontre rencontre, @RequestParam boolean forfaitVisiteurs){

        if (isForfaitPossible(authentication,rencontre.getId())){

            rencontre.setPointsVisites(0);
            rencontre.setPointsVisiteurs(0);

            // Recuperation des matchs et mises à jour des sets

            List<Match> matchs = getOrCreateMatchs(rencontre);
            for (Match match : matchs){

                Integer nbJeux = matchService.getNbJeuxMax(match);

                List<Set> sets = new ArrayList<>();
                for (int i=0;i<2;i++){
                    Set set = new Set();
                    set.setMatch(match);
                    set.setOrdre(i+1);
                    if (forfaitVisiteurs){
                        set.setJeuxVisites(nbJeux);
                        set.setJeuxVisiteurs(0);
                    }else{
                        set.setJeuxVisites(0);
                        set.setJeuxVisiteurs(nbJeux);
                    }
                    sets.add(set);
                }

                match = matchService.updateMatchAndSets(match.getId(),sets);

                // mise a jour des points de la rencontre
                rencontre.setPointsVisites(rencontre.getPointsVisites() + match.getPointsVisites());
                rencontre.setPointsVisiteurs(rencontre.getPointsVisiteurs() + match.getPointsVisiteurs());
            }

            // Commentaires de la rencontre
            String commentaires = "Forfait de l'équipe ";
            if (forfaitVisiteurs){
                commentaires+=rencontre.getEquipeVisiteurs().getCodeAlphabetique();
            }else{
                commentaires+=rencontre.getEquipeVisites().getCodeAlphabetique();
            }
            rencontre.setCommentairesEncodeur(commentaires);

            rencontre.setResultatsEncodes(true);
            rencontre.setValide(true);

            rencontreRepository.save(rencontre);

            //Ajout d'une trace pour preciser le forfait encode par l'administrateur

            String trace = "";
            trace = commentaires + " encodé par l'administrateur";
            traceService.addTrace(authentication,"rencontre",rencontre.getId().toString(),trace);

            return rencontre;

        }else{
            throw new ForbiddenException();
        }

    }


    // DTO pour les membres afin de ne pas recuperer les donnees privees
    // Attention a la rencontre --> rencontreDto

    /**
     * Permet de recuperer les matchs pour une rencontre
     * Si les matchs n'existent pas, ils sont créés à la volée
     *
     * Cette création dépend du type de championnat et de la catégorie
     *
     * @param rencontreId
     * @return
     */
    @RequestMapping(method= RequestMethod.GET, path="/public/rencontre/{rencontreId}/matchs")
    public List<MatchDto> getMatchsByRencontre(@PathVariable("rencontreId") Long rencontreId) {

        List<MatchDto> matchsDto = new ArrayList<>();
        List<Match> matchs = new ArrayList<Match>();

        Rencontre rencontre = rencontreRepository.findById(rencontreId).get();

        matchs = getOrCreateMatchs(rencontre);

        for (Match match : matchs){
            matchsDto.add(new MatchDto(match));
        }

        return matchsDto;

    }

    /**
     * Permet de recuperer ou de creer les matchs d'une rencontre s'ils n'existent pas encore
     * @param rencontre
     * @return
     */
    private List<Match> getOrCreateMatchs(Rencontre rencontre){

        List<Match> matchs = (List<Match>) matchRepository.findByRencontre(rencontre);

        if (matchs.isEmpty()){
            matchs = matchService.createMatchs(rencontre);
        }

        return matchs;
    }

    @RequestMapping(value = "/private/rencontre/{rencontreId}/match", method = RequestMethod.PUT)
    public Match updateMatch(Authentication authentication, @PathVariable("rencontreId") Long rencontreId, @RequestBody Match match){

        //  Verifier les autorisations des joueurs qui tentent de mettre a jour les matchs

        if (isResultatsRencontreModifiables(authentication,rencontreId)) {
            return matchRepository.save(match);
        }else{
            throw new ForbiddenException();
        }

    }

    @RequestMapping(value = "/private/rencontre/{rencontreId}/match/sets", method = RequestMethod.PUT)
    public Match updateMatchAndSets(Authentication authentication, @PathVariable("rencontreId") Long rencontreId,
                                    @RequestParam Long matchId,
                                    @RequestParam(required = false) boolean setUnique,
                                    @RequestBody List<Set> sets){

        //  Verifier les autorisations des joueurs qui tentent de mettre a jour les matchs

        if (isResultatsRencontreModifiables(authentication,rencontreId)) {
            return matchService.updateMatchAndSets(matchId,sets);
        }else{
            throw new ForbiddenException();
        }

    }

    // Permettre a certains utilisateurs d'autoriser d'autres membres a encoder/valider les resultats

    // --> ces autorisations sont accessibles par uniquement les capitaines, responsables de club de l'equipe concernee + administrateur
    // Entite Autorisation = typeAutorisation (enum:encodage/validation), membreFk, rencontreFk

    // Nouvelles methodes d'autorisations qui seront egalement appeles avant la sauvegarde/suppression des autorisations sur des rencontres

    @RequestMapping(value = "/private/rencontre/{rencontreId}/canAuthoriseEncodage", method = RequestMethod.GET)
    public boolean canAuthoriseEncodage(Authentication authentication, @PathVariable("rencontreId") Long rencontreId) {
        if (rencontreId!=null){
            if (userService.isAdmin(authentication)){
                return true;
            }
            // On va analyser si l'utilisateur connecte est capitaine de l'equipe visitee ou responsable du club de l'equipe visitee
            Rencontre rencontre = rencontreRepository.findById(rencontreId).get();
            return isCapitaineOrResponsableClub(authentication,rencontre.getEquipeVisites());
        }

        return false;
    }

    @RequestMapping(value = "/private/rencontre/{rencontreId}/canAuthoriseValidation", method = RequestMethod.GET)
    public boolean canAuthoriseValidation(Authentication authentication, @PathVariable("rencontreId") Long rencontreId) {
        if (rencontreId!=null){
            if (userService.isAdmin(authentication)){
                return true;
            }
            // On va analyser si l'utilisateur connecte est capitaine de l'equipe visiteur ou responsable du club de l'equipe visiteur
            Rencontre rencontre = rencontreRepository.findById(rencontreId).get();
            return isCapitaineOrResponsableClub(authentication,rencontre.getEquipeVisiteurs());
        }

        return false;
    }

    @RequestMapping(value = "/private/rencontre/{rencontreId}/autorisations", method = RequestMethod.GET)
    public List<AutorisationRencontreDto> getAutorisations(Authentication authentication, @PathVariable("rencontreId") Long rencontreId) {
        List<AutorisationRencontreDto> autorisationRencontreDtos = new ArrayList<>();
        List<AutorisationRencontre> autorisationRencontres = autorisationrencontreRepository.findByRencontreFk(rencontreId);
        for (AutorisationRencontre autorisationRencontre: autorisationRencontres){
            autorisationRencontreDtos.add(new AutorisationRencontreDto(autorisationRencontre));
        }
        return autorisationRencontreDtos;
    }

    @RequestMapping(value = "/private/rencontre/{rencontreId}/autorisation", method = RequestMethod.POST)
    public AutorisationRencontreDto addAutorisation(Authentication authentication, @PathVariable("rencontreId") Long rencontreId, @RequestParam("allOthersOfTheTeam") boolean allOthersOfTheTeam, @RequestBody AutorisationRencontre autorisationRencontre){
        autorisationRencontre.setRencontreFk(rencontreId);
        if (TypeAutorisation.ENCODAGE.equals(autorisationRencontre.getType())){
            if (canAuthoriseEncodage(authentication,autorisationRencontre.getRencontreFk())){
                AutorisationRencontreDto authorizationDto = new AutorisationRencontreDto(autorisationrencontreRepository.save(autorisationRencontre));
                if (allOthersOfTheTeam){
                    Rencontre rencontreInitiale = rencontreRepository.findById(rencontreId).get();
                    // On va egalement creer des autorisations pour toutes les autres rencontres de l'equipe concernee
                    List<Rencontre> rencontresOfTeam = rencontreRepository.findByEquipeVisites(rencontreInitiale.getEquipeVisites());
                    for (Rencontre rencontre : rencontresOfTeam){
                        // On va ajouter l'autorisation pour toutes les autres rencontres
                        if (!rencontre.getId().equals(rencontreId)){
                            AutorisationRencontre otherAutorisation = new AutorisationRencontre();
                            otherAutorisation.setRencontreFk(rencontre.getId());
                            otherAutorisation.setType(TypeAutorisation.ENCODAGE);
                            otherAutorisation.setMembre(autorisationRencontre.getMembre());
                            autorisationrencontreRepository.save(otherAutorisation);
                        }
                    }

                }
                // On va retourner l'autorisation initiale pour la rencontre a partir de laquelle on a fait la demande
                return authorizationDto;
            }
        }else if (TypeAutorisation.VALIDATION.equals(autorisationRencontre.getType())){
            if (canAuthoriseValidation(authentication,autorisationRencontre.getRencontreFk())){
                AutorisationRencontreDto authorizationDto = new AutorisationRencontreDto(autorisationrencontreRepository.save(autorisationRencontre));
                if (allOthersOfTheTeam){
                    Rencontre rencontreInitiale = rencontreRepository.findById(rencontreId).get();
                    // On va egalement creer des autorisations pour toutes les autres rencontres de l'equipe concernee
                    List<Rencontre> rencontresOfTeam = rencontreRepository.findByEquipeVisiteurs(rencontreInitiale.getEquipeVisiteurs());
                    for (Rencontre rencontre : rencontresOfTeam){
                        // On va ajouter l'autorisation pour toutes les autres rencontres
                        if (!rencontre.getId().equals(rencontreId)){
                            AutorisationRencontre otherAutorisation = new AutorisationRencontre();
                            otherAutorisation.setRencontreFk(rencontre.getId());
                            otherAutorisation.setType(TypeAutorisation.VALIDATION);
                            otherAutorisation.setMembre(autorisationRencontre.getMembre());
                            autorisationrencontreRepository.save(otherAutorisation);
                        }
                    }

                }
                // On va retourner l'autorisation initiale pour la rencontre a partir de laquelle on a fait la demande
                return authorizationDto;
            }
        }
        throw new ForbiddenException();
    }

    @RequestMapping(value = "/private/rencontre/{rencontreId}/autorisation", method = RequestMethod.DELETE)
    public boolean removeAutorisation(Authentication authentication, @PathVariable("rencontreId") Long rencontreId, @RequestParam Long autorisationRencontreId, @RequestParam("allOthersOfTheTeam") boolean allOthersOfTheTeam){
        AutorisationRencontre autorisationRencontre = autorisationrencontreRepository.findById(autorisationRencontreId).get();
        if (TypeAutorisation.ENCODAGE.equals(autorisationRencontre.getType())){
            if (canAuthoriseEncodage(authentication,autorisationRencontre.getRencontreFk())){
                autorisationrencontreRepository.deleteById(autorisationRencontreId);
                if (allOthersOfTheTeam){
                    Rencontre rencontreInitiale = rencontreRepository.findById(rencontreId).get();
                    // Recuperer l'equipe visitee de la rencontre
                    Equipe equipeVisitee = rencontreInitiale.getEquipeVisites();
                    // Recuperer les rencontres de cette equipe en tant qu'equipe visitee
                    List<Rencontre> rencontresOfTeam = rencontreRepository.findByEquipeVisites(equipeVisitee);
                    // Recuperer les autorisations pour chacune des rencontres differentes de l'initiale
                    for (Rencontre rencontre : rencontresOfTeam){
                        if (!rencontre.getId().equals(rencontreId)){
                            List<AutorisationRencontre> otherAuthorizations = autorisationrencontreRepository.findByRencontreFk(rencontre.getId());
                            // Pour le membre concerne et le type ENCODAGE, supprimer l'autorisation
                            for (AutorisationRencontre otherAuthorization : otherAuthorizations){
                                if (otherAuthorization.getType().equals(TypeAutorisation.ENCODAGE)
                                        && otherAuthorization.getMembre().getId().equals(autorisationRencontre.getMembre().getId())){
                                    autorisationrencontreRepository.deleteById(otherAuthorization.getId());
                                }
                            }
                        }
                    }
                }
                return true;
            }
        }else if (TypeAutorisation.VALIDATION.equals(autorisationRencontre.getType())){
            if (canAuthoriseValidation(authentication,autorisationRencontre.getRencontreFk())){
                autorisationrencontreRepository.deleteById(autorisationRencontreId);
                if (allOthersOfTheTeam){
                    Rencontre rencontreInitiale = rencontreRepository.findById(rencontreId).get();
                    // Recuperer l'equipe visiteur de la rencontre
                    Equipe equipeVisiteur = rencontreInitiale.getEquipeVisiteurs();
                    // Recuperer les rencontres de cette equipe en tant qu'equipe visiteur
                    List<Rencontre> rencontresOfTeam = rencontreRepository.findByEquipeVisiteurs(equipeVisiteur);
                    // Recuperer les autorisations pour chacune des rencontres differentes de l'initiale
                    for (Rencontre rencontre : rencontresOfTeam){
                        if (!rencontre.getId().equals(rencontreId)){
                            List<AutorisationRencontre> otherAuthorizations = autorisationrencontreRepository.findByRencontreFk(rencontre.getId());
                            // Pour le membre concerne et le type VALIDATION, supprimer l'autorisation
                            for (AutorisationRencontre otherAuthorization : otherAuthorizations){
                                if (otherAuthorization.getType().equals(TypeAutorisation.VALIDATION)
                                        && otherAuthorization.getMembre().getId().equals(autorisationRencontre.getMembre().getId())){
                                    autorisationrencontreRepository.deleteById(otherAuthorization.getId());
                                }
                            }
                        }
                    }
                }
                return true;
            }
        }
        throw new ForbiddenException();
    }


    // Les methodes de modification des resultats et validation sont etendues pour recuperer la liste de ces autorisations sur base de la rencontre

    @RequestMapping(value = "/private/rencontre/{rencontreId}/isResultatsModifiables", method = RequestMethod.GET)
    public boolean isResultatsRencontreModifiables(Authentication authentication, @PathVariable("rencontreId") Long rencontreId) {

        // Tester le fait d'etre capitaine de l'equipe visites ou resp. visites ou admin + analyse des autorisations donnees

        Rencontre rencontre = rencontreRepository.findById(rencontreId).get();
        if (!rencontre.isResultatsEncodes() && !rencontre.isValide()){
            if (rencontre.getDivision().getChampionnat() != null) {
                if (rencontre.getDivision().getChampionnat().isCalendrierValide() && !rencontre.getDivision().getChampionnat().isCloture()) {

                    // Administrateur : OK
                    if (userService.isAdmin(authentication)){
                        return true;
                    }
                    // Capitaine Equipe visites : OK
                    // Responsable Club visite : OK
                    Membre membre = userService.getMembreFromAuthentication(authentication);
                    boolean capitaineOrResponsableVisites = isCapitaineOrResponsableClub(membre,rencontre.getEquipeVisites());
                    if (capitaineOrResponsableVisites){
                        return true;
                    }

                    // Analyse des autorisations

                    List<AutorisationRencontre> autorisationRencontres = autorisationrencontreRepository.findByRencontreFk(rencontreId);
                    for (AutorisationRencontre autorisationRencontre : autorisationRencontres){
                        if (TypeAutorisation.ENCODAGE.equals(autorisationRencontre.getType()) && autorisationRencontre.getMembre().equals(membre)){
                            return true;
                        }
                    }

                }
            }
        }
        return false;
    }

    @RequestMapping(value = "/private/rencontre/{rencontreId}/isForfaitPossible", method = RequestMethod.GET)
    public boolean isForfaitPossible(Authentication authentication, @PathVariable("rencontreId") Long rencontreId) {
        if (userService.isAdmin(authentication)){
            Rencontre rencontre = rencontreRepository.findById(rencontreId).get();
            if ((rencontre.getPointsVisites()==null || rencontre.getPointsVisites()==0) && (rencontre.getPointsVisiteurs()==null || rencontre.getPointsVisiteurs()==0)) {
                return true;
            }
        }
        return false;

    }

    @RequestMapping(value = "/private/rencontre/{rencontreId}/isResultatsCloturables", method = RequestMethod.GET)
    public boolean isResultatsCloturables(Authentication authentication, @PathVariable("rencontreId") Long rencontreId) {

        // Tester le fait d'etre capitaine de l'equipe visites ou resp. visites ou admin ou en fonction des autorisations

        boolean totauxSuffisants = false;

        Rencontre rencontre = rencontreRepository.findById(rencontreId).get();

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
                            totauxSuffisants = totalPoints == 12;
                        }

                    } else if (TypeChampionnat.COUPE_HIVER.equals(championnat.getType())) {

                        // 4 rencontres de 2 points

                        if (rencontre.getPointsVisites() != null && rencontre.getPointsVisiteurs() != null) {
                            Integer totalPoints = rencontre.getPointsVisites() + rencontre.getPointsVisiteurs();
                            totauxSuffisants = totalPoints == 8;
                        }

                    } else if (TypeChampionnat.CRITERIUM.equals(championnat.getType())) {

                        if (rencontre.getPointsVisites() != null && rencontre.getPointsVisiteurs() != null) {
                            Integer totalPoints = rencontre.getPointsVisites() + rencontre.getPointsVisiteurs();
                            totauxSuffisants = totalPoints == 2;
                        }

                    }
                }
            }
        }

        if (totauxSuffisants){

            // Administrateur : OK
            if (userService.isAdmin(authentication)){
                return true;
            }
            // Capitaine Equipe visites : OK
            // Responsable Club visite : OK
            Membre membre = userService.getMembreFromAuthentication(authentication);
            boolean capitaineOrResponsableVisites = isCapitaineOrResponsableClub(membre,rencontre.getEquipeVisites());
            if (capitaineOrResponsableVisites){
                return true;
            }

            // Analyse des autorisations

            List<AutorisationRencontre> autorisationRencontres = autorisationrencontreRepository.findByRencontreFk(rencontreId);
            for (AutorisationRencontre autorisationRencontre : autorisationRencontres){
                if (TypeAutorisation.ENCODAGE.equals(autorisationRencontre.getType()) && autorisationRencontre.getMembre().equals(membre)){
                    return true;
                }
            }
        }

        return false;
    }

    @RequestMapping(value = "/private/rencontre/{rencontreId}/isPoursuiteEncodagePossible", method = RequestMethod.GET)
    public boolean isPoursuiteEncodagePossible(Authentication authentication, @PathVariable("rencontreId") Long rencontreId) {

        // Tester le fait d'etre capitaine des equipes ou responsables des clubs ou admin

        Rencontre rencontre = rencontreRepository.findById(rencontreId).get();
        if (rencontre.isResultatsEncodes() && !rencontre.isValide()) {
            if (rencontre.getDivision().getChampionnat().isCalendrierValide() && !rencontre.getDivision().getChampionnat().isCloture()){


                // Administrateur : OK
                if (userService.isAdmin(authentication)){
                    return true;
                }
                // Capitaine Equipe visites : OK
                // Responsable Club visite : OK
                Membre membre = userService.getMembreFromAuthentication(authentication);
                boolean capitaineOrResponsableVisites = isCapitaineOrResponsableClub(membre,rencontre.getEquipeVisites());
                if (capitaineOrResponsableVisites){
                    return true;
                }
                boolean capitaineOrResponsableVisiteurs = isCapitaineOrResponsableClub(membre,rencontre.getEquipeVisiteurs());
                if (capitaineOrResponsableVisiteurs){
                    return true;
                }

                // Analyse des autorisations

                List<AutorisationRencontre> autorisationRencontres = autorisationrencontreRepository.findByRencontreFk(rencontreId);
                for (AutorisationRencontre autorisationRencontre : autorisationRencontres){
                    if (autorisationRencontre.getMembre().equals(membre)){
                        return true;
                    }
                }

            }
        }
        return false;
    }

    @RequestMapping(value = "/private/rencontre/{rencontreId}/isEtatValidable", method = RequestMethod.GET)
    public boolean isRencontreEtatValidable(Authentication authentication, @PathVariable("rencontreId") Long rencontreId) {

        // Tester le fait d'etre capitaine de l'equipe visites ou resp. visites ou admin

        Rencontre rencontre = rencontreRepository.findById(rencontreId).get();
        if (rencontre.isResultatsEncodes() && !rencontre.isValide()) {
            if (rencontre.getDivision().getChampionnat().isCalendrierValide() && !rencontre.getDivision().getChampionnat().isCloture()){

                // Administrateur : OK
                if (userService.isAdmin(authentication)){
                    return true;
                }
                // Capitaine Equipe visites : OK
                // Responsable Club visite : OK
                Membre membre = userService.getMembreFromAuthentication(authentication);
                boolean capitaineOrResponsableVisites = isCapitaineOrResponsableClub(membre,rencontre.getEquipeVisites());
                if (capitaineOrResponsableVisites){
                    return true;
                }

                // Analyse des autorisations

                List<AutorisationRencontre> autorisationRencontres = autorisationrencontreRepository.findByRencontreFk(rencontreId);
                for (AutorisationRencontre autorisationRencontre : autorisationRencontres){
                    if (TypeAutorisation.ENCODAGE.equals(autorisationRencontre.getType()) && autorisationRencontre.getMembre().equals(membre)){
                        return true;
                    }
                }


            }
        }
        return false;
    }

    @RequestMapping(value = "/private/rencontre/{rencontreId}/isValidable", method = RequestMethod.GET)
    public boolean isRencontreValidable(Authentication authentication, @PathVariable("rencontreId") Long rencontreId) {

        // Tester le fait d'etre capitaine de l'equipe visiteurs ou resp. visiteurs ou admin

        Rencontre rencontre = rencontreRepository.findById(rencontreId).get();
        if (rencontre.isResultatsEncodes() && !rencontre.isValide()) {
            if (rencontre.getDivision().getChampionnat().isCalendrierValide() && !rencontre.getDivision().getChampionnat().isCloture()){

                // Administrateur : OK
                if (userService.isAdmin(authentication)){
                    return true;
                }
                // Capitaine Equipe visiteurs : OK
                // Responsable Club visiteur : OK
                Membre membre = userService.getMembreFromAuthentication(authentication);
                boolean capitaineOrResponsableVisiteurs = isCapitaineOrResponsableClub(membre,rencontre.getEquipeVisiteurs());
                if (capitaineOrResponsableVisiteurs){
                    return true;
                }

                // Analyse des autorisations

                List<AutorisationRencontre> autorisationRencontres = autorisationrencontreRepository.findByRencontreFk(rencontreId);
                for (AutorisationRencontre autorisationRencontre : autorisationRencontres){
                    if (TypeAutorisation.VALIDATION.equals(autorisationRencontre.getType()) && autorisationRencontre.getMembre().equals(membre)){
                        return true;
                    }
                }


            }
        }
        return false;
    }

    //TODO : envoi d'un mail/sms si poursuite ou validation a effectuer

    @RequestMapping(value = "/private/rencontre/{rencontreId}/resultatsEncodes", method = RequestMethod.PUT)
    public boolean updateResultatsEncodes(Authentication authentication, @PathVariable("rencontreId") Long rencontreId, @RequestParam boolean resultatsEncodes, @RequestBody(required = false) String message) {

        String trace = "";

        if (resultatsEncodes) {

            // seul le capitaine visites, responsable club visite et admin peuvent signaler la fin de l'encodage --> via le test "is.."
            // tracer qui a finalisé l'encodage --> table pour conserver l'activite sur une rencontre

            if (isResultatsCloturables(authentication,rencontreId)) {
                rencontreRepository.updateResultatsEncodes(rencontreId, resultatsEncodes);
                trace = "Encodage des résultats terminé";
            } else {
                return false;
            }
        } else {
            if (isPoursuiteEncodagePossible(authentication,rencontreId)){

                // Les capitaines des equipes, responsable de clubs et admin peuvent demander la poursuite de l'encodage --> via le test "is..."

                rencontreRepository.updateResultatsEncodes(rencontreId, resultatsEncodes);
                trace = "Poursuite de l'encodage des résultats";
            }
        }

        if (message!=null){
            trace += " : " + message;
        }

        traceService.addTrace(authentication,"rencontre",rencontreId.toString(),trace);

        return resultatsEncodes;
    }

    @RequestMapping(value = "/private/rencontre/{rencontreId}/validite", method = RequestMethod.PUT)
    public boolean updateValiditeRencontre(Authentication authentication, @PathVariable("rencontreId") Long rencontreId, @RequestParam boolean validite, @RequestBody(required = false) String message) {

        String trace = "";

        if (validite) {

            // Seul le capitaine visiteurs, responsable club visiteur et admin peuvent signaler la fin de l'encodage --> via le test "is.."
            // Tracer qui a validé

            if (isRencontreValidable(authentication,rencontreId)) {
                rencontreRepository.updateValiditeRencontre(rencontreId, validite);
                trace = "Validation des résultats";
            } else {
                return false;
            }
        } else {

            // Only admin peut devalider une rencontre tant que le championnat n'est pas cloture
            // Tracer qui a dévalidé

            Rencontre rencontre = rencontreRepository.findById(rencontreId).get();
            if (!rencontre.getDivision().getChampionnat().isCloture() && (userService.isAdmin(authentication))){
                rencontreRepository.updateValiditeRencontre(rencontreId, validite);
                trace = "Dévalidation des résultats";
            }else{
                return true;
            }
        }

        if (message!=null){
            trace += " : " + message;
        }

        traceService.addTrace(authentication,"rencontre",rencontreId.toString(),trace);

        return validite;
    }

    @RequestMapping(value = "/private/rencontre/{rencontreId}/validiteParAdversaire", method = RequestMethod.PUT)
    public boolean updateValiditeRencontreParAdversaire(Authentication authentication, @PathVariable("rencontreId") Long rencontreId, @RequestParam Long adversaireId, @RequestBody String password) {

        String trace = "";

        // Seul le capitaine visites, responsable club visite et admin peuvent demander une validation par l'adversaire --> via le test "is.."
        // Tracer qui a validé

        if (isRencontreEtatValidable(authentication,rencontreId)) {

            // Tester la correspondance adversaire/mot de passe

            Membre adversaire = membreRepository.findById(adversaireId).get();
            if (adversaire!=null){

                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                // On teste si le mot de passe coïncide
                if (encoder.matches(password, adversaire.getPassword())) {

                    rencontreRepository.updateValiditeRencontre(rencontreId, true);
                    trace = "Validation des résultats par adversaire (" + adversaire.getNom() + " " + adversaire.getPrenom() + ")";

                    traceService.addTrace(authentication,"rencontre",rencontreId.toString(),trace);

                    return true;

                }

            }

        }

        return false;

    }

    /**
     * Permet de tester si un utilisateur connecte est capitaine ou responsable de club d'une equipe
     * @param authentication
     * @param equipe
     * @return
     */
    private boolean isCapitaineOrResponsableClub(Authentication authentication, Equipe equipe){
        Membre membre = userService.getMembreFromAuthentication(authentication);
        return isCapitaineOrResponsableClub(membre, equipe);
    }

    /**
     * Permet de tester si un membre est capitaine ou responsable de club d'une equipe
     * @param membre
     * @param equipe
     * @return
     */
    private boolean isCapitaineOrResponsableClub(Membre membre, Equipe equipe){
        if (membre!=null){
            if (membre.equals(equipe.getCapitaine())){
                return true;
            }
            if (equipe.getClub().equals(membre.getClub())){
                if (membre.isResponsableClub()){
                    return true;
                }
            }
        }
        return false;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(method = RequestMethod.GET, path = "/private/rencontres/interseries")
    public List<RencontreDto> getInterseries(@RequestParam Long championnatId) {

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

        Championnat championnat = championnatRepository.findById(championnatId).get();
        if (!championnat.isCalendrierValide() || championnat.isCloture()) {
            throw new RuntimeException("Operation not supported - Championnat cloture");
        }

        List<Rencontre> rencontresInterseries = new ArrayList<>();

        if (TypeChampionnat.ETE.equals(championnat.getType()) || TypeChampionnat.HIVER.equals(championnat.getType())) {

            List<Division> divisions = (List<Division>) divisionRepository.findByChampionnat(championnat);
            for (Division division : divisions) {

                List<Poule> poules = (List<Poule>) pouleRepository.findByDivision(division);
                Collections.sort(poules, new Comparator<Poule>() {
                    @Override
                    public int compare(Poule o1, Poule o2) {
                        return o1.getNumero().compareTo(o2.getNumero());
                    }
                });

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
                                if (division.isMultiIS() && division.isWithFinales() && (i==0 || i==1)){
                                    rencontre.setInformationsInterserie("Demi-finale");
                                }else if (division.isMultiIS() && division.isWithFinales() && i>1){
                                    rencontre.setInformationsInterserie("Match pour la " + ((i*2)+1) + "eme place");
                                }else if (division.isMultiIS() && !division.isWithFinales()){
                                    rencontre.setInformationsInterserie("Match pour la " + ((i*2)+1) + "eme place");
                                }else if (!division.isMultiIS() && division.isWithFinales()){
                                    rencontre.setInformationsInterserie("Demi-finale");
                                }else if (!division.isMultiIS() && !division.isWithFinales()){
                                    rencontre.setInformationsInterserie("Finale");
                                    rencontre.setFinaleInterserie(true);
                                }

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

                                List<Rencontre> rencontresA = rencontreRepository.getRencontresByDivisionAndEquipes(division.getId(), equipeA.getId(), adversaireA.getId());
                                // On est cense n'avoir qu'une seule rencontre opposant ces deux equipes
                                if (rencontresA.size() > 0) {
                                    Rencontre interserie = rencontresA.get(0);
                                    if (interserie.isValide()) {
                                        equipeGagnanteA = classementService.getGagnantRencontreInterserie(interserie);
                                        if (interserie.getEquipeVisites().equals(equipeGagnanteA)) {
                                            equipePerdanteA = interserie.getEquipeVisiteurs();
                                        } else {
                                            equipePerdanteA = interserie.getEquipeVisites();
                                        }
                                    }
                                }

                                List<Rencontre> rencontresB = rencontreRepository.getRencontresByDivisionAndEquipes(division.getId(), equipeB.getId(), adversaireB.getId());

                                // On est cense n'avoir qu'une seule rencontre opposant ces deux equipes
                                if (rencontresB.size() > 0) {
                                    Rencontre interserie = rencontresB.get(0);
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
                                    petiteFinale.setInformationsInterserie("Petite finale");
                                    petiteFinale.setDivision(division);
                                    petiteFinale.setEquipeVisites(equipePerdanteA);
                                    petiteFinale.setEquipeVisiteurs(equipePerdanteB);

                                    if (!isInterserieExists(petiteFinale)) {
                                        rencontresInterseries.add(petiteFinale);
                                    }

                                    // Creation de la grande finale

                                    Rencontre finale = new Rencontre();
                                    finale.setInformationsInterserie("Finale");
                                    finale.setFinaleInterserie(true);
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

        List<RencontreDto> interseriesDto = new ArrayList<>();
        for (Rencontre rencontre : rencontresInterseries){
            interseriesDto.add(new RencontreDto(rencontre));
        }

        return interseriesDto;

    }

    /**
     * Permet de verifier si la rencontre interserie existe deja dans le systeme
     *
     * @param rencontre Rencontre
     * @return
     */
    private boolean isInterserieExists(Rencontre rencontre) {
        Long nbSameRencontreInterserie = rencontreRepository.countInterserieByDivisionAndEquipes(rencontre.getDivision().getId(), rencontre.getEquipeVisites().getId(), rencontre.getEquipeVisiteurs().getId());
        return nbSameRencontreInterserie > 0;
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
        Championnat championnat = championnatRepository.findById(championnatId).get();
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

        List<Rencontre> newRencontres = rencontreService.refreshRencontres(anciennesRencontres, nouvellesRencontres);

        List<Rencontre> rencontresSaved = rencontreService.saveRencontres(newRencontres);

        // Calendrier rafraichi --> false
        championnatRepository.updateCalendrierARafraichir(championnatId, false);

        return rencontresSaved;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(method = RequestMethod.DELETE, path = "/private/rencontres/calendrier")
    public void deleteCalendrier(@RequestParam Long championnatId) {
        Championnat championnat = championnatRepository.findById(championnatId).get();
        if (!championnat.isCalendrierValide()) {
            rencontreService.deleteByChampionnat(championnatId);
        }
    }

    @RequestMapping(path = "/public/rencontres/calendrier", method = RequestMethod.GET)
    ResponseEntity<byte[]> getCalendrier(@RequestParam Long championnatId, @RequestParam(required = false) boolean excel) throws Exception {

        TimeZone timeZone = TimeZone.getTimeZone("Europe/Paris");

        if (excel) {
            Championnat championnat = championnatRepository.findById(championnatId).get();
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
            POIUtils.write(sheet, 0, 3, "Club", null, null);
            POIUtils.write(sheet, 0, 4, "Court", null, null);
            POIUtils.write(sheet, 0, 5, "Visites", null, null);
            Cell lastCell = POIUtils.write(sheet, 0, 6, "Visiteurs", null, null);

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
                if (rencontre.getCourt()!=null){
                    lastCell = POIUtils.write(sheet, i + 1, 4, rencontre.getCourt().getCode(), null, null);
                }
                lastCell = POIUtils.write(sheet, i + 1, 5, rencontre.getEquipeVisites().getCodeAlphabetique(), null, null);
                lastCell = POIUtils.write(sheet, i + 1, 6, rencontre.getEquipeVisiteurs().getCodeAlphabetique(), null, null);

            }

            // Freeze de la premiere ligne
            sheet.createFreezePane(0, 1);

            // Auto-resize des colonnes
            for (int i = 0; i < 7; i++) {
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

        Terrain terrainCriterium = null;
        // S'il s'agit du criterium, on va recuperer le terrain par defaut a utiliser
        if (TypeChampionnat.CRITERIUM.equals(poule.getDivision().getChampionnat().getType())){
            List<Terrain> terrains = (List<Terrain>) terrainRepository.findByTerrainCriteriumParDefaut(true);
            if (terrains !=null && terrains.size()==1){
                terrainCriterium = terrains.get(0);
            }
        }

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

                // En cas de criterium, recuperer le terrain dedie a ce championnat
                if (TypeChampionnat.CRITERIUM.equals(poule.getDivision().getChampionnat().getType())){
                    rencontre.setTerrain(terrainCriterium);
                }else{
                    // Si l'equipe visitee possede un terrain, on le precise pour la rencontre
                    if (rencontre.getEquipeVisites().getTerrain() != null) {
                        rencontre.setTerrain(rencontre.getEquipeVisites().getTerrain());
                    }
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

                // En cas de criterium, recuperer le terrain dedie a ce championnat
                if (TypeChampionnat.CRITERIUM.equals(poule.getDivision().getChampionnat().getType())){
                    rencontreRetour.setTerrain(terrainCriterium);
                }else {
                    // Si l'equipe visitee possede un terrain, on le precise pour la rencontre
                    if (rencontreRetour.getEquipeVisites().getTerrain() != null) {
                        rencontreRetour.setTerrain(rencontreRetour.getEquipeVisites().getTerrain());
                    }
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
