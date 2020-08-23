package be.company.fca.service.impl;

import be.company.fca.model.Membre;
import be.company.fca.model.Trace;
import be.company.fca.model.User;
import be.company.fca.repository.TraceRepository;
import be.company.fca.repository.UserRepository;
import be.company.fca.service.TraceService;
import be.company.fca.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional(readOnly = true)
public class TraceServiceImpl implements TraceService {

    @Autowired
    TraceRepository traceRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = false)
    public Trace addTrace(Authentication authentication, String type, String foreignKey, String message) {
        Trace trace = new Trace();
        trace.setDateHeure(new Date());
        trace.setType(type);
        trace.setForeignKey(foreignKey);
        trace.setUtilisateur(getUsername(authentication));
        trace.setMessage(message);
        return traceRepository.save(trace);
    }

    private String getUsername(Authentication authentication){
        // Ajout d'un utilisateur admin qui est present independamment de la table users de la DB
        if ("admin".equals(authentication.getName().toLowerCase())){
            return "admin";
        }
        if (userService.isAdmin(authentication)){
            User user = userRepository.findByUsername(authentication.getName().toLowerCase());
            return user.getPrenom() + " " + user.getNom();
        }else{
            Membre membreConnecte = userService.getMembreFromAuthentication(authentication);
            if (membreConnecte!=null){
                return membreConnecte.getPrenom() + " " + membreConnecte.getNom();
            }
        }
        return "UNKNOWN";
    }
}
