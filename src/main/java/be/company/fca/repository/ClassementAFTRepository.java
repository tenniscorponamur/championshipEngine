package be.company.fca.repository;

import be.company.fca.model.ClassementAFT;
import org.springframework.data.repository.CrudRepository;

public interface ClassementAFTRepository  extends CrudRepository<ClassementAFT, Long> {

    /**
     * Permet de recuperer les classements AFT d'un membre
     * @param membreFk Identifiant du membre
     * @return Classements AFT du membre
     */
    Iterable<ClassementAFT> findByMembreFk(Long membreFk);
}
