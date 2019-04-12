package be.company.fca.controller;

import be.company.fca.dto.CaracteristiquesMatchDto;
import be.company.fca.dto.InfosCalculClassementDto;
import be.company.fca.dto.MatchDto;
import be.company.fca.exceptions.ForbiddenException;
import be.company.fca.model.*;
import be.company.fca.repository.*;
import be.company.fca.service.ClassementCorpoService;
import be.company.fca.utils.DateUtils;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des classements corpo des membres")
public class ClassementCorpoController {

    @Autowired
    private MembreRepository membreRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private ClassementCorpoRepository classementCorpoRepository;

    @Autowired
    private ClassementCorpoService classementCorpoService;

    @Autowired
    private ClassementJobRepository classementJobRepository;

    @Autowired
    private ClassementJobTraceRepository classementJobTraceRepository;


    // Table de correspondance pour le calcul des points
    Map<TypeChampionnat,Map<ResultatMatch,Map<Integer,Integer>>> correspondancePoints;

    public ClassementCorpoController() {
        initCorrespondancePoints();
    }

    @RequestMapping(path="/public/membre/{membreId}/classementsCorpo", method= RequestMethod.GET)
    Iterable<ClassementCorpo> getClassementsCorpoByMembre(@PathVariable("membreId") Long membreId) {
        return classementCorpoRepository.findByMembreFk(membreId);
    }

    /**
     * Permet de sauvegarder les classements corpo obtenus par un membre
     * Procede a la suppression des anciens classements avant la sauvegarde effective des nouveaux
     *
     * @param membreId Identifiant du membre
     * @param classementCorpoList Liste des classements obtenus par le membre
     * @return Le classement actuel du membre
     */
    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/membre/{membreId}/classementsCorpo", method = RequestMethod.PUT)
    public ClassementCorpo saveClassementsCorpo(@PathVariable("membreId") Long membreId, @RequestBody List<ClassementCorpo> classementCorpoList){

        // delete classement actuel membre --> methode specifique pour ne pas toucher d'autres elements du membre
        membreRepository.updateClassementCorpo(membreId,null);

        ClassementCorpo classementCorpoActuel = classementCorpoService.saveClassementsCorpo(membreId,classementCorpoList);

        // save classement actuel

        membreRepository.updateClassementCorpo(membreId,classementCorpoActuel);

        return classementCorpoActuel;
    }

    /**
     * Permet de calculer un classement corpo a une date donnee
     * en partant d'une autre date
     * Ce classement sera determine en fonction des matchs joues entre les deux dates (3 minimum)
     * @param membreId
     * @param startDate
     * @param endDate
     * @return
     */
    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/membre/{membreId}/classementCorpo/simulation", method = RequestMethod.GET)
    public ClassementCorpo simulationClassement(@PathVariable("membreId") Long membreId, @RequestParam @DateTimeFormat(pattern="yyyyMMdd")  Date startDate, @RequestParam @DateTimeFormat(pattern="yyyyMMdd") Date endDate) {
        ClassementCorpo classementCorpo = new ClassementCorpo();
        classementCorpo.setDateClassement(endDate);
        classementCorpo.setMembreFk(membreId);
        InfosCalculClassementDto infosCalculClassement = getClassementInfos(membreId,startDate,endDate);
        Integer endPoints = infosCalculClassement.getPointsFin();
        classementCorpo.setPoints(endPoints);
        return classementCorpo;
    }

    /**
     * Permet de calculer un classement corpo a une date donnee et de recuperer les differentes informations detaillees ayant mene a ce resultat
     * en partant d'une autre date
     * Ce classement sera determine en fonction des matchs joues entre les deux dates (3 minimum)
     * @param membreId
     * @param startDate
     * @param endDate
     * @return
     */
    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/membre/{membreId}/classementCorpo/simulationWithDetail", method = RequestMethod.GET)
    public InfosCalculClassementDto simulationClassementWithInfos(@PathVariable("membreId") Long membreId, @RequestParam @DateTimeFormat(pattern="yyyyMMdd")  Date startDate, @RequestParam @DateTimeFormat(pattern="yyyyMMdd") Date endDate) {
        return getClassementInfos(membreId,startDate,endDate);
    }

