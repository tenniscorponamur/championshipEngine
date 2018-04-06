package be.company.fca.repository;

import be.company.fca.model.Membre;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MembreRepository extends PagingAndSortingRepository<Membre,Long> {

    /**
     * Permet de recuperer un membre sur base de son numero
     * @param numero Numero de membre
     * @return Membre correspondant au numero
     */
    Membre findByNumero(String numero);

}
