package be.company.fca.service;

import be.company.fca.model.Membre;
import org.springframework.security.core.Authentication;

public interface UserService {

    /**
     * Permet de savoir si l'utilisateur connecte est administrateur
     * @param authentication
     * @return
     */
    public boolean isAdmin(Authentication authentication);

    /**
     * Permet de recuperer le membre associe a un utilisateur connecte
     * @param authentication
     * @return
     */
    public Membre getMembreFromAuthentication(Authentication authentication);
}
