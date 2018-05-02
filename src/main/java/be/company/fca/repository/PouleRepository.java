package be.company.fca.repository;

import be.company.fca.model.Division;
import be.company.fca.model.Equipe;
import be.company.fca.model.Poule;
import org.springframework.data.repository.CrudRepository;

public interface PouleRepository extends CrudRepository<Poule,Long>{

    /**
     * Permet de recuperer les poules d'une division
     * @param division Division
     * @return Poules d'une division
     */
    Iterable<Poule> findByDivision(Division division);
}
