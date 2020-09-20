package be.company.fca.controller;

import be.company.fca.dto.EquipeDto;
import be.company.fca.model.*;
import be.company.fca.repository.*;
import be.company.fca.service.DivisionService;
import be.company.fca.service.EquipeService;
import be.company.fca.service.UserService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
@Api(description = "API REST pour la gestion des equipes")
public class EquipeController {

    @Autowired
    private EquipeRepository equipeRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private MembreRepository membreRepository;

    @Autowired
    private MembreEquipeRepository membreEquipeRepository;

    @Autowired
    private DivisionRepository divisionRepository;

    @Autowired
    private PouleRepository pouleRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private SetRepository setRepository;

    @Autowired
    private AutorisationRencontreRepository autorisationrencontreRepository;

    @Autowired
    private RencontreRepository rencontreRepository;

    @Autowired
    private ChampionnatRepository championnatRepository;

    @Autowired
    private EquipeService equipeService;

    @Autowired
    private UserService userService;

    // DTO pour les capitaines d'equipe afin de ne pas recuperer les donnees privees

    @RequestMapping(method= RequestMethod.GET, path="/public/equipes")
    public List<EquipeDto> getEquipesByDivisionOrPoule(@RequestParam Long divisionId, @RequestParam(required = false) Long pouleId) {
        List<EquipeDto> equipesDto = new ArrayList<>();
        List<Equipe> equipes = new ArrayList<Equipe>();

        if (pouleId!=null){
            Poule poule = new Poule();
            poule.setId(pouleId);
            equipes = (List<Equipe>) equipeRepository.findByPoule(poule);
        }else{
            Division division = new Division();
            division.setId(divisionId);
            equipes = (List<Equipe>) equipeRepository.findByDivision(division);
        }

        for (Equipe equipe : equipes){
            equipesDto.add(new EquipeDto(equipe));
        }

        return equipesDto;
    }

