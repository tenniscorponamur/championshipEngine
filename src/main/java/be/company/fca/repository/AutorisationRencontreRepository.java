package be.company.fca.repository;

import be.company.fca.model.AutorisationRencontre;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AutorisationRencontreRepository extends CrudRepository<AutorisationRencontre,Long> {

    /**
     * Permet de recuperer les autorisations d'une rencontre
     * @param rencontreFk Identifiant de la rencontre
     * @return Liste des autorisations pour la rencontre
     */
    List<AutorisationRencontre> findByRencontreFk(Long rencontreFk);

}
