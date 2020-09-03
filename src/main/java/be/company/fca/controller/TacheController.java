package be.company.fca.controller;

import be.company.fca.model.*;
import be.company.fca.repository.*;
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
    private ClassementCorpoRepository classementCorpoRepository;

    @Autowired
    private ClassementAFTRepository classementAFTRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @PreAuthorize("hasAuthority('RESPONSABLE_CLUB')")
    @RequestMapping(path = "/private/tache/nouveauMembre", method = RequestMethod.POST)
    public boolean tacheNouveauMembre(Authentication authentication,
                                      @RequestBody Membre membre,
                                      @RequestParam(required = false) String numeroAft,
                                      @RequestParam(required = false) String codeClassementAft,
                                      @RequestParam(required = false) Integer pointsCorpo,
                                      @RequestParam(required = false) String commentairesDemande) {

        Membre membreConnecte = userService.getMembreFromAuthentication(authentication);
        if (membreConnecte != null) {
            Tache tache = new Tache();
            tache.setTypeTache(TypeTache.NOUVEAU_MEMBRE);
            tache.setDateDemande(new Date());
            tache.setDemandeur(membreConnecte);
            tache.setCommentairesDemande(commentairesDemande);

            // Verifier adhesion politique
            if (!membre.isAdhesionPolitique()) {
                return false;
            }

            // Verifier numero AFT non connu
            if (numeroAft != null) {
                Membre membreConnuAft = membreRepository.findByNumeroAft(numeroAft);
                if (membreConnuAft != null) {
                    return false;
                }
            }

            // Verifier appartenance club du responsable
            if (membreConnecte.getClub() != null && membre.getClub() != null && membreConnecte.getClub().equals(membre.getClub())) {
                membre.setPassword(PasswordUtils.DEFAULT_MEMBER_PASSWORD);
                membre.setActif(false);
                membre.setFictif(true);
                membre = membreRepository.save(membre);
                tache.setMembre(membre);
                tache.setNumeroAft(numeroAft);
                tache.setCodeClassementAft(codeClassementAft);
                tache.setPointsCorpo(pointsCorpo);

                tacheRepository.save(tache);

                return true;
            }

        }
        return false;
    }

    @PreAuthorize("hasAuthority('RESPONSABLE_CLUB')")
    @RequestMapping(path = "/private/tache/activiteMembre", method = RequestMethod.POST)
    public boolean tacheActiviteMembre(Authentication authentication,
                                      @RequestParam(required = true) Long membreId,
                                      @RequestParam(required = true) boolean activite,
                                      @RequestParam(required = false) Integer pointsCorpo,
                                      @RequestParam(required = false) String commentairesDemande) {

        Membre membreConnecte = userService.getMembreFromAuthentication(authentication);
        if (membreConnecte != null) {
            Tache tache = new Tache();
            if (activite){
                tache.setTypeTache(TypeTache.REACTIVATION_MEMBRE);
                tache.setReactivationMembre(true);
            }else{
                tache.setTypeTache(TypeTache.DESACTIVATION_MEMBRE);
                tache.setDesactivationMembre(true);
            }
            tache.setDateDemande(new Date());
            tache.setDemandeur(membreConnecte);
            tache.setCommentairesDemande(commentairesDemande);

            //On verifie que le membre est bien actif ou inactif selon la demande
            Membre membre = membreRepository.findById(membreId).get();
            if (activite){
                if (membre.isActif()){
                    return false;
                }
            }else{
                if (!membre.isActif()){
                    return false;
                }
            }

            // Verifier appartenance club du responsable
            if (membreConnecte.getClub() != null && membre.getClub() != null && membreConnecte.getClub().equals(membre.getClub())) {

                tache.setMembre(membre);
                tache.setPointsCorpo(pointsCorpo);

                tacheRepository.save(tache);

                return true;
            }

        }
        return false;
    }

    @PreAuthorize("hasAuthority('RESPONSABLE_CLUB')")
    @RequestMapping(path = "/private/tache/pointsMembre", method = RequestMethod.POST)
    public boolean tachePointsMembre(Authentication authentication,
                                       @RequestParam(required = true) Long membreId,
                                       @RequestParam(required = false) Integer pointsCorpo,
                                       @RequestParam(required = false) String commentairesDemande) {

        Membre membreConnecte = userService.getMembreFromAuthentication(authentication);
        if (membreConnecte != null) {
            Tache tache = new Tache();
            tache.setTypeTache(TypeTache.CHANGEMENT_POINTS_CORPO);
            tache.setDateDemande(new Date());
            tache.setDemandeur(membreConnecte);
            tache.setCommentairesDemande(commentairesDemande);

            //On verifie que le membre est bien actif ou inactif selon la demande
            Membre membre = membreRepository.findById(membreId).get();

            // Verifier appartenance club du responsable
            if (membreConnecte.getClub() != null && membre.getClub() != null && membreConnecte.getClub().equals(membre.getClub())) {

                tache.setMembre(membre);
                tache.setPointsCorpo(pointsCorpo);

                tacheRepository.save(tache);

                return true;
            }

        }
        return false;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN_USER','RESPONSABLE_CLUB')")
    @RequestMapping(method = RequestMethod.GET, path = "/private/taches")
    public List<Tache> getTaches(Authentication authentication, @RequestParam(required = false) boolean withArchives) throws ParseException {
        boolean adminConnected = userService.isAdmin(authentication);
        List<Tache> tasks = new ArrayList<>();
        if (adminConnected) {
            tasks = tacheRepository.findByArchived(false);
            if (withArchives) {
                tasks.addAll(tacheRepository.findByArchived(true));
            }
            return tasks;
        } else {
            Membre membreConnecte = userService.getMembreFromAuthentication(authentication);
            if (membreConnecte != null) {
                tasks.addAll(tacheRepository.findByDemandeurAndArchived(membreConnecte, false));
                if (withArchives) {
                    tasks.addAll(tacheRepository.findByDemandeurAndArchived(membreConnecte, true));
                }
            }
        }
        return tasks;
    }

    @PreAuthorize("hasAuthority('RESPONSABLE_CLUB')")
    @RequestMapping(path = "/private/tache/{tacheId}/markAsRead", method = RequestMethod.PUT)
    public boolean markAsRead(Authentication authentication, @PathVariable("tacheId") Long tacheId) {
        Tache tache = tacheRepository.findById(tacheId).get();
        if (tache.isValidationTraitement() || tache.isRefusTraitement()) {
            tache.setMarkAsRead(true);
            tacheRepository.save(tache);
            return true;
        } else {
            return false;
        }
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(path = "/private/tache/{tacheId}/archive", method = RequestMethod.PUT)
    public boolean archive(Authentication authentication, @PathVariable("tacheId") Long tacheId) {
        Tache tache = tacheRepository.findById(tacheId).get();
        // On ne va pas archiver tant que ce n'est pas marque comme lu par le demandeur
        if (tache.isMarkAsRead()){
            tache.setArchived(true);
            tacheRepository.save(tache);
            return true;
        }else{
            return false;
        }
    }


    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(path = "/private/tache/{tacheId}", method = RequestMethod.PUT)
    public Tache traitementTache(Authentication authentication,
                                 @PathVariable("tacheId") Long tacheId,
                                 @RequestParam(required = false) String numeroAft,
                                 @RequestParam(required = false) Integer pointsCorpo,
                                 @RequestParam(required = false) boolean validation,
                                 @RequestParam(required = false) String commentairesRefus) {

        Tache tache = tacheRepository.findById(tacheId).get();

        if (!tache.isValidationTraitement() && !tache.isRefusTraitement()) {
            //Si refus
            if (!validation) {
                tache.setAgentTraitant(authentication.getName());
                tache.setDateTraitement(new Date());
                tache.setRefusTraitement(true);
                tache.setCommentairesRefus(commentairesRefus);
                tacheRepository.save(tache);
                return tache;
            } else {
                if (TypeTache.NOUVEAU_MEMBRE.equals(tache.getTypeTache())) {

                    Membre membre = tache.getMembre();
                    membre.setNumeroAft(numeroAft);
                    membre.setDateAffiliationCorpo(new Date());
                    membre.setActif(true);
                    membre.setFictif(false);

                    if (tache.getCodeClassementAft() != null) {
                        ClassementAFT classementAft = new ClassementAFT();
                        classementAft.setMembreFk(membre.getId());
                        classementAft.setDateClassement(new Date());
                        EchelleAFT echelleAFT = EchelleAFT.getEchelleAFTByCode(tache.getCodeClassementAft());
                        classementAft.setCodeClassement(echelleAFT.getCode());
                        classementAft.setPoints(echelleAFT.getPoints());
                        classementAft = classementAFTRepository.save(classementAft);
                        membre.setClassementAFTActuel(classementAft);
                    }

                    ClassementCorpo classementCorpo = new ClassementCorpo();
                    classementCorpo.setMembreFk(membre.getId());
                    classementCorpo.setDateClassement(new Date());
                    classementCorpo.setPoints(pointsCorpo);
                    classementCorpo = classementCorpoRepository.save(classementCorpo);
                    membre.setClassementCorpoActuel(classementCorpo);

                    membreRepository.save(membre);

                    tache.setAgentTraitant(authentication.getName());
                    tache.setDateTraitement(new Date());
                    tache.setValidationTraitement(true);
                    tache.setCommentairesRefus(commentairesRefus);
                    tacheRepository.save(tache);

                    return tache;

                } else if (TypeTache.DESACTIVATION_MEMBRE.equals(tache.getTypeTache())) {

                    Membre membre = tache.getMembre();
                    membre.setActif(false);
                    membreRepository.save(membre);

                    tache.setAgentTraitant(authentication.getName());
                    tache.setDateTraitement(new Date());
                    tache.setValidationTraitement(true);
                    tache.setCommentairesRefus(commentairesRefus);
                    tacheRepository.save(tache);

                    return tache;

                }else if (TypeTache.REACTIVATION_MEMBRE.equals(tache.getTypeTache())) {

                    Membre membre = tache.getMembre();
                    membre.setActif(true);
                    membreRepository.save(membre);

                    //Sauvegarde du nouveau classement eventuel
                    if (pointsCorpo!=null){
                        if (membre.getClassementCorpoActuel()==null || membre.getClassementCorpoActuel().getPoints() != pointsCorpo){
                            ClassementCorpo classementCorpo = new ClassementCorpo();
                            classementCorpo.setMembreFk(membre.getId());
                            classementCorpo.setDateClassement(new Date());
                            classementCorpo.setPoints(pointsCorpo);
                            classementCorpo = classementCorpoRepository.save(classementCorpo);
                            membre.setClassementCorpoActuel(classementCorpo);

                            membreRepository.save(membre);
                        }
                    }

                    tache.setAgentTraitant(authentication.getName());
                    tache.setDateTraitement(new Date());
                    tache.setValidationTraitement(true);
                    tache.setCommentairesRefus(commentairesRefus);
                    tacheRepository.save(tache);

                    return tache;

                }else if (TypeTache.CHANGEMENT_POINTS_CORPO.equals(tache.getTypeTache())) {

                    Membre membre = tache.getMembre();

                    //Sauvegarde du nouveau classement
                    if (pointsCorpo!=null){
                        if (membre.getClassementCorpoActuel()==null || membre.getClassementCorpoActuel().getPoints() != pointsCorpo){
                            ClassementCorpo classementCorpo = new ClassementCorpo();
                            classementCorpo.setMembreFk(membre.getId());
                            classementCorpo.setDateClassement(new Date());
                            classementCorpo.setPoints(pointsCorpo);
                            classementCorpo = classementCorpoRepository.save(classementCorpo);
                            membre.setClassementCorpoActuel(classementCorpo);

                            membreRepository.save(membre);
                        }
                    }

                    tache.setAgentTraitant(authentication.getName());
                    tache.setDateTraitement(new Date());
                    tache.setValidationTraitement(true);
                    tache.setCommentairesRefus(commentairesRefus);
                    tacheRepository.save(tache);

                    return tache;
                }
            }
        }
        return null;

    }


}