    /**
     * Permet de recuperer les informations du calcul de classement d'un membre
     * entre deux dates
     * @param membreId
     * @param startDate
     * @param endDate
     * @return
     */
    private InfosCalculClassementDto getClassementInfos(Long membreId, Date startDate, Date endDate){

        InfosCalculClassementDto infosCalculClassement = new InfosCalculClassementDto();
        infosCalculClassement.setMembreId(membreId);
        infosCalculClassement.setStartDate(startDate);
        infosCalculClassement.setEndDate(endDate);

        Membre membre = membreRepository.findOne(membreId);

        // On enregistre le classement de depart
        Integer startPoints = getPointsCorpoByMembreAndDate(membre,startDate,false);

        infosCalculClassement.setPointsDepart(startPoints);

        // Recuperer l'ensemble des matchs joues par le membre entre les deux dates

        List<Match> matchs = (List<Match>) matchRepository.findValidesByMembreBetweenDates(membre.getId(),DateUtils.shrinkToDay(startDate),DateUtils.shrinkToDay(endDate));

        // On va filtrer la liste des matchs valides en analysant si tous les joueurs sont bien precises pour simple et double
        // Les matchs où des joueurs manquent à l'appel sont comptabilisés pour les points des rencontres mais ne rentrent pas
        // en ligne de compte pour le calcul des classements

        List<Match> matchsValables = filtreMatchsValables(matchs);

        // On ne calcule un nouveau classement qu'a partir de 3 matchs joues

        Integer totalGagnesPerdus = 0;

        if (matchsValables.size()>=3){

            for (Match match : matchsValables){

                CaracteristiquesMatchDto caracteristiquesMatch = getCaracteristiquesMatch(match,membre);

                infosCalculClassement.getCaracteristiquesMatchList().add(caracteristiquesMatch);

                // Faire la somme et utiliser a la fin la table de conversion des points en "points corpo"

                totalGagnesPerdus+=caracteristiquesMatch.getPointsGagnesOuPerdus();

            }

        }

        Integer pointsClassementsGagnesPerdus = getPointsClassement(totalGagnesPerdus);

        // Calculer les points du classement resultat en se basant sur les points de depart
        // Perte max --> NC --> 5 points
        Integer endPoints = Math.max(5,startPoints + pointsClassementsGagnesPerdus);

        infosCalculClassement.setPointsFin(endPoints);

        return infosCalculClassement;

    }


    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/classementCorpo/job", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ClassementJob calculClassementsMembres(@RequestParam @DateTimeFormat(pattern="yyyyMMdd")  Date startDate, @RequestParam boolean avecSauvegarde) {

        // Si un job est en cours d'execution, on ne peut pas en lancer un autre

        List<ClassementJob> jobs = classementJobRepository.findByStatus(ClassementJobStatus.INITIALIZED);
        if (!jobs.isEmpty()){
            System.err.println("Job en cours d'execution : " + jobs);
            throw new ForbiddenException();
        }

        jobs = classementJobRepository.findByStatus(ClassementJobStatus.WORK_IN_PROGRESS);
        if (!jobs.isEmpty()) {
            System.err.println("Job en cours d'execution : " + jobs);
            throw new ForbiddenException();
        }

        ClassementJob classementJob = new ClassementJob();
        classementJob.setStartDate(startDate);
        classementJob.setEndDate(new Date());
        classementJob.setStatus(ClassementJobStatus.INITIALIZED);
        classementJob.setAvecSauvegarde(avecSauvegarde);

        classementJob = classementJobRepository.save(classementJob);

