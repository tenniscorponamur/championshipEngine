package be.company.fca.service.impl;

import be.company.fca.model.Membre;
import be.company.fca.model.Role;
import be.company.fca.model.User;
import be.company.fca.repository.MembreRepository;
import be.company.fca.repository.UserRepository;
import be.company.fca.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    @Autowired
    private MembreRepository membreRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean isPrivateInformationsAuthorized(Authentication authentication) {
        if (authentication!=null){
            List<String> roles = new ArrayList<>();
            for (GrantedAuthority grantedAuthority : authentication.getAuthorities()){
                roles.add(grantedAuthority.getAuthority());
            }
            return roles.contains(Role.ADMIN_USER.toString());
        }
        return false;
    }

    @Override
    public boolean isAdmin(Authentication authentication) {
        if (authentication!=null){
            List<String> roles = new ArrayList<>();
            for (GrantedAuthority grantedAuthority : authentication.getAuthorities()){
                roles.add(grantedAuthority.getAuthority());
            }
            return roles.contains(Role.ADMIN_USER.toString());
        }
        return false;
    }

    @Override
    public Membre getMembreFromAuthentication(Authentication authentication){
        User user = userRepository.findByUsername(authentication.getName());
        if (user!=null){
            return user.getMembre();
        }else{
            Membre membre = membreRepository.findByNumeroAft(authentication.getName());
            if (membre.isActif()){
                return membre;
            }
        }
        return null;
    }




}
