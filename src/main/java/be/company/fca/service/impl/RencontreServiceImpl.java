package be.company.fca.service.impl;

import be.company.fca.model.Championnat;
import be.company.fca.model.Division;
import be.company.fca.model.Match;
import be.company.fca.model.Rencontre;
import be.company.fca.repository.DivisionRepository;
import be.company.fca.repository.MatchRepository;
import be.company.fca.repository.RencontreRepository;
import be.company.fca.repository.SetRepository;
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
    private MatchRepository matchRepository;

    @Autowired
    private SetRepository setRepository;

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
            matchRepository.deleteByRencontre(oldRencontre);
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
            newRencontre.setPointsVisites(oldRencontre.getPointsVisites());
            newRencontre.setPointsVisiteurs(oldRencontre.getPointsVisiteurs());
            newRencontre.setValide(oldRencontre.isValide());
        }

        newRencontre = rencontreRepository.save(newRencontre);

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
                if (rencontreA.getEquipeVisites().equals(rencontreB.getEquipeVisites())){
                    if (rencontreA.getEquipeVisiteurs().equals(rencontreB.getEquipeVisiteurs())){
                        return true;
                    }
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
