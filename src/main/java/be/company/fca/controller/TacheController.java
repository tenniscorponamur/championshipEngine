package be.company.fca.controller;

import be.company.fca.model.*;
import be.company.fca.repository.MembreRepository;
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

                //TODO : sauver la tâche
                //tacheRepository.save(tache);
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

            User agentTraitant = userRepository.findByUsername("fca");

            Membre kDeBruyne = membreRepository.findById(30L).get();
            Membre vKompany = membreRepository.findById(31L).get();

            Tache task = new Tache();
            task.setDateDemande(new Date());
            task.setTypeTache(TypeTache.DESACTIVATION_MEMBRE);
            task.setDemandeur(kDeBruyne);
            task.setMembre(vKompany);

            task.setDateTraitement(new Date());
            task.setAgentTraitant(agentTraitant);
            task.setValidationTraitement(true);

            tasks.add(task);

            Tache task2 = new Tache();
            task2.setDateDemande(new Date());
            task2.setTypeTache(TypeTache.NOUVEAU_MEMBRE);

            Membre newMembre = new Membre();
            newMembre.setGenre(Genre.HOMME);
            newMembre.setNom("CALAY");
            newMembre.setPrenom("DAMIEN");
            newMembre.setDateNaissance(new SimpleDateFormat("dd/MM/yyyy").parse("22/12/1983"));

            task2.setDemandeur(kDeBruyne);
            task2.setMembre(newMembre);

            task2.setDateTraitement(new Date());
            task2.setAgentTraitant(agentTraitant);
            task2.setRefusTraitement(true);
            task2.setCommentairesRefus("Ce membre était déjà connu du Corpo - Nous avons donc réactivé son affiliation");

            tasks.add(task2);
        }

        return tasks;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(method = RequestMethod.GET, path = "/private/taches/ouvertes")
    public List<Tache> getTachesOuvertes(Authentication authentication) throws ParseException {
        List<Tache> tasks = new ArrayList<>();

        Membre kDeBruyne = membreRepository.findById(30L).get();
        Membre vKompany = membreRepository.findById(31L).get();

        Tache task = new Tache();
        task.setDateDemande(new Date());
        task.setTypeTache(TypeTache.DESACTIVATION_MEMBRE);
        task.setDemandeur(kDeBruyne);
        task.setMembre(vKompany);
        task.setDesactivationMembre(true);
        tasks.add(task);

        Tache task2 = new Tache();
        task2.setDateDemande(new Date());
        task2.setTypeTache(TypeTache.NOUVEAU_MEMBRE);

        Membre newMembre = new Membre();
        newMembre.setGenre(Genre.HOMME);
        newMembre.setNom("CALAY");
        newMembre.setPrenom("DAMIEN");
        newMembre.setDateNaissance(new SimpleDateFormat("dd/MM/yyyy").parse("22/12/1983"));

        task2.setDemandeur(kDeBruyne);
        task2.setMembre(newMembre);
        tasks.add(task2);

        return tasks;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(method = RequestMethod.GET, path = "/private/taches/traitees")
    public List<Tache> getTachesFermees(Authentication authentication) throws ParseException {

        //TODO : ne recuperer que les demandes traites pour son propre compte
        //TODO : l'admin peut acceder a toutes les demandes

        boolean adminConnected = userService.isAdmin(authentication);

        List<Tache> tasks = new ArrayList<>();

        User agentTraitant = userRepository.findByUsername("fca");

        Membre kDeBruyne = membreRepository.findById(30L).get();
        Membre vKompany = membreRepository.findById(31L).get();

        Tache task = new Tache();
        task.setDateDemande(new Date());
        task.setTypeTache(TypeTache.DESACTIVATION_MEMBRE);
        task.setDemandeur(kDeBruyne);
        task.setMembre(vKompany);

        task.setDateTraitement(new Date());
        task.setAgentTraitant(agentTraitant);
        task.setValidationTraitement(true);

        tasks.add(task);

        Tache task2 = new Tache();
        task2.setDateDemande(new Date());
        task2.setTypeTache(TypeTache.NOUVEAU_MEMBRE);

        Membre newMembre = new Membre();
        newMembre.setGenre(Genre.HOMME);
        newMembre.setNom("CALAY");
        newMembre.setPrenom("DAMIEN");
        newMembre.setDateNaissance(new SimpleDateFormat("dd/MM/yyyy").parse("22/12/1983"));

        task2.setDemandeur(kDeBruyne);
        task2.setMembre(newMembre);

        task2.setDateTraitement(new Date());
        task2.setAgentTraitant(agentTraitant);
        task2.setRefusTraitement(true);
        task2.setCommentairesRefus("Ce membre était déjà connu du Corpo - Nous avons donc réactivé son affiliation");

        tasks.add(task2);

        return tasks;
    }
}
