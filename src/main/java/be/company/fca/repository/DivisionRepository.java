package be.company.fca.repository;

import be.company.fca.model.Club;
import be.company.fca.model.Division;
import org.springframework.data.repository.CrudRepository;

public interface DivisionRepository extends CrudRepository<Division, Long> {
}
