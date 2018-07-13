package be.company.fca.repository;

import be.company.fca.model.ClassementCorpo;
import org.springframework.data.repository.CrudRepository;

public interface ClassementCorpoRepository extends CrudRepository<ClassementCorpo, Long> {

    /**
     * Permet de recuperer les classements corpo d'un membre
     * @param membreFk Identifiant du membre
     * @return Classements corpo du membre
     */
    Iterable<ClassementCorpo> findByMembreFk(Long membreFk);
}
