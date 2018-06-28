package be.company.fca.repository;

import be.company.fca.model.Club;
import be.company.fca.model.Genre;
import be.company.fca.model.Membre;
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
            " set membre.club =:club, membre.capitaine = :capitaine " +
            " where membre.id =:membreId")
    void updateClubInfos(@Param("membreId") Long membreId,
                         @Param("club") Club club,
                         @Param("capitaine") boolean capitaine);


}
