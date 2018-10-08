package be.company.fca.controller;

import be.company.fca.dto.RencontreDto;
import be.company.fca.model.*;
import be.company.fca.repository.*;
import be.company.fca.service.ClassementService;
import be.company.fca.service.RencontreService;
import be.company.fca.utils.DateUtils;
import be.company.fca.utils.ReportUtils;
import io.swagger.annotations.Api;
import net.sf.jasperreports.engine.*;
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

    @RequestMapping(method= RequestMethod.GET, path="/public/rencontres")
    public List<RencontreDto> getRencontresByDivisionOrPouleOrEquipe(@RequestParam Long divisionId,@RequestParam(required = false) Long pouleId, @RequestParam(required = false) Long equipeId) {
        List<RencontreDto> rencontresDto = new ArrayList<>();
        List<Rencontre> rencontres = new ArrayList<Rencontre>();

        if (equipeId!=null){
            Equipe equipe = new Equipe();
            equipe.setId(equipeId);
            rencontres = (List<Rencontre>) rencontreRepository.findRencontresByEquipe(equipe);
        }else if (pouleId!=null){
            Poule poule = new Poule();
            poule.setId(pouleId);
            rencontres = (List<Rencontre>) rencontreRepository.findByPoule(poule);
        }else{
            Division division = new Division();
            division.setId(divisionId);
            rencontres = (List<Rencontre>) rencontreRepository.findByDivision(division);
        }

        for (Rencontre rencontre : rencontres){
            rencontresDto.add(new RencontreDto(rencontre));
        }

        return rencontresDto;
    }

    /**
     * Permet de recuperer les 5 derniers resultats encodes (rencontres validees) dans le systeme
     * @return
     */
    @RequestMapping(method= RequestMethod.GET, path="/public/rencontres/last")
    public List<Rencontre> getLastTenResults(@RequestParam Integer numberOfResults){
        Pageable pageRequest = new PageRequest(0,numberOfResults);
        return rencontreRepository.getLastResults(pageRequest);
    }

    @RequestMapping(method= RequestMethod.GET, path="/public/rencontres/next")
    public List<Rencontre> getComingMeetings(@RequestParam Integer numberOfResults){
        Pageable pageRequest = new PageRequest(0,numberOfResults);
        return rencontreRepository.getNextMeetings(DateUtils.shrinkToDay(new Date()),pageRequest);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/rencontre", method = RequestMethod.POST)
    public Rencontre createRencontre(@RequestBody Rencontre rencontre){
        return rencontreRepository.save(rencontre);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/rencontre", method = RequestMethod.PUT)
    public Rencontre updateRencontre(@RequestBody Rencontre rencontre){
        return rencontreRepository.save(rencontre);
    }

    @RequestMapping(value = "/public/rencontre/{rencontreId}/isValidable", method = RequestMethod.GET)
    public boolean isRencontreValidable(@PathVariable("rencontreId") Long rencontreId){
        Rencontre rencontre = rencontreRepository.findOne(rencontreId);

        if (rencontre.getDivision().getChampionnat()!=null){

            // La validite change en fonction du type et de la categorie de championnat

            // Criterium : Simple -> au moins 2 points atteints par l'une des deux equipes
            // Coupe d'hiver : au moins 8 points atteints au total des deux equipes


            Championnat championnat = rencontre.getDivision().getChampionnat();
            if (TypeChampionnat.HIVER.equals(championnat.getType())
                    || TypeChampionnat.ETE.equals(championnat.getType())) {

                if (rencontre.getPointsVisites()!=null && rencontre.getPointsVisiteurs()!=null){

                    // 6 rencontres de 2 points chacune

                    Integer totalPoints = rencontre.getPointsVisites() + rencontre.getPointsVisiteurs();
                    return totalPoints == 12;
                }

            } else if (TypeChampionnat.COUPE_HIVER.equals(championnat.getType())) {

                // 4 rencontres de 2 points

                if (rencontre.getPointsVisites()!=null && rencontre.getPointsVisiteurs()!=null){
                    Integer totalPoints = rencontre.getPointsVisites() + rencontre.getPointsVisiteurs();
                    return totalPoints == 8;
                }

            } else if (TypeChampionnat.CRITERIUM.equals(championnat.getType())){

                if (rencontre.getPointsVisites()!=null && rencontre.getPointsVisiteurs()!=null){
                    Integer totalPoints = rencontre.getPointsVisites() + rencontre.getPointsVisiteurs();
                    return totalPoints == 2;
                }

            }
        }

        return false;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/rencontre/{rencontreId}/validite", method = RequestMethod.PUT)
    public boolean updateValiditeRencontre(@PathVariable("rencontreId") Long rencontreId, @RequestBody boolean validite){
        if (validite) {
            if (isRencontreValidable(rencontreId)){
                rencontreRepository.updateValiditeRencontre(rencontreId,validite);
            }else{
                return false;
            }
        }else{
            rencontreRepository.updateValiditeRencontre(rencontreId,validite);
        }

        return validite;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(method= RequestMethod.GET, path="/private/rencontres/interseries")
    public List<Rencontre> getInterseries(@RequestParam Long championnatId){

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
        if (!championnat.isCalendrierValide() || championnat.isCloture()){
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
                            if (division.isMultiIS()){
                                maxInterserie = Math.min(classementPoule1.getClassementEquipes().size(), classementPoule2.getClassementEquipes().size());
                                if (division.isWithFinales()){
                                    switchFirsts = true;
                                }
                            }else{
                                // Si seul l'aspect "finales" est precise, seules les deux premieres rencontres vont etre jouees
                                if (division.isWithFinales()){
                                    maxInterserie = 2;
                                    switchFirsts = true;
                                }
                            }

                            // On conserve les informations des deux premieres equipes classees pour le cas des petite et grande finale
                            Equipe equipeA = null;
                            Equipe adversaireA = null;
                            Equipe equipeB = null;
                            Equipe adversaireB = null;

                            for (int i=0;i<maxInterserie;i++){
                                int realI = i;
                                // On inverse les adversaires pour les deux premieres rencontres (premier contre second)
                                if (switchFirsts){
                                    if (i==0){
                                        realI = 1;
                                    }else if (i==1){
                                        realI = 0;
                                    }
                                }
                                Equipe equipe1 = classementPoule1.getClassementEquipes().get(i).getEquipe();
                                Equipe equipe2 = classementPoule2.getClassementEquipes().get(realI).getEquipe();
                                if (i==0){
                                    equipeA = equipe1;
                                    adversaireA = equipe2;
                                }else if (i==1){
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
                            if (division.isWithFinales()){
                                // Analyser si les deux rencontres interseries concernes ont ete jouees et validees

                                //rencontreA1B2 jouee et validee;
                                //rencontreB1A2 jouee et validee;

                                // Si oui, se faire rencontrer les vainqueurs d'un cote et les perdants de l'autre
                                Equipe equipeGagnanteA = null;
                                Equipe equipeGagnanteB = null;
                                Equipe equipePerdanteA = null;
                                Equipe equipePerdanteB = null;

                                List<Rencontre> rencontres = rencontreRepository.getRencontresByDivisionAndEquipes(division.getId(),equipeA.getId(),adversaireA.getId());
                                // On est cense n'avoir qu'une seule rencontre opposant ces deux equipes
                                if (rencontres.size()>0){
                                    Rencontre interserie = rencontres.get(0);
                                    if (interserie.isValide()) {
                                        equipeGagnanteA = classementService.getGagnantRencontreInterserie(interserie);
                                        if (interserie.getEquipeVisites().equals(equipeGagnanteA)){
                                            equipePerdanteA = interserie.getEquipeVisiteurs();
                                        }else{
                                            equipePerdanteA = interserie.getEquipeVisites();
                                        }
                                    }
                                }
                                rencontreRepository.getRencontresByDivisionAndEquipes(division.getId(),equipeB.getId(),adversaireB.getId());
                                // On est cense n'avoir qu'une seule rencontre opposant ces deux equipes
                                if (rencontres.size()>0){
                                    Rencontre interserie = rencontres.get(0);
                                    if (interserie.isValide()) {
                                        equipeGagnanteB = classementService.getGagnantRencontreInterserie(interserie);
                                        if (interserie.getEquipeVisites().equals(equipeGagnanteB)){
                                            equipePerdanteB = interserie.getEquipeVisiteurs();
                                        }else{
                                            equipePerdanteB = interserie.getEquipeVisites();
                                        }
                                    }
                                }

                                if (equipeGagnanteA!=null && equipePerdanteA !=null && equipeGagnanteB != null && equipePerdanteB !=null){

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
     * @param rencontre Rencontre
     * @return
     */
    private boolean isInterserieExists(Rencontre rencontre){
        Long nbSameRencontre = rencontreRepository.countByDivisionAndEquipes(rencontre.getDivision().getId(),rencontre.getEquipeVisites().getId(),rencontre.getEquipeVisiteurs().getId());
        return nbSameRencontre>0;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(method= RequestMethod.POST, path="/private/rencontres/calendrier")
    public Iterable<Rencontre> createCalendrier(@RequestParam Long championnatId) {

        List<Rencontre> rencontres = new ArrayList<Rencontre>();

        Championnat championnat = new Championnat();
        championnat.setId(championnatId);
        Iterable<Division> divisionList = divisionRepository.findByChampionnat(championnat);
        for (Division division : divisionList){
            Iterable<Poule> pouleList = pouleRepository.findByDivision(division);
            for (Poule poule : pouleList){
                rencontres.addAll(generateCalendar(poule));

            }
        }

        List<Rencontre> rencontresSaved = rencontreService.saveRencontres(rencontres);

        // Calendrier rafraichi --> false
        championnatRepository.updateCalendrierARafraichir(championnatId,false);

        return rencontresSaved;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(method= RequestMethod.PUT, path="/private/rencontres/calendrier")
    public Iterable<Rencontre> refreshCalendrier(@RequestParam Long championnatId){

        // On peut faire un refresh tant que le calendrier n'a pas ete valide
        Championnat championnat = championnatRepository.findOne(championnatId);
        if (championnat.isCalendrierValide()){
            throw new RuntimeException("Operation not supported - Calendrier validé");
        }

        List<Rencontre> anciennesRencontres = new ArrayList<>();
        List<Rencontre> nouvellesRencontres = new ArrayList<>();
        Iterable<Division> divisionList = divisionRepository.findByChampionnat(championnat);
        for (Division division : divisionList){
            Iterable<Poule> pouleList = pouleRepository.findByDivision(division);
            for (Poule poule : pouleList){
                anciennesRencontres.addAll((Collection<? extends Rencontre>) rencontreRepository.findByPoule(poule));
                nouvellesRencontres.addAll(generateCalendar(poule));
            }
        }

        List<Rencontre> rencontresSaved = rencontreService.refreshRencontres(anciennesRencontres,nouvellesRencontres);

        // Calendrier rafraichi --> false
        championnatRepository.updateCalendrierARafraichir(championnatId,false);

        return rencontresSaved;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(method= RequestMethod.DELETE, path="/private/rencontres/calendrier")
    public void deleteCalendrier(@RequestParam Long championnatId) {
        Championnat championnat = championnatRepository.findOne(championnatId);
        if (!championnat.isCalendrierValide()){
            rencontreService.deleteByChampionnat(championnatId);
        }
    }


//    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(path="/public/rencontres/calendrier", method= RequestMethod.GET)
    ResponseEntity<byte[]> getCalendrier(@RequestParam Long championnatId) throws Exception {
        JasperReport jasperReport = JasperCompileManager.compileReport(ReportUtils.getCalendrierTemplate());
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


    /**
     * Permet de generer le calendrier d'une poule
     * @param poule Poule
     * @return Liste des rencontres d'une poule
     */
    private List<Rencontre> generateCalendar(Poule poule){

        List<Rencontre> rencontres = new ArrayList<>();

        List<Equipe> equipes = (List<Equipe>) equipeRepository.findByPoule(poule);

        if (equipes.size()<2){
            return rencontres;
        }

        int nbJournees = getNbJournees(equipes);
        int nbRencontresParJournee = equipes.size() / 2;
        //System.err.println("Nb rencontres par journee : " + nbRencontresParJournee);
        //System.err.println("Nombre de matchs à jouer :" + nbJournees*nbRencontresParJournee);

        // Decoupe en journees

        for (int i=0;i<nbJournees;i++){

            // S'il s'agit d'un nombre pair d'equipes, on va boucler sur toutes les equipes sauf la premiere
            List<Equipe> equipesReduites = new ArrayList<>(equipes.subList(1,equipes.size()));

            for (int j=0;j<nbRencontresParJournee;j++){

                Rencontre rencontre = new Rencontre();
                rencontre.setPoule(poule);
                rencontre.setDivision(poule.getDivision());
                rencontre.setNumeroJournee(i+1);

                // Pour un nombre d'equipes impair, permutation circulaire parmi toutes les equipes --> l'une sera bye via la procedure
                if (nombreEquipesImpair(equipes)){
                    rencontre.setEquipeVisites(equipes.get((0+j+i)%equipes.size()));
                    rencontre.setEquipeVisiteurs(equipes.get( (equipes.size() - 1 - j + i)%equipes.size() ));
                }else{
                    // Pour un nombre d'equipes pair,
                    // On va garder la premiere equipe fixe et faire tourner les autres
                    if (j==0){
                        rencontre.setEquipeVisites(equipes.get(0));
                        rencontre.setEquipeVisiteurs(equipesReduites.get( (equipesReduites.size() - 1 - j + i) % equipesReduites.size() ));
                    }else{
                        rencontre.setEquipeVisites(equipesReduites.get( (equipesReduites.size()-1 + i + j) % equipesReduites.size()));
                        rencontre.setEquipeVisiteurs(equipesReduites.get( (equipesReduites.size() - 1 - j + i) % equipesReduites.size() ));
                    }
                }

                // Si l'equipe visitee possede un terrain, on le precise pour la rencontre
                if (rencontre.getEquipeVisites().getTerrain()!=null){
                    rencontre.setTerrain(rencontre.getEquipeVisites().getTerrain());
                }

                //System.err.println(rencontre);
                rencontres.add(rencontre);

            }

        }

        // Si la poule est specifee avec matchs aller-retour, on va dupliquer les rencontres
        //  en inversant les equipes visites-visiteurs
        if (poule.isAllerRetour()){
            List<Rencontre> rencontresRetour = new ArrayList<>();

            for (Rencontre rencontreAller : rencontres){
                Rencontre rencontreRetour = new Rencontre();
                rencontreRetour.setPoule(poule);
                rencontreRetour.setDivision(poule.getDivision());
                rencontreRetour.setNumeroJournee(nbJournees + rencontreAller.getNumeroJournee());
                rencontreRetour.setEquipeVisites(rencontreAller.getEquipeVisiteurs());
                rencontreRetour.setEquipeVisiteurs(rencontreAller.getEquipeVisites());

                // Si l'equipe visitee possede un terrain, on le precise pour la rencontre
                if (rencontreRetour.getEquipeVisites().getTerrain()!=null){
                    rencontreRetour.setTerrain(rencontreRetour.getEquipeVisites().getTerrain());
                }

                rencontresRetour.add(rencontreRetour);
            }

            rencontres.addAll(rencontresRetour);
        }

        return rencontres;

    }

    private boolean nombreEquipesImpair(List<Equipe> equipes){
        return equipes.size()%2!=0;
    }

    private int getNbJournees(List<Equipe> equipes){
        if (nombreEquipesImpair(equipes)){
            return equipes.size();
        }else{
            return equipes.size()-1;
        }
    }

}
