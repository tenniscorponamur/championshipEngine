package be.company.fca.service;

import be.company.fca.model.User;
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

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

        if (!"fca".equals(s) && !"leopold".equals(s)) {
            throw new UsernameNotFoundException(String.format("The username %s doesn't exist", s));
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ADMIN_USER"));

        // UncryptedPassword : jwtpass

        User user = new User();
        user.setUsername(s);
        user.setPassword(new BCryptPasswordEncoder().encode("jwtpass"));
        user.setAuthorities(authorities);

        user.setPrenom("Fabrice");
        user.setNom("Calay");

        return user;
    }
}
