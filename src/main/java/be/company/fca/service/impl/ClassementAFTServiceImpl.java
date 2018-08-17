package be.company.fca.service.impl;

import be.company.fca.model.ClassementAFT;
import be.company.fca.model.ClassementCorpo;
import be.company.fca.repository.ClassementAFTRepository;
import be.company.fca.repository.ClassementCorpoRepository;
import be.company.fca.repository.MembreRepository;
import be.company.fca.service.ClassementAFTService;
import be.company.fca.service.ClassementCorpoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class ClassementAFTServiceImpl implements ClassementAFTService {

    @Autowired
    private MembreRepository membreRepository;

    @Autowired
    private ClassementAFTRepository classementAFTRepository;

    @Override
    @Transactional(readOnly = false)
    public ClassementAFT saveClassementsAFT(Long membreId, List<ClassementAFT> classementAFTList) {

        // delete all classement for membre
        classementAFTRepository.deleteByMembreFk(membreId);

        //order classements by date

        Collections.sort(classementAFTList, new Comparator<ClassementAFT>() {
            @Override
            public int compare(ClassementAFT classementAFT1, ClassementAFT classementAFT2) {
                return classementAFT1.getDateClassement().compareTo(classementAFT2.getDateClassement());
            }
        });

        ClassementAFT classementAFTActuel = null;
        if (classementAFTList.size()>0){
            classementAFTActuel = classementAFTList.get(classementAFTList.size()-1);
        }

        // save classements
        for (ClassementAFT classementAFT : classementAFTList){
            classementAFT.setId(null);
            classementAFT.setMembreFk(membreId);
            classementAFTRepository.save(classementAFTActuel);
        }

        return classementAFTActuel;
    }
}
