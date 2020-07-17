package be.company.fca.repository;

import be.company.fca.model.Membre;
import be.company.fca.model.Tache;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TacheRepository extends CrudRepository<Tache, Long> {

    List<Tache> findByArchived(boolean archived);

    List<Tache> findByDemandeurAndArchived(Membre demandeur, boolean archived);

}
