package be.company.fca.repository;

import be.company.fca.model.CategorieChampionnat;
import be.company.fca.model.Championnat;
import be.company.fca.model.Club;
import be.company.fca.model.TypeChampionnat;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChampionnatRepository extends CrudRepository<Championnat, Long> {

    /**
     * Permet de recuperer une liste de championnat par type et par annee
     * Retourne les championnat de toutes les categories pour ce type et cette annee
     * @param type
     * @param annee
     * @return
     */
    List<Championnat> findByTypeAndAnnee(TypeChampionnat type, String annee);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Championnat championnat " +
            " set championnat.annee =:annee, " +
            " championnat.type =:type, " +
            " championnat.categorie =:categorie" +
            " where championnat.id =:championnatId")
    void updateInfosGenerales(@Param("championnatId") Long championnatId,
                              @Param("annee") String annee,
                              @Param("type")TypeChampionnat typeChampionnat,
                              @Param("categorie") CategorieChampionnat categorieChampionnat);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Championnat championnat " +
            " set championnat.ordre =:ordre" +
            " where championnat.id =:championnatId")
    void updateOrdre(@Param("championnatId") Long championnatId,
                                     @Param("ordre") Long ordre);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Championnat championnat " +
            " set championnat.calendrierARafraichir =:calendrierARafraichir" +
            " where championnat.id =:championnatId")
    void updateCalendrierARafraichir(@Param("championnatId") Long championnatId,
                                    @Param("calendrierARafraichir") boolean calendrierARafraichir);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Championnat championnat " +
            " set championnat.calendrierValide =:calendrierValide" +
            " where championnat.id =:championnatId")
    void updateCalendrierValide(@Param("championnatId") Long championnatId,
                                     @Param("calendrierValide") boolean calendrierValide);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Championnat championnat " +
            " set championnat.cloture =:cloture" +
            " where championnat.id =:championnatId")
    void updateChampionnatCloture(@Param("championnatId") Long championnatId,
                                     @Param("cloture") boolean cloture);
}
