package be.company.fca.utils;

import be.company.fca.model.Membre;
import be.company.fca.model.Role;
import be.company.fca.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.List;

public class UserUtils {

    /**
     * Permet de construire un utilisateur sur base d'un membre
     * @param membre
     * @return
     */
    public static User getUserFromMembre(Membre membre){
        if (membre.isActif()){
            User user = new User();
            user.setUsername(membre.getNumeroAft());
            user.setPassword(PasswordUtils.DEFAULT_PASSWORD);
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

    /**
     * Permet de determiner si un utilisateur connecte peut acceder aux informations privees des membres
     * @param authentication Utilisateur connecte
     * @return true si l'utilisateur connecte peut acceder aux informations privees des membres
     */
    public static boolean isPrivateInformationsAuthorized(Authentication authentication){
        if (authentication!=null){
            List<String> roles = new ArrayList<>();
            for (GrantedAuthority grantedAuthority : authentication.getAuthorities()){
                roles.add(grantedAuthority.getAuthority());
            }
            return roles.contains(Role.ADMIN_USER.toString());
        }
        return false;
    }
}
