package be.company.fca.repository;

import be.company.fca.model.MembreEquipe;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface MembreEquipeRepository extends CrudRepository<MembreEquipe,Long> {

    @Transactional
    @Modifying(clearAutomatically = true)
    void deleteByEquipeFkAndMembreFk(Long equipeFk, Long membreFk);

    @Transactional
    @Modifying(clearAutomatically = true)
    void deleteByEquipeFk(Long equipeFk);

    @Transactional
    @Modifying(clearAutomatically = true)
    void deleteByMembreFk(Long membreFk);


}
