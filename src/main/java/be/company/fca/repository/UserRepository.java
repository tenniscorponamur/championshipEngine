package be.company.fca.repository;

import be.company.fca.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User,Long>{

    /**
     * Permet de recuperer un utilisateur sur base de son nom d'utilisateur
     * @param username Nom d'utilisateur
     * @return Utilisateur correspondant au nom d'utilisateur
     */
    User findByUsername(String username);

}
