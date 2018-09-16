package be.company.fca.controller;

import be.company.fca.model.*;
import be.company.fca.repository.ClassementCorpoRepository;
import be.company.fca.repository.MembreRepository;
import be.company.fca.service.ClassementCorpoService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des classements corpo des membres")
public class ClassementCorpoController {

    @Autowired
    private MembreRepository membreRepository;

    @Autowired
    private ClassementCorpoRepository classementCorpoRepository;

    @Autowired
    private ClassementCorpoService classementCorpoService;

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
     * Recupere le classement Corpo
     * @param membre
     * @param startDate
     * @param endDate
     * @return
     */
    @RequestMapping(value = "/public/membre/simulationClassement", method = RequestMethod.GET)
    public ClassementCorpo simulationClassement(Membre membre, Date startDate, Date endDate){


        //TODO : recup parametres pour test

        membre = membreRepository.findByNumeroAft("6065450");

        GregorianCalendar gc = new GregorianCalendar();
        gc.add(Calendar.YEAR,-2);
        startDate = gc.getTime();
        endDate = new Date();


        // On enregistre le classement de depart
        Integer startPoints = getPointsCorpoByMembreAndDate(membre,startDate);

        // TODO :Recuperer l'ensemble des matchs joues par le membre entre les deux dates

        List<Match> matchs = new ArrayList<>();

        // On ne calcule un nouveau classement qu'a partir de 3 matchs joues

        if (matchs.size()>=3){

            for (Match match : matchs){

                Integer differencePoints = getDifferencePoints(match,membre);

                //Si la difference de points est positive, cela signifie que l'adversaire etait mieux classe

                // On recupere le resultat du match

                ResultatMatch resultatMatch = getResultatMatch(match, membre);

                if (ResultatMatch.victoire.equals(resultatMatch)){

                    // TODO : utilisation table

                }else if (ResultatMatch.defaite.equals(resultatMatch)){

                }else if (ResultatMatch.matchNul.equals(resultatMatch)){

                }

                // TODO : faire la somme et utiliser a la fin la table de conversion des points en "points corpo"

            }

        }
        ClassementCorpo classementCorpo = new ClassementCorpo();
        classementCorpo.setDateClassement(endDate);
        classementCorpo.setMembreFk(membre.getId());

        //TODO : calculer les points du classement resultat en se basant sur les points de depart

        classementCorpo.setPoints(10);
        return classementCorpo;

    }

    private enum ResultatMatch {
        victoire,
        defaite,
        matchNul
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
    private Integer getPointsCorpoByMembreAndDate(Membre membre, Date date){

        // On recupere les classements corpo du membre
        List<ClassementCorpo> classementsCorpos = (List<ClassementCorpo>) classementCorpoRepository.findByMembreFk(membre.getId());

        // Si le membre n'a pas de classement, on va considerer qu'il est non-classe (5 points)

        Integer points = EchelleCorpo.getAllEchellesCorpo().get(0).getPoints();

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


        return points;
    }

    /**
     * Permet de recuperer la difference de points Corpo entre
     * un joueur (ou la paire dont il fait partie) et son/ses adversaires
     *
     * Le calcul est effectue en soustrayant les points du membre (ou de sa paire) des points de  l'adversaire
     *
     * Ainsi, si le chiffre est positif, cela signifie que l'adversaire etait plus fort
     *
     * @param match
     * @return
     */
    private Integer getDifferencePoints(Match match, Membre membre){

        // Determiner s'il s'agit d'un simple ou d'un double

        if (TypeMatch.SIMPLE.equals(match.getType())){
            Integer pointsMembre = getPointsCorpoByMembreAndDate(membre,match.getRencontre().getDateHeureRencontre());
            Membre adversaire = null;
            if (membre.equals(match.getJoueurVisites1())){
                adversaire = match.getJoueurVisiteurs1();
            }else{
                adversaire = match.getJoueurVisites1();
            }
            Integer pointsAdversaire = getPointsCorpoByMembreAndDate(adversaire,match.getRencontre().getDateHeureRencontre());
            return pointsAdversaire - pointsMembre;
        }else{

            Integer pointsMembre = getPointsCorpoByMembreAndDate(membre,match.getRencontre().getDateHeureRencontre());

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

            Integer pointsPartenaire = getPointsCorpoByMembreAndDate(partenaire,match.getRencontre().getDateHeureRencontre());
            Integer pointsAdversaire1 = getPointsCorpoByMembreAndDate(adversaire1,match.getRencontre().getDateHeureRencontre());
            Integer pointsAdversaire2 = getPointsCorpoByMembreAndDate(adversaire2,match.getRencontre().getDateHeureRencontre());

            return getPointsAssimilesSimples(pointsAdversaire1 + pointsAdversaire2) - getPointsAssimilesSimples(pointsMembre + pointsPartenaire);

        }

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




}
