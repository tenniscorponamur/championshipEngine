package be.company.fca.service;

import be.company.fca.dto.UserDto;
import be.company.fca.model.Membre;
import be.company.fca.model.Role;
import be.company.fca.model.User;
import be.company.fca.repository.MembreRepository;
import be.company.fca.repository.UserRepository;
import be.company.fca.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EngineUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MembreRepository membreRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = null;

        // Ajout d'un utilisateur admin qui est present independamment de la table users de la DB

        if ("admin".equals(username.toLowerCase())){
            user = UserUtils.getAdminUser();
        }else{
            user = userRepository.findByUsername(username.toLowerCase());

            // Pour l'authentification des membres
            // Si le user n'existe pas, on va regarder dans les membres actifs sur base du numero AFT

            if (user==null){
                Membre membre = membreRepository.findByNumeroAft(username);
                user = UserUtils.getUserFromMembre(membre);
            }
        }

        if (user==null){
            throw new UsernameNotFoundException(String.format("The username %s doesn't exist", username));
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : UserUtils.getRoles(user)){
            authorities.add(new SimpleGrantedAuthority(role));
        }
        user.setAuthorities(authorities);

        return user;
    }
}
