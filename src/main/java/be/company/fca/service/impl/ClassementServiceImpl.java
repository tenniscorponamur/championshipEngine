package be.company.fca.service.impl;

import be.company.fca.model.*;
import be.company.fca.model.Set;
import be.company.fca.repository.*;
import be.company.fca.service.ClassementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class ClassementServiceImpl implements ClassementService {

    @Autowired
    private ChampionnatRepository championnatRepository;

    @Autowired
    private EquipeRepository equipeRepository;

    @Autowired
    private RencontreRepository rencontreRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private SetRepository setRepository;

    @Autowired
    private DivisionRepository divisionRepository;

    @Autowired
    private PouleRepository pouleRepository;


    /**
     * Extrait du reglement :
     * --------------------
     *
     * Article 8 : Classement des équipes.

     8.1. Le classement final s'établit par ordre décroissant des points obtenus. En cas d'égalité dans les points, l'ordre de classement est déterminé comme suit :
     Si une équipe lauréate a gagné une rencontre par forfait, cette dernière est retirée du championnat parmi toutes les équipes entrant en compétition pour le départage
     a) si 2 équipes à égalité : le résultat de la rencontre entre les 2 équipes.
     b) Si plus de 2 équipes à égalité :seul les résultats entre ces équipes sont pris en considération pour déterminer le classement définitif en observant les critères suivants
     1) le plus grand nombre de points
     2) le plus grand nombre de victoires (en simples et en doubles)
     3) le plus petit nombre de sets perdus.( en simple et en doubles)
     4) le plus petit nombre de jeux perdus (en simple et en doubles)
     Si après ces critères, 2 équipes se trouvent toujours à égalité, le départage se fait suivant la règle du point 8.1.a
     En cas d’égalité totale après tous ces critères, le tirage au sort donnera le vainqueur final.

     8.2. Les équipes premières des différentes séries d'une division se rencontrent pour se départager. La commission sportive fixe la date, l'heure et l'endroit des rencontres inter séries et en détermine les règles particulières.

     8.3. Est déclarée championne de sa division l'équipe qui s'est classée première de sa division ou qui remporte la rencontre inter séries selon l'article 8.2.

     */

    @Override
    public Classement getClassementPoule(Poule poule) {
        Classement classement = initClassement(poule);

        List<Rencontre> rencontresPoule = (List<Rencontre>) rencontreRepository.findByPoule(poule);
        for (Rencontre rencontre : rencontresPoule) {
            if (rencontre.isValide()) {
                attributionPointsEquipe(classement,rencontre);
            }
        }

        triEquipes(classement);

        return classement;
    }

    @Override
    public List<Classement> getClassementsByChampionnat(Long championnatId) {

        Championnat championnat = new Championnat();
        championnat.setId(championnatId);
        List<Division> divisions = (List<Division>) divisionRepository.findByChampionnat(championnat);

        Map<Poule,Classement> classementMap = new HashMap<Poule,Classement>();
        Map<Division,List<Rencontre>> interseriesMap = new HashMap<>();

        for (Division division : divisions){

            List<Rencontre> rencontresInterseries = new ArrayList<>();

            List<Poule> poules = (List<Poule>) pouleRepository.findByDivision(division);

            for (Poule poule : poules) {
                Classement classement = initClassement(poule);
                classementMap.put(poule,classement);
            }

            List<Rencontre> rencontresDivision = (List<Rencontre>) rencontreRepository.findByDivision(division);

            for (Rencontre rencontre : rencontresDivision){

                if (rencontre.isValide()){

                    Poule pouleRencontre = rencontre.getPoule();

                    if (pouleRencontre!=null){
                        Classement classementPoule = classementMap.get(pouleRencontre);
                        attributionPointsEquipe(classementPoule,rencontre);
                    }else{

                        // Il s'agit d'une rencontre interserie
                        rencontresInterseries.add(rencontre);
                    }
                }

            }

            interseriesMap.put(division,rencontresInterseries);

        }

        // Tri des equipes au sein des classements

        List<Classement> classements = new ArrayList<>();
        for (Classement classement : classementMap.values()){
            triEquipes(classement);
            setGagnantInterseries(classement,interseriesMap.get(classement.getPoule().getDivision()));
            classements.add(classement);
        }

        // Trier les classements selon divisions/poules par numero

        Collections.sort(classements, new Comparator<Classement>() {
            @Override
            public int compare(Classement classementA, Classement classementB) {
                int compareDivision = classementA.getPoule().getDivision().getNumero().compareTo(classementB.getPoule().getDivision().getNumero());
                if (compareDivision!=0){
                    return compareDivision;
                }else{
                    int comparePoule = classementA.getPoule().getNumero().compareTo(classementB.getPoule().getNumero());
                    return comparePoule;
                }
            }
        });

        return classements;
    }

    @Override
    public List<ClassementClub> getClassementsClubByChampionnat(Long championnatId) {
        List<ClassementClub> classementClubs = new ArrayList<>();

        // Ce classement n'est construit que pour le criterium
        // Parcourir les equipes et les classements des poules --> attribuer des points aux clubs concernes

        Championnat championnatSelected = championnatRepository.findById(championnatId).get();

        if (TypeChampionnat.CRITERIUM.equals(championnatSelected.getType())) {
            // Boucler sur les championnats CRITERIUM de la saison correspondante

            List<Championnat> championnats = championnatRepository.findByTypeAndAnnee(TypeChampionnat.CRITERIUM, championnatSelected.getAnnee());

            Map<Club, Integer> pointsClub = new HashMap<>();

            for (Championnat championnat : championnats) {

                List<Division> divisions = (List<Division>) divisionRepository.findByChampionnat(championnat);
                for (Division division : divisions) {
                    Map<Club, Integer> nbEquipesClubs = new HashMap<>();
                    List<Poule> poules = (List<Poule>) pouleRepository.findByDivision(division);
                    for (Poule poule : poules) {
                        Classement classement = getClassementPoule(poule);
                        List<ClassementEquipe> classementEquipes = classement.getClassementEquipes();
                        for (int i = 0; i < classementEquipes.size(); i++) {
                            ClassementEquipe classementEquipe = classementEquipes.get(i);
                            Club club = classementEquipe.getEquipe().getClub();

                            // Dans chaque classement, on prend les 2 premieres equipes du club
                            // Pour la premiere, on multiplie par 2 les points obtenus (sets gagnés)
                            // Pour la seconde, on prend directement les points obtenus
                            // Pas de points pour la troisieme equipe

                            // Premiere equipe du club dans ce classement -> setsGagnes * 2
                            // Seconde equipe du club dans ce classement -> setsGagnes * 1
                            // Troisieme et suivante -> setGagnes * 0 = 0

                            Integer nbEquipesClubInClassement = nbEquipesClubs.get(club);
                            if (nbEquipesClubInClassement == null) {
                                nbEquipesClubInClassement = 0;
                            }
                            Integer pointsAttribues = Math.max(2 - nbEquipesClubInClassement, 0) * classementEquipe.getSetsGagnes();
                            nbEquipesClubInClassement = nbEquipesClubInClassement + 1;
                            nbEquipesClubs.put(club, nbEquipesClubInClassement);

                            // Ajout des points pour le club
                            Integer points = pointsClub.get(club);
                            if (points == null) {
                                points = 0;
                            }
                            points += pointsAttribues;
                            pointsClub.put(club, points);
                        }
                    }
                }
            }

            for (Club club : pointsClub.keySet()) {
                ClassementClub classementClub = new ClassementClub();
                classementClub.setClub(club);
                classementClub.setPoints(pointsClub.get(club));
                classementClubs.add(classementClub);
            }

            Collections.sort(classementClubs, new Comparator<ClassementClub>() {
                @Override
                public int compare(ClassementClub o1, ClassementClub o2) {
                    return (-1) * Integer.compare(o1.getPoints(), o2.getPoints());
                }
            });
        }
        return classementClubs;
    }


    /**
     * Permet de creer un classement initial d'une poule
     * (sans calcul de points a cette etape)
     * Il recupere l'ensemble des equipes de la poule pour les definir
     * dans le classement
     * @param poule
     * @return
     */
    private Classement initClassement(Poule poule){
        Classement classement = new Classement();
        classement.setPoule(poule);
        List<Equipe> equipesPoule = (List<Equipe>) equipeRepository.findByPoule(poule);
        for (Equipe equipe : equipesPoule){
            classement.getClassementEquipes().add(new ClassementEquipe(equipe));
        }
        return classement;
    }

    /**
     * Permet d'attribuer les points aux equipes concernees dans un classement sur base d'une rencontre
     * @param classement Classement
     * @param rencontre Rencontre
     */
    private void attributionPointsEquipe(Classement classement,Rencontre rencontre){

        ClassementEquipe classementEquipeVisites = classement.findByEquipe(rencontre.getEquipeVisites());
        ClassementEquipe classementEquipeVisiteurs = classement.findByEquipe(rencontre.getEquipeVisiteurs());

        int pointsVisites = 0;
        int pointsVisiteurs = 0;

        if (rencontre.getPointsVisites()!=null && rencontre.getPointsVisiteurs()!=null){
            if (rencontre.getPointsVisites() > rencontre.getPointsVisiteurs()){
                pointsVisites = 2;
                // On conserve la liste des equipes battues
                classementEquipeVisites.getEquipesBattues().add(rencontre.getEquipeVisiteurs());

            }else if (rencontre.getPointsVisites() < rencontre.getPointsVisiteurs()){
                pointsVisiteurs = 2;
                // On conserve la liste des equipes battues
                classementEquipeVisiteurs.getEquipesBattues().add(rencontre.getEquipeVisites());
            }else{
                pointsVisites = 1;
                pointsVisiteurs = 1;
            }
        }

        // Ajout du match joue

        classementEquipeVisites.ajoutMatchJoue();
        classementEquipeVisiteurs.ajoutMatchJoue();

        // Ajout des points obtenus

        classementEquipeVisites.ajoutPoints(pointsVisites);
        classementEquipeVisiteurs.ajoutPoints(pointsVisiteurs);

        // Ajout des sets gagnes et perdus

        classementEquipeVisites.ajoutSetsGagnes(rencontre.getPointsVisites());
        classementEquipeVisiteurs.ajoutSetsGagnes(rencontre.getPointsVisiteurs());

        classementEquipeVisites.ajoutSetsPerdus(rencontre.getPointsVisiteurs());
        classementEquipeVisiteurs.ajoutSetsPerdus(rencontre.getPointsVisites());

        // Recuperation des jeux gagnes et perdus en analysant les resultats des sets

        List<Match> matchs = (List<Match>) matchRepository.findByRencontre(rencontre);

        for (Match match : matchs){
            List<Set> sets = (List<Set>) setRepository.findByMatch(match);
            for (Set set : sets){
                classementEquipeVisites.ajoutJeuxGagnes(set.getJeuxVisites());
                classementEquipeVisiteurs.ajoutJeuxGagnes(set.getJeuxVisiteurs());

                classementEquipeVisites.ajoutJeuxPerdus(set.getJeuxVisiteurs());
                classementEquipeVisiteurs.ajoutJeuxPerdus(set.getJeuxVisites());
            }

        }

    }

    /**
     * Permet de trier les equipes dans un classement donne
     * selon les regles etablies dans le reglement
     * @param classement
     */
    private void triEquipes(Classement classement){

        // Tri des equipes sur base des points obtenus (et les autres criteres par la suite - rencontre directe - sets gagnes - jeux gagnes

        Collections.sort(classement.getClassementEquipes(), new Comparator<ClassementEquipe>() {
            @Override
            public int compare(ClassementEquipe classementEquipeA, ClassementEquipe classementEquipeB) {

                // Premier critere, les points

                int comparePoints = (-1) * Integer.compare(classementEquipeA.getPoints(),classementEquipeB.getPoints());

                if (comparePoints!=0){
                    return comparePoints;
                }else{

                    // Second critere, les confrontations directes

                    boolean equipeAwinsAgainstEquipeB = classementEquipeA.getEquipesBattues().contains(classementEquipeB.getEquipe());
                    boolean equipeBwinsAgainstEquipeA = classementEquipeB.getEquipesBattues().contains(classementEquipeA.getEquipe());

                    if (equipeAwinsAgainstEquipeB && !equipeBwinsAgainstEquipeA){
                        return -1;
                    } else if (!equipeAwinsAgainstEquipeB && equipeBwinsAgainstEquipeA){
                        return 1;
                    } else {

                        // Troisieme critere : les sets gagnes

                        int compareSetsGagnes = (-1) * Integer.compare(classementEquipeA.getSetsGagnes(),classementEquipeB.getSetsGagnes());

                        if (compareSetsGagnes!=0) {
                            return compareSetsGagnes;
                        }else{

                            // Quatrieme critere : les sets perdus

                            int compareSetsPerdus = Integer.compare(classementEquipeA.getSetsPerdus(),classementEquipeB.getSetsPerdus());

                            if (compareSetsPerdus!=0) {
                                return compareSetsPerdus;
                            }else{

                                // Cinquieme critere : les jeux perdus

                                return Integer.compare(classementEquipeA.getJeuxPerdus(),classementEquipeB.getJeuxPerdus());

                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Permet de preciser le gagnant des rencontres interseries dans un classement
     * Si plusieurs rencontres interseries dans la division ont lieu, elles seront toutes analyses pour ne
     * conserver que le gagnant final
     * @param classement
     * @param rencontreInterseries
     */
    private void setGagnantInterseries(Classement classement, List<Rencontre> rencontreInterseries){

        List<Equipe> equipesVictorieusesDivision = getEquipeVictorieuse(classement.getPoule().getDivision(),rencontreInterseries);
        for (Equipe equipeVictorieuseDivision : equipesVictorieusesDivision){
            if (equipeVictorieuseDivision!=null){
                // Si l'equipe est classe dans cette poule, elle sera marquee comme gagnant la division suite a la rencontre interseries
                ClassementEquipe classementEquipe = classement.findByEquipe(equipeVictorieuseDivision);
                if (classementEquipe!=null){
                    classementEquipe.setGagnantInterseries(true);
                }
            }
        }
    }

    /**
     * Permet de recuperer l'equipe victorieuse d'une liste de rencontre interserie pour une division donnee
     * TODO : gerer le cas de rencontres multiples (actuellement seule une rencontre unique est consideree)
     * @param division
     * @param rencontreInterserieList
     * @return
     */
    private List<Equipe> getEquipeVictorieuse(Division division, List<Rencontre> rencontreInterserieList){

        List<Equipe> equipes = new ArrayList<>();
        // Dans la meme division, si plusieurs rencontres interseries avec la meme equipe, il faut conserver uniquement l'equipe victorieuse de ses deux matchs

        // S'il s'agit d'une division avec petite finale et grande finale, on ne doit conserver que l'equipe qui a joue le plus de rencontres et qui a gagne l'ensemble de ceux-ci
        if (division.isWithFinales()){

            // Il faut analyser les victoires pour determiner le gagnant final
            // Comme toutes les rencontres ne sont potentiellement pas encore disputees, on ne peut pas considerer comme gagnant l'equipe qui a
            // gagne le plus de rencontre interserie sinon meme le match pour la 5eme place consacrera le gagnant...
            // On va tester le fait qu'il s'agit de la finale via un boolean pour ce type de match

            // Analyser la finale uniquement !!

            for (Rencontre interserie : rencontreInterserieList){
                if (interserie.isFinaleInterserie()){
                    equipes.add(getGagnantRencontreInterserie(interserie));
                }
            }

        }else{

            // On considere qu'il n'y a qu'une rencontre interserie par division possible (deux poules maximum) par niveau au classement

            for (Rencontre interserie : rencontreInterserieList){
                equipes.add(getGagnantRencontreInterserie(interserie));
            }

        }

        return equipes;
    }

    /**
     * Permet de recuperer l'equipe gagnante d'une rencontre inter-serie
     * @param rencontre
     * @return
     */
    public Equipe getGagnantRencontreInterserie(Rencontre rencontre){

        if (rencontre.getPointsVisites()!=null && rencontre.getPointsVisiteurs()!=null){

            if (rencontre.getPointsVisites() > rencontre.getPointsVisiteurs()){

                return rencontre.getEquipeVisites();

            }else if (rencontre.getPointsVisites() < rencontre.getPointsVisiteurs()){

                return rencontre.getEquipeVisiteurs();

            }else{

                Integer jeuxGagnesVisites = 0;
                Integer jeuxGagnesVisiteurs = 0;

                List<Match> matchs = (List<Match>) matchRepository.findByRencontre(rencontre);
                for (Match match : matchs){
                    List<Set> sets = (List<Set>) setRepository.findByMatch(match);
                    for (Set set : sets){
                        jeuxGagnesVisites += set.getJeuxVisites();
                        jeuxGagnesVisiteurs += set.getJeuxVisiteurs();
                    }
                }

                if (jeuxGagnesVisites>jeuxGagnesVisiteurs){

                    return rencontre.getEquipeVisites();

                }else if (jeuxGagnesVisites<jeuxGagnesVisiteurs){

                    return rencontre.getEquipeVisiteurs();

                }else{
                    //TODO : ajouter un boolean dans la rencontre pour designer l'equipe gagnante en cas de stricte egalite
                }

            }

        }


        return null;

    }
}
