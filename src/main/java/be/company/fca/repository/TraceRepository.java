package be.company.fca.repository;

import be.company.fca.model.Trace;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TraceRepository extends CrudRepository<Trace, Long> {

    /**
     * Permet de recuperer les traces pour un type et une foreignKey en triant par date descendante
     * @param type
     * @param foreignKey
     * @return
     */
    List<Trace> findByTypeAndForeignKeyOrderByDateHeureDesc(String type, String foreignKey);
}
