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
    @Query(value = "delete from membre_equipe where membre_equipe.equipe_fk = :equipeFk and membre_equipe.membre_fk = :membreFk", nativeQuery = true)
    void deleteByEquipeFkAndMembreFk(@Param("equipeFk") Long equipeFk, @Param("membreFk") Long membreFk);
}
