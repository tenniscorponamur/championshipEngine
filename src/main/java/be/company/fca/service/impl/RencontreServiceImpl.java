package be.company.fca.service.impl;

import be.company.fca.model.*;
import be.company.fca.repository.*;
import be.company.fca.service.MatchService;
import be.company.fca.service.RencontreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RencontreServiceImpl implements RencontreService{

    @Autowired
    private DivisionRepository divisionRepository;

    @Autowired
    private RencontreRepository rencontreRepository;

    @Autowired
    private MembreRepository membreRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private SetRepository setRepository;

    @Autowired
    private MatchService matchService;

    @Override
    @Transactional(readOnly = false)
    public List<Rencontre> saveRencontres(List<Rencontre> rencontreList) {

        List<Rencontre> savedRencontres = new ArrayList<>();

        for (Rencontre rencontre : rencontreList){
            savedRencontres.add(rencontreRepository.save(rencontre));
        }

        return savedRencontres;

    }

    @Override
    @Transactional(readOnly = false)
    public List<Rencontre> refreshRencontres(List<Rencontre> oldRencontreList, List<Rencontre> newRencontreList) {
        // On recupere la date, le terrain, les points, le caractere valide et les matchs de l'ancienne rencontre
        // Cela signifie qu'on analyse sur base de equipeA contre equipeB dans une poule et une division donnee

        for (Rencontre rencontre : newRencontreList){
            initAndSaveFromOlds(rencontre, oldRencontreList);
        }

        for (Rencontre oldRencontre : oldRencontreList){
            setRepository.deleteByRencontreId(oldRencontre.getId());
            matchRepository.deleteByRencontreId(oldRencontre.getId());
            rencontreRepository.deleteById(oldRencontre.getId());
        }

        return newRencontreList;
    }

    /**
     *
     * @param newRencontre
     * @param oldRencontreList
     */
    @Transactional(readOnly = false)
    private void initAndSaveFromOlds(Rencontre newRencontre, List<Rencontre> oldRencontreList){
        Rencontre oldRencontre = getMatchingOldRencontre(newRencontre, oldRencontreList);
        // Initialisation de la nouvelle rencontre si une ancienne etait presente
        if (oldRencontre!=null) {
            newRencontre.setDateHeureRencontre(oldRencontre.getDateHeureRencontre());
            newRencontre.setTerrain(oldRencontre.getTerrain());
            newRencontre.setEquipeVisites(oldRencontre.getEquipeVisites());
            newRencontre.setEquipeVisiteurs(oldRencontre.getEquipeVisiteurs());
            newRencontre.setPointsVisites(oldRencontre.getPointsVisites());
            newRencontre.setPointsVisiteurs(oldRencontre.getPointsVisiteurs());
            newRencontre.setValide(oldRencontre.isValide());
        }

        newRencontre.setId(rencontreRepository.save(newRencontre).getId());

        // Si jamais des matchs ont ete crees pour l'ancienne rencontre, on va les recuperer dans la nouvelle
        if (oldRencontre!=null) {
            List<Match> matchs = (List<Match>) matchRepository.findByRencontre(oldRencontre);
            for (Match match : matchs){
                match.setRencontre(newRencontre);
                matchRepository.save(match);
            }
        }

    }

    private Rencontre getMatchingOldRencontre(Rencontre newRencontre, List<Rencontre> oldRencontreList){
        for (Rencontre oldRencontre : oldRencontreList){
            if (isEquivalent(newRencontre,oldRencontre)){
                return oldRencontre;
            }
        }
        return null;
    }

    /**
     * Permet de recuperer ou de creer les matchs d'une rencontre s'ils n'existent pas encore
     * @param rencontre
     * @return
     */
    @Transactional(readOnly = false)
    public List<Match> getOrCreateMatchs(Rencontre rencontre){

        List<Match> matchs = (List<Match>) matchRepository.findByRencontre(rencontre);

        if (matchs.isEmpty()){
            matchs = matchService.createMatchs(rencontre);
        }

        return matchs;
    }

    /**
     * Permet de recuperer ou de creer/remplir les matchs d'une rencontre s'ils n'existent pas encore
     * @param rencontre
     * @return
     */
    @Transactional(readOnly = false)
    public List<Match> getAndFillMatchs(Rencontre rencontre){

        List<Match> matchs = getOrCreateMatchs(rencontre);

        // on va analyser les compositions d'equipe pour precharger les joueurs le cas echeant (sous conditions)
        loadCompositions(rencontre,matchs);

        return matchs;
    }

    /**
     * Permet de charger les joueurs/joueuses definis dans la composition des equipes
     * des matchs de la rencontre concerne
     * @param rencontre Rencontre concernee
     * @param matchs Matchs de la rencontre
     *
     */
    @Transactional(readOnly = false)
    private void loadCompositions(Rencontre rencontre, List<Match> matchs){
        if (!rencontre.isResultatsEncodes()){
            // Dans le cas d'un criterium
            // Si les matchs sont charges mais qu'aucun joueur/joueuse n'a ete precise,
            if (TypeChampionnat.CRITERIUM.equals(rencontre.getDivision().getChampionnat().getType())) {

                boolean joueursVisitesCharges = false;
                boolean joueursVisiteursCharges = false;
                // On verifie d'abord si des joueurs ont ete charges pour
                // chaque equipe dans les matchs concernes
                for (Match match : matchs){
                    if (match.getJoueurVisites1()!=null){
                        joueursVisitesCharges=true;
                    }
                    if (match.getJoueurVisiteurs1()!=null){
                        joueursVisiteursCharges=true;
                    }
                    if (TypeMatch.DOUBLE.equals(match.getType())){
                        if (match.getJoueurVisites2()!=null){
                            joueursVisitesCharges=true;
                        }
                        if (match.getJoueurVisiteurs2()!=null){
                            joueursVisiteursCharges=true;
                        }
                    }
                }
                if (!joueursVisitesCharges){
                    // On va charger les joueurs definis dans la composition
                    List<Membre> membresEquipeVisitee = membreRepository.findMembresByEquipeFk(rencontre.getEquipeVisites().getId());
                    // Comme nous sommes en criterium, un seul match par rencontre et pas de choix de composition pour les doubles
                    // On peut donc prendre directement la composition telle qu'elle est definie
                    // TODO : voir s'il est possible de mettre en place une mecanique si on etend le mecanisme aux autres types de championnat
                    if (membresEquipeVisitee.size()>0){
                        for (Match match : matchs){
                            match.setJoueurVisites1(membresEquipeVisitee.get(0));
                            // enregistrer ce joueur
                            matchRepository.save(match);
                            if (TypeMatch.DOUBLE.equals(match.getType())){
                                if (membresEquipeVisitee.size()>1){
                                    match.setJoueurVisites2(membresEquipeVisitee.get(1));
                                    // enregistrer ce joueur
                                    matchRepository.save(match);
                                }
                            }
                        }
                    }
                }
                if (!joueursVisiteursCharges){
                    // On va charger les joueurs definis dans la composition
                    List<Membre> membresEquipeVisiteur = membreRepository.findMembresByEquipeFk(rencontre.getEquipeVisiteurs().getId());
                    // Comme nous sommes en criterium, un seul match par rencontre et pas de choix de composition pour les doubles
                    // On peut donc prendre directement la composition telle qu'elle est definie
                    // TODO : voir s'il est possible de mettre en place une mecanique si on etend le mecanisme aux autres types de championnat
                    if (membresEquipeVisiteur.size()>0){
                        for (Match match : matchs){
                            match.setJoueurVisiteurs1(membresEquipeVisiteur.get(0));
                            // enregistrer ce joueur
                            matchRepository.save(match);
                            if (TypeMatch.DOUBLE.equals(match.getType())){
                                if (membresEquipeVisiteur.size()>1){
                                    match.setJoueurVisiteurs2(membresEquipeVisiteur.get(1));
                                    // enregistrer ce joueur
                                    matchRepository.save(match);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Permet de comparer deux rencontres sur base de leurs division, poule et equipes respectives
     * @param rencontreA
     * @param rencontreB
     * @return true si les rencontres sont jugees equivalentes
     */
    private boolean isEquivalent(Rencontre rencontreA, Rencontre rencontreB){
        if (rencontreA.getDivision().equals(rencontreB.getDivision())){
            if (  (rencontreA.getPoule()==null&&rencontreB.getPoule()==null)
                || (rencontreA.getPoule()!=null&&rencontreA.getPoule().equals(rencontreB.getPoule()))
               ){
                if ( (rencontreA.getEquipeVisites().equals(rencontreB.getEquipeVisites()) && rencontreA.getEquipeVisiteurs().equals(rencontreB.getEquipeVisiteurs()))
                  || (rencontreA.getEquipeVisiteurs().equals(rencontreB.getEquipeVisites()) && rencontreA.getEquipeVisites().equals(rencontreB.getEquipeVisiteurs()))) {
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteByChampionnat(Long championnatId) {
        setRepository.deleteByChampionnatId(championnatId);
        matchRepository.deleteByChampionnatId(championnatId);
        rencontreRepository.deleteByChampionnatId(championnatId);
    }
}
