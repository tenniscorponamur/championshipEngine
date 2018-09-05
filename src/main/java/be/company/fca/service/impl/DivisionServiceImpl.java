package be.company.fca.service.impl;

import be.company.fca.model.Championnat;
import be.company.fca.model.Division;
import be.company.fca.repository.DivisionRepository;
import be.company.fca.service.DivisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class DivisionServiceImpl implements DivisionService {

    @Autowired
    private DivisionRepository divisionRepository;

    @Override
    @Transactional(readOnly = false)
    public List<Division> saveDivisionsInChampionship(Championnat championnat, List<Division> divisionList) {

        for (Division division : divisionList){
            division.setChampionnat(championnat);
            divisionRepository.save(division);
        }

        return divisionList;
    }

}
