package be.company.fca.repository;

import be.company.fca.model.ClassementJob;
import be.company.fca.model.ClassementJobStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ClassementJobRepository extends CrudRepository<ClassementJob,Long> {

    /**
     * Permet de recuperer les jobs d'un certain statut
     * @param status Statut d'un job de calcul de classements corpo
     * @return Jobs de calcul de classements corpo d'un certain statut
     */
    List<ClassementJob> findByStatus(ClassementJobStatus status);
}
