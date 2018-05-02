package be.company.fca.repository;

import be.company.fca.model.Championnat;
import be.company.fca.model.Division;
import be.company.fca.model.Equipe;
import be.company.fca.model.Poule;
import org.springframework.data.repository.CrudRepository;

public interface EquipeRepository extends CrudRepository<Equipe,Long>{

    /**
     * Permet de recuperer les equipes d'une division
     * @param division Division
     * @return Equipes d'une division
     */
    Iterable<Equipe> findByDivision(Division division);

    /**
     * Permet de recuperer les equipes d'une poule
     * @param poule Poule
     * @return Equipes d'une poule
     */
    Iterable<Equipe> findByPoule(Poule poule);

}
