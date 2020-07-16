package be.company.fca.controller;

import be.company.fca.model.*;
import be.company.fca.repository.MembreRepository;
import be.company.fca.repository.TacheRepository;
import be.company.fca.repository.UserRepository;
import be.company.fca.service.UserService;
import be.company.fca.utils.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class TacheController {

    @Autowired
    private TacheRepository tacheRepository;

    @Autowired
    private MembreRepository membreRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @PreAuthorize("hasAuthority('RESPONSABLE_CLUB')")
    @RequestMapping(path = "/private/tache/nouveauMembre", method = RequestMethod.POST)
    public boolean tacheNouveauMembre(Authentication authentication,
                                      @RequestBody Membre membre,
                                      @RequestParam(required = false) String codeClassementAft,
                                      @RequestParam(required = false) Integer pointsCorpo,
                                      @RequestParam(required = false) String commentairesDemande) {

        Membre membreConnecte = userService.getMembreFromAuthentication(authentication);
        if (membreConnecte!=null){
            Tache tache = new Tache();
            tache.setTypeTache(TypeTache.NOUVEAU_MEMBRE);
            tache.setDateDemande(new Date());
            tache.setDemandeur(membreConnecte);
            tache.setCommentairesDemande(commentairesDemande);

            // Verifier adhesion politique
            if (!membre.isAdhesionPolitique()){
                return false;
            }

            // Verifier numero AFT non connu
            if (membre.getNumeroAft()!=null){
                Membre membreConnuAft = membreRepository.findByNumeroAft(membre.getNumeroAft());
                if (membreConnuAft!=null){
                    return false;
                }
            }

            // Verifier appartenance club du responsable
            if (membreConnecte.getClub() != null && membre.getClub() != null && membreConnecte.getClub().equals(membre.getClub())){
                membre.setPassword(PasswordUtils.DEFAULT_MEMBER_PASSWORD);
                membre.setActif(false);
                membre.setFictif(true);
                membre = membreRepository.save(membre);
                tache.setMembre(membre);
                tache.setCodeClassementAft(codeClassementAft);
                tache.setPointsCorpo(pointsCorpo);

                tacheRepository.save(tache);

                return true;
            }

        }
        return false;
    }


    @PreAuthorize("hasAuthority('RESPONSABLE_CLUB')")
    @RequestMapping(method = RequestMethod.GET, path = "/private/taches")
    public List<Tache> getTaches(Authentication authentication) throws ParseException {
        List<Tache> tasks = new ArrayList<>();
        Membre membreConnecte = userService.getMembreFromAuthentication(authentication);
        if (membreConnecte != null){
            return tacheRepository.findByDemandeurAndArchived(membreConnecte, false);
        }
        return tasks;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(method = RequestMethod.GET, path = "/private/taches/ouvertes")
    public List<Tache> getTachesOuvertes(Authentication authentication) throws ParseException {
        return tacheRepository.findByValidationTraitementAndRefusTraitementAndArchived(false, false, false);
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(method = RequestMethod.GET, path = "/private/taches/traitees")
    public List<Tache> getTachesFermees(Authentication authentication) throws ParseException {
        List<Tache> tasks = tacheRepository.findByValidationTraitementAndRefusTraitementAndArchived(true, false, false);
        tasks.addAll(tacheRepository.findByValidationTraitementAndRefusTraitementAndArchived(false, true, false));
        return tasks;
    }
}
