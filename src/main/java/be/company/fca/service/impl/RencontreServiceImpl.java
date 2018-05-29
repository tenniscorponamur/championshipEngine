package be.company.fca.service.impl;

import be.company.fca.model.Championnat;
import be.company.fca.model.Division;
import be.company.fca.model.Rencontre;
import be.company.fca.repository.DivisionRepository;
import be.company.fca.repository.RencontreRepository;
import be.company.fca.service.RencontreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class RencontreServiceImpl implements RencontreService{

    @Autowired
    private DivisionRepository divisionRepository;

    @Autowired
    private RencontreRepository rencontreRepository;

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
    public void deleteByChampionnat(Long championnatId) {

        Championnat championnat = new Championnat();
        championnat.setId(championnatId);

        Iterable<Division> divisionList = divisionRepository.findByChampionnat(championnat);
        for (Division division : divisionList){
            rencontreRepository.deleteByDivision(division);
        }
    }
}
