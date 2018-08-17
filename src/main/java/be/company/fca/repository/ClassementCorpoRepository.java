package be.company.fca.repository;

import be.company.fca.model.Championnat;
import be.company.fca.model.ClassementCorpo;
import be.company.fca.model.Division;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ClassementCorpoRepository extends CrudRepository<ClassementCorpo, Long> {

    /**
     * Permet de recuperer les classements corpo d'un membre
     * @param membreFk Identifiant du membre
     * @return Classements corpo du membre
     */
    Iterable<ClassementCorpo> findByMembreFk(Long membreFk);

    /**
     * Permet de supprimer les classements Corpo d'un membre
     * @param membreFk Identifiant d'un membre
     * @return Anciens classements Corpo du membre
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    Iterable<ClassementCorpo> deleteByMembreFk(Long membreFk);
}
