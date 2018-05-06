package be.company.fca.service.impl;

import be.company.fca.model.Equipe;
import be.company.fca.repository.EquipeRepository;
import be.company.fca.service.EquipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public class EquipeServiceImpl implements EquipeService {

    @Autowired
    private EquipeRepository equipeRepository;

    @Override
    @Transactional(readOnly = false)
    public List<Equipe> updateEquipeNames(List<Equipe> equipeList) {

        for (Equipe equipe : equipeList){
            equipeRepository.updateName(equipe.getId(),equipe.getCodeAlphabetique());
        }

        return equipeList;

    }
}
