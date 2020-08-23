package be.company.fca.utils;

import be.company.fca.model.Membre;
import be.company.fca.model.Role;
import be.company.fca.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.List;

public class UserUtils {

    public static User getAdminUser(){
        User user = new User();
        user.setUsername("admin");
        user.setPassword(PasswordUtils.DEFAULT_ADMIN_PASSWORD);
        user.setPrenom("Ad");
        user.setNom("Min");
        user.setAdmin(true);
        return user;
    }

    /**
     * Permet de construire un utilisateur sur base d'un membre
     * @param membre
     * @return
     */
    public static User getUserFromMembre(Membre membre){
        if (membre.isActif() && !membre.isFictif()){
            User user = new User();
            user.setUsername(membre.getNumeroAft());
            user.setPassword(membre.getPassword());
            user.setPrenom(membre.getPrenom());
            user.setNom(membre.getNom());
            user.setMembre(membre);
            return user;
        }
        return null;
    }

    /**
     * Roles attribues par defaut a un utilisateur
     * @return Liste des roles attribues par defaut a un utilisateur
     */
    public static List<String> getRoles(User user){
        List<String> roles = new ArrayList<String>();
        if (user.isAdmin()){
            roles.add(Role.ADMIN_USER.toString());
        }
        if (user.getMembre()!=null){
            if (user.getMembre().isCapitaine()){
                roles.add(Role.CAPITAINE.toString());
            }
            if (user.getMembre().isResponsableClub()){
                roles.add(Role.RESPONSABLE_CLUB.toString());
            }
        }
        return roles;
    }

}
