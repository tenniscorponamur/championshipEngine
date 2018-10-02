package be.company.fca.service.impl;

import be.company.fca.model.ClassementCorpo;
import be.company.fca.repository.ClassementCorpoRepository;
import be.company.fca.repository.MembreRepository;
import be.company.fca.service.ClassementCorpoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClassementCorpoServiceImpl implements ClassementCorpoService {

    @Autowired
    private MembreRepository membreRepository;

    @Autowired
    private ClassementCorpoRepository classementCorpoRepository;

    @Override
    @Transactional(readOnly = false)
    public ClassementCorpo saveClassementsCorpo(Long membreId, List<ClassementCorpo> classementCorpoList) {

        // delete all classement for membre
        classementCorpoRepository.deleteByMembreFk(membreId);

        //order classements by date

        Collections.sort(classementCorpoList, new Comparator<ClassementCorpo>() {
            @Override
            public int compare(ClassementCorpo classementCorpo1, ClassementCorpo classementCorpo2) {
                return classementCorpo1.getDateClassement().compareTo(classementCorpo2.getDateClassement());
            }
        });

        ClassementCorpo classementCorpoActuel = null;
        if (classementCorpoList.size()>0){
            classementCorpoActuel = classementCorpoList.get(classementCorpoList.size()-1);
        }

        // save classements
        for (ClassementCorpo classementCorpo : classementCorpoList){
            classementCorpo.setId(null);
            classementCorpo.setMembreFk(membreId);
            classementCorpoRepository.save(classementCorpo);
        }

        return classementCorpoActuel;
    }

}
