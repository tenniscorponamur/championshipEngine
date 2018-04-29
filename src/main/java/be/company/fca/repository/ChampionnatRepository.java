package be.company.fca.repository;

import be.company.fca.model.Championnat;
import be.company.fca.model.Club;
import org.springframework.data.repository.CrudRepository;

public interface ChampionnatRepository extends CrudRepository<Championnat, Long> {
}
