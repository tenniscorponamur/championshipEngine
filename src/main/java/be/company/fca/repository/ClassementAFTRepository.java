package be.company.fca.repository;

import be.company.fca.model.ClassementAFT;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ClassementAFTRepository  extends CrudRepository<ClassementAFT, Long> {

    /**
     * Permet de recuperer les classements AFT d'un membre
     * @param membreFk Identifiant du membre
     * @return Classements AFT du membre
     */
    Iterable<ClassementAFT> findByMembreFk(Long membreFk);

    /**
     * Permet de supprimer les classements AFT d'un membre
     * @param membreFk Identifiant d'un membre
     * @return Anciens classements AFT du membre
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    Iterable<ClassementAFT> deleteByMembreFk(Long membreFk);

}
