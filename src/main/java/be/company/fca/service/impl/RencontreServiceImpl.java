package be.company.fca.service.impl;

import be.company.fca.model.Rencontre;
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
}