    @RequestMapping(method= RequestMethod.GET, path="/public/equipesByClub")
    public List<EquipeDto> getEquipesByChampionnatAndClub(@RequestParam Long championnatId, @RequestParam Long clubId) {
        List<EquipeDto> equipesDto = new ArrayList<>();
        List<Equipe> equipes = (List<Equipe>) equipeRepository.findByChampionnatAndClub(championnatId,clubId);

        for (Equipe equipe : equipes){
            equipesDto.add(new EquipeDto(equipe));
        }

        return equipesDto;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/public/equipes/criterium")
    public List<Equipe> getEquipesCriteriumByAnnee(@RequestParam String annee) {
        List<Equipe> equipes = new ArrayList<>();
        List<Championnat> championnats = championnatRepository.findByTypeAndAnnee(TypeChampionnat.CRITERIUM,annee);
        for (Championnat championnat : championnats){
            List<Division> divisions = (List<Division>) divisionRepository.findByChampionnat(championnat);
            for (Division division : divisions){
                equipes.addAll((Collection<? extends Equipe>) equipeRepository.findByDivision(division));
            }
        }
        return equipes;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN_USER','RESPONSABLE_CLUB')")
    @RequestMapping(value = "/private/equipe", method = RequestMethod.PUT)
    public Equipe updateEquipeDetails(Authentication authentication, @RequestParam Long divisionId, @RequestBody Equipe equipe){

        // Verifier que les operations concernent bien le club du membre connecte

        boolean adminConnected = userService.isAdmin(authentication);
        boolean updatable = false;
        if (adminConnected){
            updatable = true;
        }else{
            Membre membreConnecte = userService.getMembreFromAuthentication(authentication);
            if (equipe.getClub().equals(membreConnecte.getClub())){
                updatable=true;
            }
        }

        // Verifier que les operations concernent bien le club du membre connecte

        if (!updatable){
            throw new RuntimeException("Operation not permitted - Insuffisent rights");
        }

        Division division = new Division();
        division.setId(divisionId);
        equipe.setDivision(division);

        // Terrain, capitaine, hybride et commentaires uniquement !!
        // (legere faille de securite pour le mode hybride car le responsables pourrait le changer s'il bidouille l'appel)

        equipeRepository.updateDetails(equipe.getId(),equipe.getCapitaine(),equipe.getTerrain(),equipe.isHybride(), equipe.getCommentaires());

        // On va tagguer le membre selectionne comme capitaine
        if (equipe.getCapitaine()!=null){
            Membre capitaine = membreRepository.findById(equipe.getCapitaine().getId()).get();
            capitaine.setCapitaine(true);
            membreRepository.save(capitaine);
        }

        return equipe;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN_USER','RESPONSABLE_CLUB')")
    @RequestMapping(value = "/private/equipe", method = RequestMethod.POST)
    public Equipe addEquipe(Authentication authentication, @RequestParam Long divisionId, @RequestBody Equipe equipe){

        // Verifier que les operations concernent bien le club du membre connecte

        boolean adminConnected = userService.isAdmin(authentication);
        boolean addable = false;
        if (adminConnected){
            addable = true;
        }else{
            Membre membreConnecte = userService.getMembreFromAuthentication(authentication);
            if (equipe.getClub().equals(membreConnecte.getClub())){
                addable=true;
            }
        }

        // Verifier que les operations concernent bien le club du membre connecte

        if (!addable){
            throw new RuntimeException("Operation not permitted - Insuffisent rights");
        }

        Division division = divisionRepository.findById(divisionId).get();
        equipe.setDivision(division);

        // Operation non-permise si le calendrier est valide ou cloture
        if (division.getChampionnat().isCalendrierValide() || division.getChampionnat().isCloture()){
            throw new RuntimeException("Operation not supported - Calendrier valide ou championnat cloture");
        }

        equipe.setPoule(getFirstPoule(division));

        Club club = equipe.getClub();
        Championnat championnat = equipe.getDivision().getChampionnat();

        Equipe equipeSaved = equipeRepository.save(equipe);
        // On signale que le calendrier doit etre rafraichi si l'equipe a ete sauvee
        championnatRepository.updateCalendrierARafraichir(division.getChampionnat().getId(),true);

        // Renommage des equipes de ce club afin que les choses soient correctes en sortie de cet appel
        List<Equipe> equipeList = renommageEquipesSansSauvegarde(championnat.getId(),club);
        updateEquipeNamesOnly(equipeList);

        // Nom de l'equipe sauvee a populer
        for (Equipe team : equipeList){
            if (team.getId().equals(equipeSaved.getId())){
                equipeSaved.setCodeAlphabetique(team.getCodeAlphabetique());
            }
        }

        return equipeSaved;
    }

    /**
     * Permet de recuperer la premiere poule de la division
     * ou de la creer le cas echeant
     * @param division
     */
    private Poule getFirstPoule(Division division){
        List<Poule> poules = (List<Poule>) pouleRepository.findByDivision(division);
        Poule firstPoule = null;
        if (poules.size()==0){
            Poule poule = new Poule();
            poule.setDivision(division);
            poule.setNumero(1);
            firstPoule = pouleRepository.save(poule);
        }else{
            Collections.sort(poules, new Comparator<Poule>() {
                @Override
                public int compare(Poule o1, Poule o2) {
                    return o1.getNumero().compareTo(o2.getNumero());
                }
            });
            firstPoule = poules.get(0);
        }
        return firstPoule;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN_USER','RESPONSABLE_CLUB')")
    @RequestMapping(value = "/private/equipe", method = RequestMethod.DELETE)
    public void deleteEquipe(Authentication authentication, @RequestParam Long id){

        Equipe equipe = equipeRepository.findById(id).get();

        // Operation non-permise si le calendrier est valide ou cloture
        if (equipe.getDivision().getChampionnat().isCalendrierValide() || equipe.getDivision().getChampionnat().isCloture()){
            throw new RuntimeException("Operation not supported - Calendrier valide ou championnat cloture");
        }

        // Verifier que les operations concernent bien le club du membre connecte

        boolean adminConnected = userService.isAdmin(authentication);
        boolean deletable = false;
        if (adminConnected){
            deletable = true;
        }else{
            Membre membreConnecte = userService.getMembreFromAuthentication(authentication);
            if (equipe.getClub().equals(membreConnecte.getClub())){
                deletable=true;
            }
        }

        // Verifier que les operations concernent bien le club du membre connecte

        if (!deletable){
            throw new RuntimeException("Operation not permitted - Insuffisent rights");
        }

        Club club = equipe.getClub();
        Division division = equipe.getDivision();
        Championnat championnat = equipe.getDivision().getChampionnat();

        // Supprimer la composition eventuelle
        membreEquipeRepository.deleteByEquipeFk(id);

        // Supprimer les rencontres associees a cette equipe sinon on ne pourra pas la supprimer :-)
        List<Rencontre> rencontresEquipe = (List<Rencontre>) rencontreRepository.findRencontresByEquipe(equipe);
        for (Rencontre rencontreEquipe : rencontresEquipe){
            autorisationrencontreRepository.deleteByRencontreFk(rencontreEquipe.getId());
            setRepository.deleteByRencontreId(rencontreEquipe.getId());
            matchRepository.deleteByRencontreId(rencontreEquipe.getId());
            rencontreRepository.deleteById(rencontreEquipe.getId());
        }

        equipeRepository.deleteById(id);
        // On signale que le calendrier doit etre rafraichi si l'equipe a ete supprimee
        championnatRepository.updateCalendrierARafraichir(equipe.getDivision().getChampionnat().getId(),true);

        // Renommage des equipes de ce club afin que les choses soient correctes en sortie de cet appel
        updateEquipeNamesOnly(renommageEquipesSansSauvegarde(championnat.getId(),club));

        List<Equipe> equipes = (List<Equipe>) equipeRepository.findByDivision(division);
        if (equipes.size()==0){
            pouleRepository.deleteByDivision(division);
        }
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/equipes/names", method = RequestMethod.PUT)
    public List<Equipe> updateEquipeNamesOnly(@RequestBody List<Equipe> equipeList){
        return equipeService.updateEquipeNames(equipeList);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN_USER','RESPONSABLE_CLUB')")
    @RequestMapping(value = "/private/equipes/changeNames", method = RequestMethod.PUT)
    public List<Equipe> setAndUpdateEquipeNames(Authentication authentication, @RequestParam Long championnatId, @RequestParam Long clubId){

        // Verifier que les operations concernent bien le club du membre connecte

        boolean adminConnected = userService.isAdmin(authentication);
        Club club = null;
        if (adminConnected){
            club = clubRepository.findById(clubId).get();
        }else{
            Membre membreConnecte = userService.getMembreFromAuthentication(authentication);
            club = membreConnecte.getClub();
        }

        List<Equipe> equipeList = renommageEquipesSansSauvegarde(championnatId,club);

        return equipeService.updateEquipeNames(equipeList);
    }

    /**
     * Permet de renommer les equipes dans un championnat pour un club donne
     * @param championnatId
     * @param club
     * @return
     */
    private List<Equipe> renommageEquipesSansSauvegarde(Long championnatId, Club club){
        List<Equipe> equipeList = (List<Equipe>) equipeRepository.findByChampionnatAndClub(championnatId,club.getId());

        // Changer les noms d'equipes apres tri par division

        // Dans la meme division, conserver l'ordre initial :
        // Ex : A1, B2 division 1 C3 division 2 -> A1, B2 et C3 division 1 et non pas C1, B3, A2 division 1

        Collections.sort(equipeList, new Comparator<Equipe>() {
            @Override
            public int compare(Equipe o1, Equipe o2) {
                int compareDivision = o1.getDivision().getNumero().compareTo(o2.getDivision().getNumero());
                if (compareDivision!=0){
                    return compareDivision;
                }else{
                    if (o1.getCodeAlphabetique()==null){
                        return 1;
                    }
                    if (o2.getCodeAlphabetique()==null){
                        return -1;
                    }
                    if (o1.getCodeAlphabetique()!=null && o2.getCodeAlphabetique()!=null){
                        return o1.getCodeAlphabetique().compareTo(o2.getCodeAlphabetique());
                    }
                }
                return 0;
            }
        });
        for (int i=0;i<equipeList.size();i++){
            Equipe equipe = equipeList.get(i);
            equipe.setCodeAlphabetique(club.getNom() + " " + String.valueOf((char) (97 + i)).toUpperCase());
        }

        return equipeList;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN_USER','RESPONSABLE_CLUB')")
    @RequestMapping(value = "/private/equipe/division", method = RequestMethod.PUT)
    public Equipe updateDivisionEquipe(Authentication authentication, @RequestParam Long equipeId, @RequestBody Division division){

        Equipe equipe = equipeRepository.findById(equipeId).get();

        // Operation non-permise si le calendrier est valide ou cloture
        if (equipe.getDivision().getChampionnat().isCalendrierValide() || equipe.getDivision().getChampionnat().isCloture()){
            throw new RuntimeException("Operation not supported - Calendrier valide ou championnat cloture");
        }

        // Verifier que les operations concernent bien le club du membre connecte

        boolean adminConnected = userService.isAdmin(authentication);
        boolean divisionUpdatable = false;
        if (adminConnected){
            divisionUpdatable = true;
        }else{
            Membre membreConnecte = userService.getMembreFromAuthentication(authentication);
            if (equipe.getClub().equals(membreConnecte.getClub())){
                divisionUpdatable=true;
            }
        }

        // Verifier que les operations concernent bien le club du membre connecte

        if (!divisionUpdatable){
            throw new RuntimeException("Operation not permitted - Insuffisent rights");
        }

        Division oldDivision = equipe.getDivision();
        Division newDivision = division;

        // S'il ne s'agit pas du meme championnat, operation interdite
        if (!oldDivision.getChampionnat().equals(newDivision.getChampionnat())){
            throw new RuntimeException("Operation not permitted - Not the same Championship");
        }

        equipe.setDivision(newDivision);
        equipe.setPoule(getFirstPoule(newDivision));

        equipeRepository.updateDivisionAndPoule(equipe.getId(),equipe.getDivision(),equipe.getPoule());

        // Test si ancienne division contient encore equipe sinon suppression poule (appel supplementaire car suppressions objets entretemps
        List<Equipe> equipes = (List<Equipe>) equipeRepository.findByDivision(oldDivision);
        if (equipes.size()==0){
            pouleRepository.deleteByDivision(oldDivision);
        }

        // On signale que le calendrier doit etre rafraichi si l'equipe a change de division
        championnatRepository.updateCalendrierARafraichir(equipe.getDivision().getChampionnat().getId(),true);

        // Renommage des equipes de ce club afin que les choses soient correctes en sortie de cet appel
        updateEquipeNamesOnly(renommageEquipesSansSauvegarde(equipe.getDivision().getChampionnat().getId(),equipe.getClub()));

        return equipe;
    }

    @PreAuthorize("hasAuthority('ADMIN_USER')")
    @RequestMapping(value = "/private/equipe/poule", method = RequestMethod.PUT)
    public Equipe updatePouleEquipe(@RequestParam Long equipeId, @RequestBody Poule poule){

        Equipe equipe = equipeRepository.findById(equipeId).get();

        // Operation non-permise si le calendrier est valide ou cloture
        if (equipe.getDivision().getChampionnat().isCalendrierValide() || equipe.getDivision().getChampionnat().isCloture()){
            throw new RuntimeException("Operation not supported - Calendrier valide ou championnat cloture");
        }

        equipeRepository.updatePoule(equipeId,poule);

        // On signale que le calendrier doit etre rafraichi si l'equipe a change de poule
        championnatRepository.updateCalendrierARafraichir(equipe.getDivision().getChampionnat().getId(),true);

        return equipe;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN_USER','RESPONSABLE_CLUB')")
    @RequestMapping(value = "/private/equipe/{equipeId}/membres", method = RequestMethod.GET)
    public List<Membre> getMembresEquipe(Authentication authentication, @PathVariable("equipeId") Long equipeId){

        // Verifier que les operations concernent bien le club du membre connecte

        boolean adminConnected = userService.isAdmin(authentication);
        boolean equipeAccessible = false;
        if (adminConnected){
            equipeAccessible = true;
        }else{
            Membre membreConnecte = userService.getMembreFromAuthentication(authentication);
            Equipe equipe = equipeRepository.findById(equipeId).get();
            if (equipe.getClub().equals(membreConnecte.getClub())){
                equipeAccessible=true;
            }
        }

        // Verifier que les operations concernent bien le club du membre connecte

        if (!equipeAccessible){
            throw new RuntimeException("Operation not permitted - Insuffisent rights");
        }

        return membreRepository.findMembresByEquipeFk(equipeId);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN_USER','RESPONSABLE_CLUB')")
    @RequestMapping(value = "/private/equipe/{equipeId}/membre/{membreId}", method = RequestMethod.POST)
    public boolean addMembreEquipe(Authentication authentication, @PathVariable("equipeId") Long equipeId, @PathVariable("membreId") Long membreId){

        // Verifier que les operations concernent bien le club du membre connecte

        boolean adminConnected = userService.isAdmin(authentication);
        boolean equipeAccessible = false;
        if (adminConnected){
            equipeAccessible = true;
        }else{
            Membre membreConnecte = userService.getMembreFromAuthentication(authentication);
            Equipe equipe = equipeRepository.findById(equipeId).get();
            if (equipe.getClub().equals(membreConnecte.getClub())){
                equipeAccessible=true;
            }
        }

        // Verifier que les operations concernent bien le club du membre connecte

        if (!equipeAccessible){
            throw new RuntimeException("Operation not permitted - Insuffisent rights");
        }

        MembreEquipe membreEquipe = new MembreEquipe();
        membreEquipe.setEquipeFk(equipeId);
        membreEquipe.setMembreFk(membreId);
        membreEquipeRepository.save(membreEquipe);
        return true;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN_USER','RESPONSABLE_CLUB')")
    @RequestMapping(value = "/private/equipe/{equipeId}/membre/{membreId}", method = RequestMethod.DELETE)
    public boolean deleteMembreEquipe(Authentication authentication, @PathVariable("equipeId") Long equipeId, @PathVariable("membreId") Long membreId) {

        // Verifier que les operations concernent bien le club du membre connecte

        boolean adminConnected = userService.isAdmin(authentication);
        boolean equipeAccessible = false;
        if (adminConnected){
            equipeAccessible = true;
        }else{
            Membre membreConnecte = userService.getMembreFromAuthentication(authentication);
            Equipe equipe = equipeRepository.findById(equipeId).get();
            if (equipe.getClub().equals(membreConnecte.getClub())){
                equipeAccessible=true;
            }
        }

        // Verifier que les operations concernent bien le club du membre connecte

        if (!equipeAccessible){
            throw new RuntimeException("Operation not permitted - Insuffisent rights");
        }

        membreEquipeRepository.deleteByEquipeFkAndMembreFk(equipeId,membreId);
        return true;
    }

    //1107, 1113, 1116, 1112: division
    //1093 : club
    //1092 : club

//
//    @RequestMapping(method= RequestMethod.GET, path="/public/equipe/createEquipe")
//    public Equipe createEquipe() {
//        Division division = new Division();
//        division.setId(1112L);
//        Club club = new Club();
//        club.setId(1093L);
//        Equipe equipe = new Equipe();
//        equipe.setDivision(division);
//        equipe.setClub(club);
//        return equipeRepository.save(equipe);
//    }

    public static void main(String[] args) {
        for (int i=0;i<10;i++){
        System.err.println(String.valueOf((char) (97 + i)));

        }
    }
}
