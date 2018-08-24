package be.company.fca.dto;

import be.company.fca.model.*;

import java.util.Date;

public class MembreDto {

    // Donnees publiques

    private Long id;
    private String prenom;
    private String nom;
    private Genre genre;
    private Club club;
    private boolean capitaine=false;
    private boolean actif=true;
    private String numeroAft;
    private Date dateAffiliationAft;
    private String numeroClubAft;
    private boolean onlyCorpo=false;
    private ClassementAFT classementAFTActuel;
    private ClassementCorpo classementCorpoActuel;

    // Donnees privees (accessibles uniquement si authentification)

    private Date dateNaissance;
    //TODO : coordonnees et contacts

    /**
     * Constructeur permettant de filtrer les donnees du membre a transmettre aux appelants
     * @param membre
     * @param withPrivateInformations true si les informations privees doivent egalement etre communiques
     */
    public MembreDto(Membre membre,boolean withPrivateInformations){
        this.id = membre.getId();

        this.prenom = membre.getPrenom();
        this.nom = membre.getNom();
        this.genre = membre.getGenre();
        this.club = membre.getClub();
        this.capitaine = membre.isCapitaine();
        this.actif = membre.isActif();
        this.numeroAft = membre.getNumeroAft();
        this.dateAffiliationAft = membre.getDateAffiliationAft();
        this.numeroClubAft = membre.getNumeroClubAft();
        this.onlyCorpo = membre.isOnlyCorpo();
        this.classementAFTActuel = membre.getClassementAFTActuel();
        this.classementCorpoActuel = membre.getClassementCorpoActuel();

        if (withPrivateInformations){
            this.dateNaissance = membre.getDateNaissance();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    public boolean isCapitaine() {
        return capitaine;
    }

    public void setCapitaine(boolean capitaine) {
        this.capitaine = capitaine;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public String getNumeroAft() {
        return numeroAft;
    }

    public void setNumeroAft(String numeroAft) {
        this.numeroAft = numeroAft;
    }

    public Date getDateAffiliationAft() {
        return dateAffiliationAft;
    }

    public void setDateAffiliationAft(Date dateAffiliationAft) {
        this.dateAffiliationAft = dateAffiliationAft;
    }

    public String getNumeroClubAft() {
        return numeroClubAft;
    }

    public void setNumeroClubAft(String numeroClubAft) {
        this.numeroClubAft = numeroClubAft;
    }

    public boolean isOnlyCorpo() {
        return onlyCorpo;
    }

    public void setOnlyCorpo(boolean onlyCorpo) {
        this.onlyCorpo = onlyCorpo;
    }

    public ClassementAFT getClassementAFTActuel() {
        return classementAFTActuel;
    }

    public void setClassementAFTActuel(ClassementAFT classementAFTActuel) {
        this.classementAFTActuel = classementAFTActuel;
    }

    public ClassementCorpo getClassementCorpoActuel() {
        return classementCorpoActuel;
    }

    public void setClassementCorpoActuel(ClassementCorpo classementCorpoActuel) {
        this.classementCorpoActuel = classementCorpoActuel;
    }

    public Date getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(Date dateNaissance) {
        this.dateNaissance = dateNaissance;
    }
}
