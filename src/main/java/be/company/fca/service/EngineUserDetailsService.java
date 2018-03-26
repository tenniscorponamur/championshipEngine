package be.company.fca.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
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

        UserDetails userDetails = new User(s, new BCryptPasswordEncoder().encode("jwtpass"), authorities);
//        UserDetails userDetails = new User(s, "821f498d827d4edad2ed0960408a98edceb661d9f34287ceda2962417881231a", authorities);

        return userDetails;
    }
}
