package be.company.fca.controller;

import be.company.fca.model.ClassementCorpo;
import be.company.fca.model.Match;
import be.company.fca.model.Membre;
import be.company.fca.model.TypeMatch;
import be.company.fca.repository.ClassementCorpoRepository;
import be.company.fca.repository.MembreRepository;
import be.company.fca.service.ClassementCorpoService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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




    private void simulationClassement(Membre membre, Date startDate, Date endDate){

        // On enregistre le classement de depart
        Integer startPoints = getPointsCorpoByMembreAndDate(membre,startDate);

        // TODO :Recuperer l'ensemble des matchs joues par le membre entre les deux dates

        List<Match> matchs = new ArrayList<>();

        // On ne calcule un nouveau classement qu'a partir de 3 matchs joues

        if (matchs.size()>=3){

            for (Match match : matchs){

                Integer differencePoints = getDifferencePoints(match,membre);
//                if (victoire){
//
//                }else if (defaite){
//
//                }else if (matchNul){
//
//                }

                // faire la somme et utiliser a la fin la table de conversion des points en "points corpo"

            }

        }

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
            }else if (membre.equals(match.getJoueurVisites2())){
                partenaire = match.getJoueurVisites1();
            }


            Integer pointsAdversaire1 = getPointsCorpoByMembreAndDate(adversaire1,match.getRencontre().getDateHeureRencontre());

            return pointsAdversaire1 - pointsMembre;

//            Integer pointsPaireMembre = getPointMembre(match,membre);
//            Integer pointsPaireAdverse = getPointMembre(match,membre);


            // On somme et on divise par deux en arrondissant au 5 superieur




        }

    }

    private Integer getPointsCorpoByMembreAndDate(Membre membre, Date date){
        List<ClassementCorpo> classementCorpos;

        // TODO : Analyser les classements corpo disponibles

        // Les trier par date chronologiquement

        // Si la date est anterieure au premier classement enregistre, prendre le premier classement
        // Si la date est posterieure au dernier classement enregistre, prendre le dernier classement
        // S'il n'y a pas de classement, considerer NC

        return 5;
    }

    private Integer getPointsAssimilesSimples(Integer pointsPaireDouble){
        return new Double((Math.ceil(pointsPaireDouble/10.0) /2) * 10).intValue();
    }




}
