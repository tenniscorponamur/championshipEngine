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
    private Date dateAffiliationCorpo;
    private Date dateDesaffiliationCorpo;
    private boolean responsableClub=false;
    private String codePostal;
    private String localite;
    private String rue;
    private String rueNumero;
    private String rueBoite;
    private String telephone;
    private String gsm;
    private String mail;

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
        this.dateAffiliationCorpo = membre.getDateAffiliationCorpo();
        this.dateDesaffiliationCorpo = membre.getDateDesaffiliationCorpo();
        this.responsableClub = membre.isResponsableClub();

        if (withPrivateInformations){
            this.dateNaissance = membre.getDateNaissance();
            this.codePostal = membre.getCodePostal();
            this.localite = membre.getLocalite();
            this.rue = membre.getRue();
            this.rueNumero = membre.getRueNumero();
            this.rueBoite = membre.getRueBoite();
            this.telephone = membre.getTelephone();
            this.gsm = membre.getGsm();
            this.mail = membre.getMail();
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

    public Date getDateAffiliationCorpo() {
        return dateAffiliationCorpo;
    }

    public void setDateAffiliationCorpo(Date dateAffiliationCorpo) {
        this.dateAffiliationCorpo = dateAffiliationCorpo;
    }

    public Date getDateDesaffiliationCorpo() {
        return dateDesaffiliationCorpo;
    }

    public void setDateDesaffiliationCorpo(Date dateDesaffiliationCorpo) {
        this.dateDesaffiliationCorpo = dateDesaffiliationCorpo;
    }

    public boolean isResponsableClub() {
        return responsableClub;
    }

    public void setResponsableClub(boolean responsableClub) {
        this.responsableClub = responsableClub;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public String getLocalite() {
        return localite;
    }

    public void setLocalite(String localite) {
        this.localite = localite;
    }

    public String getRue() {
        return rue;
    }

    public void setRue(String rue) {
        this.rue = rue;
    }

    public String getRueNumero() {
        return rueNumero;
    }

    public void setRueNumero(String rueNumero) {
        this.rueNumero = rueNumero;
    }

    public String getRueBoite() {
        return rueBoite;
    }

    public void setRueBoite(String rueBoite) {
        this.rueBoite = rueBoite;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getGsm() {
        return gsm;
    }

    public void setGsm(String gsm) {
        this.gsm = gsm;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}