        ClassementThread classementThread = new ClassementThread(classementJob);
        classementThread.start();

        return classementJob;

    }

    private class ClassementThread extends Thread {

        private ClassementJob classementJob;

        public ClassementThread(ClassementJob classementJob) {
            this.classementJob = classementJob;
        }

        @Override
        public void run() {
            super.run();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy  HH:mm:ss");
            Date startDate = classementJob.getStartDate();
            Date endDate = classementJob.getEndDate();

            classementJob.setStatus(ClassementJobStatus.WORK_IN_PROGRESS);
            classementJobRepository.save(classementJob);

            ClassementJobTrace initTrace = new ClassementJobTrace();
            initTrace.setClassementJob(classementJob);
            initTrace.setMessage(sdf.format(new Date()) + " : Calcul des nouveaux classements en cours");
            classementJobTraceRepository.save(initTrace);

            List<Membre> membres = (List<Membre>) membreRepository.findAll();

            for (Membre membre : membres){
                InfosCalculClassementDto infosCalculClassement = getClassementInfos(membre.getId(),startDate,endDate);

                // Le calcul des classements s'effectue avec une date de fin egale a la date du jour --> nouveau classement = classement actuel du membre

                if (classementJob.isAvecSauvegarde()){
                    ClassementCorpo newClassementCorpo = new ClassementCorpo();
                    newClassementCorpo.setDateClassement(endDate);
                    newClassementCorpo.setMembreFk(membre.getId());
                    Integer endPoints = infosCalculClassement.getPointsFin();
                    newClassementCorpo.setPoints(endPoints);
                    classementCorpoRepository.save(newClassementCorpo);
                    membreRepository.updateClassementCorpo(membre.getId(),newClassementCorpo);
                }

                String trace = "";
                if (infosCalculClassement.getPointsDepart()>infosCalculClassement.getPointsFin()){
                    trace = "Classement inférieur pour " + (membre.getPrenom() + " " + membre.getNom()) + " : " + infosCalculClassement.getPointsFin() + " points";
                }else if (infosCalculClassement.getPointsDepart()<infosCalculClassement.getPointsFin()){
                    trace = "Classement supérieur pour " + (membre.getPrenom() + " " + membre.getNom()) + " : " + infosCalculClassement.getPointsFin() + " points";
                }else{
                    trace = "Classement identique pour " + (membre.getPrenom() + " " + membre.getNom());
                }

                // Enregistrement des traces d'execution du job
                ClassementJobTrace classementJobTrace = new ClassementJobTrace();
                classementJobTrace.setClassementJob(classementJob);
                classementJobTrace.setMessage(sdf.format(new Date()) + " : " + trace);
                classementJobTraceRepository.save(classementJobTrace);

            }

            ClassementJobTrace endTrace = new ClassementJobTrace();
            endTrace.setClassementJob(classementJob);
            endTrace.setMessage(sdf.format(new Date()) + " : Calcul des nouveaux classements terminé");
            classementJobTraceRepository.save(endTrace);

            classementJob.setStatus(ClassementJobStatus.FINISHED);
            classementJobRepository.save(classementJob);

            }
    }


    // Recuperer le job en cours pour afficher les infos sur la page
    // Recuperer les traces d'un job
    // Pouvoir supprimer/(arreter) un job en cours (ou qui a pose probleme - arret inopine)

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(path="/private/classementCorpo/jobs", method= RequestMethod.GET)
    Iterable<ClassementJob> getClassementJobs(@RequestParam(name = "status", required = false) ClassementJobStatus status) {
        if (status!=null){
            return classementJobRepository.findByStatus(status);
        }else{
            return classementJobRepository.findAll();
        }
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(path="/private/classementCorpo/job/{jobId}", method= RequestMethod.GET)
    ClassementJob getClassementJob(@PathVariable Long jobId) {
        return classementJobRepository.findOne(jobId);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(path="/private/classementCorpo/job/{jobId}/traces", method= RequestMethod.GET)
    Iterable<ClassementJobTrace> getClassementJobTraces(@PathVariable Long jobId) {
        ClassementJob classementJob = classementJobRepository.findOne(jobId);
        return classementJobTraceRepository.findByClassementJob(classementJob);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(path="/private/classementCorpo/job/{jobId}", method= RequestMethod.DELETE)
    void deleteClassementJob(@PathVariable Long jobId) {
        ClassementJob classementJob = classementJobRepository.findOne(jobId);
        classementJobTraceRepository.deleteByClassementJob(classementJob);
        classementJobRepository.delete(jobId);
    }

    /**
     * Permet de retourner la liste des matchs "valables" d'une liste de matchs
     * On entend par match valable, un match à prendre en compte pour le calcul des classements corpo
     * Pour ce faire, il faut que le match référence bien tous les joueurs (2 adversaires pour simple, 4 adversaires pour double)
     * @param matchs
     * @return
     */
    private List<Match> filtreMatchsValables(List<Match> matchs){
        List<Match> matchsValables = new ArrayList<>();
        for (Match match : matchs){
            if (TypeMatch.SIMPLE.equals(match.getType())){
                if (match.getJoueurVisites1()!=null && match.getJoueurVisiteurs1()!=null){
                    matchsValables.add(match);
                }
            }else if (TypeMatch.DOUBLE.equals(match.getType())){
                if (match.getJoueurVisites1()!=null && match.getJoueurVisiteurs1()!=null && match.getJoueurVisites2()!=null && match.getJoueurVisiteurs2()!=null){
                    matchsValables.add(match);
                }
            }
        }
        return matchsValables;
    }

    /**
     * Permet de recuperer le resultat d'un match d'un membre
     * @param match
     * @param membre
     * @return
     */
    private ResultatMatch getResultatMatch(Match match, Membre membre){
        boolean visites = membre.equals(match.getJoueurVisites1()) || membre.equals(match.getJoueurVisites2());
        if (match.getPointsVisites() < match.getPointsVisiteurs()){
            return visites?ResultatMatch.defaite:ResultatMatch.victoire;
        }else if (match.getPointsVisites() > match.getPointsVisiteurs()){
            return visites?ResultatMatch.victoire:ResultatMatch.defaite;
        }else{
            return ResultatMatch.matchNul;
        }
    }

    /**
     * Permet de recuperer le classement d'un membre a une date donnee
     * @param membre
     * @param date
     * @return
     */
    private Integer getPointsCorpoByMembreAndDate(Membre membre, Date date, boolean championnatHomme){

        // En championnat hiver/ete, pour les rencontres hommes, on va recuperer le classement correspondant
        // pour une femme qui jouerait dans cette categorie

        // Pour les autres types de championnat, aucune adaptation n'est realisee pour le moment

        // Si le membre n'a pas de classement, on va considerer qu'il est non-classe (5 points)

        Integer points = EchelleCorpo.getAllEchellesCorpo().get(0).getPoints();

        if (membre!=null) {

            // On recupere les classements corpo du membre
            List<ClassementCorpo> classementsCorpos = (List<ClassementCorpo>) classementCorpoRepository.findByMembreFk(membre.getId());

            if (!classementsCorpos.isEmpty()){

                // On les trie par date (de la plus ancienne a la plus recente)
                Collections.sort(classementsCorpos, new Comparator<ClassementCorpo>() {
                    @Override
                    public int compare(ClassementCorpo o1, ClassementCorpo o2) {
                        return o1.getDateClassement().compareTo(o2.getDateClassement());
                    }
                });

                // Tant que...
                // Si la date est anterieure au premier classement enregistre, prendre le premier classement
                // Si la date est posterieure au dernier classement enregistre, prendre le dernier classement
                // S'il n'y a pas de classement, considerer NC

                int i = 0;
                points = classementsCorpos.get(0).getPoints();
                while ( (i<classementsCorpos.size()) && (classementsCorpos.get(i).getDateClassement().compareTo(date) <= 0) ) {
                    points = classementsCorpos.get(i).getPoints();
                    i++;
                }
            }
        }

        if (championnatHomme){
            if (Genre.FEMME.equals(membre.getGenre())){
                points = EchelleCorpo.getCorrespondancePointsHommeFemme().get(points);
            }
        }

        return points;
    }

    /**
     * Permet de recuperer les caracteristiques d'un match
     * un joueur (ou la paire dont il fait partie) et son/ses adversaires
     *
     * Pour la difference de points, le calcul est effectue en soustrayant les points du membre (ou de sa paire) des points de  l'adversaire
     *
     * Ainsi, si le chiffre est positif, cela signifie que l'adversaire etait plus fort
     *
     * @param match
     * @return
     */
    private CaracteristiquesMatchDto getCaracteristiquesMatch(Match match, Membre membre){

        CaracteristiquesMatchDto caracteristiquesMatch = new CaracteristiquesMatchDto();
        caracteristiquesMatch.setMatch(new MatchDto(match));

        // On considere que c'est un championnat messieurs pour les championnats hiver/ete
        // afin de prendre en compte la correspondance quand une dame joue avec les messieurs
        // Pour l'instant, le criterium et coupe d'hiver ne font pas l'objet de cette prise en compte
        boolean isChampionnatHomme = CategorieChampionnat.MESSIEURS.equals(match.getRencontre().getDivision().getChampionnat().getCategorie());

        // Determiner s'il s'agit d'un simple ou d'un double

        if (TypeMatch.SIMPLE.equals(match.getType())){
            caracteristiquesMatch.setJoueur(new MembreLight(membre));

            Integer pointsMembre = getPointsCorpoByMembreAndDate(membre,match.getRencontre().getDateHeureRencontre(),isChampionnatHomme);
            caracteristiquesMatch.setPointsJoueur(pointsMembre);

            Membre adversaire = null;
            if (membre.equals(match.getJoueurVisites1())){
                adversaire = match.getJoueurVisiteurs1();
            }else{
                adversaire = match.getJoueurVisites1();
            }
            caracteristiquesMatch.setAdversaire(new MembreLight(adversaire));
            Integer pointsAdversaire = getPointsCorpoByMembreAndDate(adversaire,match.getRencontre().getDateHeureRencontre(),isChampionnatHomme);
            caracteristiquesMatch.setPointsAdversaire(pointsAdversaire);
            caracteristiquesMatch.setDifferencePoints(pointsAdversaire - pointsMembre);

        }else{

            caracteristiquesMatch.setJoueur(new MembreLight(membre));

            Integer pointsMembre = getPointsCorpoByMembreAndDate(membre,match.getRencontre().getDateHeureRencontre(),isChampionnatHomme);
            caracteristiquesMatch.setPointsJoueur(pointsMembre);

            Membre partenaire = null;
            Membre adversaire1 = null;
            Membre adversaire2 = null;

            if (membre.equals(match.getJoueurVisites1())){
                partenaire = match.getJoueurVisites2();
                adversaire1 = match.getJoueurVisiteurs1();
                adversaire2 = match.getJoueurVisiteurs2();
            }else if (membre.equals(match.getJoueurVisites2())){
                partenaire = match.getJoueurVisites1();
                adversaire1 = match.getJoueurVisiteurs1();
                adversaire2 = match.getJoueurVisiteurs2();
            }else if (membre.equals(match.getJoueurVisiteurs1())){
                partenaire = match.getJoueurVisiteurs2();
                adversaire1 = match.getJoueurVisites1();
                adversaire2 = match.getJoueurVisites2();
            }else{
                partenaire = match.getJoueurVisiteurs1();
                adversaire1 = match.getJoueurVisites1();
                adversaire2 = match.getJoueurVisites2();
            }

            caracteristiquesMatch.setPartenaire(new MembreLight(partenaire));
            caracteristiquesMatch.setAdversaire(new MembreLight(adversaire1));
            caracteristiquesMatch.setPartenaireAdversaire(new MembreLight(adversaire2));

            Integer pointsPartenaire = getPointsCorpoByMembreAndDate(partenaire,match.getRencontre().getDateHeureRencontre(),isChampionnatHomme);
            caracteristiquesMatch.setPointsPartenaire(pointsPartenaire);
            Integer pointsAdversaire1 = getPointsCorpoByMembreAndDate(adversaire1,match.getRencontre().getDateHeureRencontre(),isChampionnatHomme);
            caracteristiquesMatch.setPointsAdversaire(pointsAdversaire1);
            Integer pointsAdversaire2 = getPointsCorpoByMembreAndDate(adversaire2,match.getRencontre().getDateHeureRencontre(),isChampionnatHomme);
            caracteristiquesMatch.setPointsPartenaireAdversaire(pointsAdversaire2);

            caracteristiquesMatch.setDifferencePoints(getPointsAssimilesSimples(pointsAdversaire1 + pointsAdversaire2) - getPointsAssimilesSimples(pointsMembre + pointsPartenaire));

        }

        Integer differencePoints = caracteristiquesMatch.getDifferencePoints();

        //Si la difference de points est positive, cela signifie que l'adversaire etait mieux classe

        // On recupere le resultat du match

        ResultatMatch resultatMatch = getResultatMatch(match, membre);
        caracteristiquesMatch.setResultatMatch(resultatMatch);

        // Limite de difference de points a +/- 15 points
        Integer differencePointsConsiderable = Math.max(-15,Math.min(15,differencePoints));

        // Utilisation de la table de correspondance
        Integer pointsGagnesOuPerdus = correspondancePoints.get(match.getRencontre().getDivision().getChampionnat().getType()).get(resultatMatch).get(differencePointsConsiderable);
        caracteristiquesMatch.setPointsGagnesOuPerdus(pointsGagnesOuPerdus);

        return caracteristiquesMatch;

    }
    /**
     * Permet de recuperer les points qui seront utilises pour une rencontre double
     * Cela fait revenir les points de la paire a des points relatifs a une rencontre simple
     * @param pointsPaireDouble
     * @return
     */
    private Integer getPointsAssimilesSimples(Integer pointsPaireDouble){
        // On somme et on divise par deux en arrondissant au 5 superieur
        return new Double((Math.ceil(pointsPaireDouble/10.0) /2) * 10).intValue();
    }

    /**
     * Initialisation de la table de correspondance pour le calcul des classements Corpo
     */
    private void initCorrespondancePoints(){

        correspondancePoints = new HashMap<>();

        // ETE
        Map<ResultatMatch,Map<Integer,Integer>> eteMap = new HashMap<>();
        Map<Integer,Integer> victoireEteMap = new HashMap<>();Map<Integer,Integer> eteDefaiteMap = new HashMap<>();Map<Integer,Integer> eteMatchNulMap = new HashMap<>();
        victoireEteMap.put(-15,0);eteDefaiteMap.put(-15,-50);eteMatchNulMap.put(-15,-25);
        victoireEteMap.put(-10,5);eteDefaiteMap.put(-10,-50);eteMatchNulMap.put(-10,-25);
        victoireEteMap.put(-5,10);eteDefaiteMap.put(-5,-25);eteMatchNulMap.put(-5,-25);
        victoireEteMap.put(0,50);eteDefaiteMap.put(0,-10);eteMatchNulMap.put(0,0);
        victoireEteMap.put(5,75);eteDefaiteMap.put(5,0);eteMatchNulMap.put(5,35);
        victoireEteMap.put(10,100);eteDefaiteMap.put(10,0);eteMatchNulMap.put(10,50);
        victoireEteMap.put(15,125);eteDefaiteMap.put(15,0);eteMatchNulMap.put(15,60);
        eteMap.put(ResultatMatch.victoire,victoireEteMap);eteMap.put(ResultatMatch.defaite,eteDefaiteMap);eteMap.put(ResultatMatch.matchNul,eteMatchNulMap);

        // HIVER
        Map<ResultatMatch,Map<Integer,Integer>> hiverMap = new HashMap<>();
        Map<Integer,Integer> hiverVictoireMap = new HashMap<>();Map<Integer,Integer> hiverDefaiteMap = new HashMap<>();Map<Integer,Integer> hiverMatchNulMap = new HashMap<>();
        hiverVictoireMap.put(-15,0);hiverDefaiteMap.put(-15,-50);hiverMatchNulMap.put(-15,-25);
        hiverVictoireMap.put(-10,5);hiverDefaiteMap.put(-10,-50);hiverMatchNulMap.put(-10,-25);
        hiverVictoireMap.put(-5,10);hiverDefaiteMap.put(-5,-25);hiverMatchNulMap.put(-5,-25);
        hiverVictoireMap.put(0,50);hiverDefaiteMap.put(0,-10);hiverMatchNulMap.put(0,0);
        hiverVictoireMap.put(5,50);hiverDefaiteMap.put(5,0);hiverMatchNulMap.put(5,35);
        hiverVictoireMap.put(10,100);hiverDefaiteMap.put(10,0);hiverMatchNulMap.put(10,50);
        hiverVictoireMap.put(15,125);hiverDefaiteMap.put(15,0);hiverMatchNulMap.put(15,60);
        hiverMap.put(ResultatMatch.victoire,hiverVictoireMap);hiverMap.put(ResultatMatch.defaite,hiverDefaiteMap);hiverMap.put(ResultatMatch.matchNul,hiverMatchNulMap);

        // CRITERIUM
        Map<ResultatMatch,Map<Integer,Integer>> criteriumMap = new HashMap<>();
        Map<Integer,Integer> criteriumVictoireMap = new HashMap<>();Map<Integer,Integer> criteriumDefaiteMap = new HashMap<>();Map<Integer,Integer> criteriumMatchNulMap = new HashMap<>();
        criteriumVictoireMap.put(-15,0);criteriumDefaiteMap.put(-15,-50);criteriumMatchNulMap.put(-15,-25);
        criteriumVictoireMap.put(-10,5);criteriumDefaiteMap.put(-10,-50);criteriumMatchNulMap.put(-10,-25);
        criteriumVictoireMap.put(-5,10);criteriumDefaiteMap.put(-5,-25);criteriumMatchNulMap.put(-5,-25);
        criteriumVictoireMap.put(0,50);criteriumDefaiteMap.put(0,-10);criteriumMatchNulMap.put(0,0);
        criteriumVictoireMap.put(5,50);criteriumDefaiteMap.put(5,0);criteriumMatchNulMap.put(5,35);
        criteriumVictoireMap.put(10,100);criteriumDefaiteMap.put(10,0);criteriumMatchNulMap.put(10,50);
        criteriumVictoireMap.put(15,125);criteriumDefaiteMap.put(15,0);criteriumMatchNulMap.put(15,60);
        criteriumMap.put(ResultatMatch.victoire,criteriumVictoireMap);criteriumMap.put(ResultatMatch.defaite,criteriumDefaiteMap);criteriumMap.put(ResultatMatch.matchNul,criteriumMatchNulMap);


        // coupeHiver
        Map<ResultatMatch,Map<Integer,Integer>> coupeHiverMap = new HashMap<>();
        Map<Integer,Integer> coupeHiverVictoireMap = new HashMap<>();Map<Integer,Integer> coupeHiverDefaiteMap = new HashMap<>();Map<Integer,Integer> coupeHiverMatchNulMap = new HashMap<>();
        coupeHiverVictoireMap.put(-15,0);coupeHiverDefaiteMap.put(-15,-50);coupeHiverMatchNulMap.put(-15,-25);
        coupeHiverVictoireMap.put(-10,5);coupeHiverDefaiteMap.put(-10,-50);coupeHiverMatchNulMap.put(-10,-25);
        coupeHiverVictoireMap.put(-5,10);coupeHiverDefaiteMap.put(-5,-25);coupeHiverMatchNulMap.put(-5,-25);
        coupeHiverVictoireMap.put(0,50);coupeHiverDefaiteMap.put(0,-10);coupeHiverMatchNulMap.put(0,0);
        coupeHiverVictoireMap.put(5,50);coupeHiverDefaiteMap.put(5,0);coupeHiverMatchNulMap.put(5,35);
        coupeHiverVictoireMap.put(10,100);coupeHiverDefaiteMap.put(10,0);coupeHiverMatchNulMap.put(10,50);
        coupeHiverVictoireMap.put(15,125);coupeHiverDefaiteMap.put(15,0);coupeHiverMatchNulMap.put(15,60);
        coupeHiverMap.put(ResultatMatch.victoire,coupeHiverVictoireMap);coupeHiverMap.put(ResultatMatch.defaite,coupeHiverDefaiteMap);coupeHiverMap.put(ResultatMatch.matchNul,coupeHiverMatchNulMap);

        correspondancePoints.put(TypeChampionnat.ETE,eteMap);
        correspondancePoints.put(TypeChampionnat.HIVER,hiverMap);
        correspondancePoints.put(TypeChampionnat.CRITERIUM,criteriumMap);
        correspondancePoints.put(TypeChampionnat.COUPE_HIVER,coupeHiverMap);

    }

    private Integer getPointsClassement(Integer pointsMatchsObtenus){
        if (pointsMatchsObtenus<-600){
            return -20;
        } else if (pointsMatchsObtenus < -400){
            return -15;
        }else if (pointsMatchsObtenus < -200){
            return -10;
        }else if (pointsMatchsObtenus < -20){
            return -5;
        }else if (pointsMatchsObtenus < 350){
            return 0;
        }else if (pointsMatchsObtenus < 501){
            return 5;
        }else if (pointsMatchsObtenus < 751){
            return 10;
        }else{
            return 15;
        }

    }

/*


TABLE DE CORRESPONDANCE POUR LE CALCUL DES CLASSEMENTS
======================================================


Points obtenus durant l'année (fourchette min)	Points obtenus durant l'année (fourchette max)	Nombre de classements gagnés (ou perdus)  (* 5 points)
-9 999	-601	-4
-600	-401	-3
-400	-201	-2
-200	-21	    -1
-20	    349	    0
350	    500	    1
501	    750	    2
751	    9 999	3


Différence de points par rapport à l'adversaire	Type de championnat	Nombre de points gagnés en cas de victoire	Nombre de points perdus en cas de défaite	Nombre de points gagnés ou perdus en cas de nul
15	Hiver   	125	0	60
15	Criterium	125	0	60
15	Eté	        125	0	60
10	Hiver	    100	0	50
10	Criterium	100	0	50
10	Eté	        100	0	50
5	Hiver	    50	0	35
5	Criterium	50	0	35
5	Eté	        75	0	35
0	Hiver   	50	-10	0
0	Criterium	50	-10	0
0	Eté	        50	-10	0
-5	Hiver	    10	-25	-15
-5	Criterium	10	-25	-15
-5	Eté     	10	-25	-15
-10	Hiver   	5	-50	-25
-10	Criterium	5	-50	-25
-10	Eté	        5	-50	-25
-15	Hiver	    0	-50	-25
-15	Criterium	0	-50	-25
-15	Eté	        0	-50	-25

 */




}
