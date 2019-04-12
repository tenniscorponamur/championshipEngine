package be.company.fca.repository;

import be.company.fca.model.ClassementJob;
import be.company.fca.model.ClassementJobTrace;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ClassementJobTraceRepository extends CrudRepository<ClassementJobTrace,Long> {

    /**
     * Permet de recuperer les traces d'un job de calcul des classements corpo
     * @param classementJob Job de calcul de classements corpo
     * @return Taces d'un job de calcul de classements corpo
     */
    List<ClassementJobTrace> findByClassementJob(ClassementJob classementJob);

    /**
     * Permet de supprimer les traces d'un job de calcul des classements corpo
     * @param classementJob Job de calcul de classements corpo
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    void deleteByClassementJob(ClassementJob classementJob);
}
