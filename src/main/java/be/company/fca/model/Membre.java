package be.company.fca.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="MEMBRE")
public class Membre {

    @Id
    @GeneratedValue
    private Long id;

    @Column( name =  "numero", length = 500, unique = true)
    private String numero;

    @Column( name =  "prenom", length = 500, nullable = false)
    private String prenom;

    @Column( name =  "nom", length = 500, nullable = false)
    private String nom;

    @Column( name =  "password", length = 500, nullable = false)
    private String password;

    @Temporal(TemporalType.DATE)
    @Column( name = "dateNaissance" )
    private Date dateNaissance;

    @Column ( name = "genre" )
    @Enumerated(EnumType.STRING)
    private Genre genre;

    @ManyToOne
    @JoinColumn(name = "club_fk")
    private Club club;

    @Column( name = "capitaine", nullable = false)
    private boolean capitaine=false;

    // Infos tennistiques

    @Column( name = "actif", nullable = false)
    private boolean actif=true;

    @Column( name =  "numero_aft")
    private String numeroAft;

    @Temporal(TemporalType.DATE)
    @Column( name = "date_affiliation_aft" )
    private Date dateAffiliationAft;

    @Column( name =  "numero_club_aft")
    private String numeroClubAft;

    @Column( name = "only_corpo", nullable = false)
    private boolean onlyCorpo=false;

    // Classements actuels

    @ManyToOne
    @JoinColumn(name = "classementaft_fk")
    private ClassementAFT classementAFTActuel;

    @ManyToOne
    @JoinColumn(name = "classementcorpo_fk")
    private ClassementCorpo classementCorpoActuel;

    @Temporal(TemporalType.DATE)
    @Column( name = "date_affiliation_corpo" )
    private Date dateAffiliationCorpo;

    @Temporal(TemporalType.DATE)
    @Column( name = "date_desaffiliation_corpo" )
    private Date dateDesaffiliationCorpo;

    @Column( name = "responsable_club", nullable = false)
    private boolean responsableClub=false;

    @Column( name =  "code_postal")
    private String codePostal;

    @Column( name =  "localite")
    private String localite;

    @Column( name =  "rue")
    private String rue;

    @Column( name =  "rue_numero")
    private String rueNumero;

    @Column( name =  "rue_boite")
    private String rueBoite;

    @Column( name =  "telephone")
    private String telephone;

    @Column( name =  "gsm")
    private String gsm;

    @Column( name =  "mail")
    private String mail;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
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

    public Date getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(Date dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Membre membre = (Membre) o;

        return id != null ? id.equals(membre.id) : membre.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
