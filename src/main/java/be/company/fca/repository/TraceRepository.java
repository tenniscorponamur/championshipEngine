package be.company.fca.repository;

import be.company.fca.model.Trace;
import org.springframework.data.repository.CrudRepository;

public interface TraceRepository extends CrudRepository<Trace, Long> {
}
