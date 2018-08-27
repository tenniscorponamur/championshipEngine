package be.company.fca.repository;

import be.company.fca.model.*;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

public interface MembreRepository extends PagingAndSortingRepository<Membre,Long> {

    /**
     * Permet de recuperer un membre sur base de son numero
     * @param numero Numero de membre
     * @return Membre correspondant au numero
     */
    Membre findByNumero(String numero);

    /**
     * Permet de recuperer les membres d'un club
     * @param club Club du membre
     * @return Membres appartenant au club
     */
    Iterable<Membre> findByClub(Club club);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Membre membre " +
            " set membre.genre =:genre, " +
            " membre.prenom =:prenom, " +
            " membre.nom =:nom, " +
            " membre.dateNaissance =:dateNaissance" +
            " where membre.id =:membreId")
    void updateInfosGenerales(@Param("membreId") Long membreId,
                              @Param("genre") Genre genre,
                              @Param("prenom") String prenom,
                              @Param("nom") String nom,
                              @Param("dateNaissance") Date dateNaissance);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Membre membre " +
            " set membre.club =:club, " +
            " membre.capitaine = :capitaine, " +
            " membre.responsableClub = :responsableClub, " +
            " membre.dateAffiliationCorpo = :dateAffiliationCorpo, " +
            " membre.dateDesaffiliationCorpo = :dateDesaffiliationCorpo " +
            " where membre.id =:membreId")
    void updateClubInfos(@Param("membreId") Long membreId,
                         @Param("club") Club club,
                         @Param("capitaine") boolean capitaine,
                         @Param("responsableClub") boolean responsableClub,
                         @Param("dateAffiliationCorpo") Date dateAffiliationCorpo,
                         @Param("dateDesaffiliationCorpo") Date dateDesaffiliationCorpo
                         );

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Membre membre " +
            " set membre.numeroAft =:numeroAft, " +
            " membre.numeroClubAft = :numeroClubAft, " +
            " membre.dateAffiliationAft = :dateAffiliationAft, " +
            " membre.onlyCorpo = :onlyCorpo " +
            " where membre.id =:membreId")
    void updateInfosAft(@Param("membreId") Long membreId,
                         @Param("numeroAft") String numeroAft,
                         @Param("numeroClubAft") String numeroClubAft,
                         @Param("dateAffiliationAft") Date dateAffiliationAft,
                         @Param("onlyCorpo") boolean onlyCorpo);


    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Membre membre " +
            " set membre.classementCorpoActuel =:classementCorpo " +
            " where membre.id =:membreId")
    void updateClassementCorpo( @Param("membreId") Long membreId,
                                @Param("classementCorpo") ClassementCorpo classementCorpo);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Membre membre " +
            " set membre.classementAFTActuel =:classementAFT " +
            " where membre.id =:membreId")
    void updateClassementAFT( @Param("membreId") Long membreId,
                                @Param("classementAFT") ClassementAFT classementAFT);

}
