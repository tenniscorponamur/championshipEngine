package be.company.fca.utils;

import be.company.fca.model.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.List;

public class UserUtils {

    /**
     * Roles attribues par defaut a un utilisateur
     * @return Liste des roles attribues par defaut a un utilisateur
     */
    public static List<String> getDefaultRoles(){
        List<String> roles = new ArrayList<String>();
        roles.add(Role.ADMIN_USER.toString());
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
