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

    @Temporal(TemporalType.DATE)
    @Column( name = "dateNaissance" )
    private Date dateNaissance;

    @Column ( name = "genre" )
    @Enumerated(EnumType.STRING)
    private Genre genre;

    @Column( name = "actif", nullable = false)
    private boolean actif=true;

    @ManyToOne
    @JoinColumn(name = "club_fk")
    private Club club;

    @Column( name = "capitaine", nullable = false)
    private boolean capitaine=false;

    // Classements actuels

    @ManyToOne
    @JoinColumn(name = "classementaft_fk")
    private ClassementAFT classementAFTactuel;

    @ManyToOne
    @JoinColumn(name = "classementcorpo_fk")
    private ClassementCorpo classementCorpoActuel;

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

    public ClassementAFT getClassementAFTactuel() {
        return classementAFTactuel;
    }

    public void setClassementAFTactuel(ClassementAFT classementAFTactuel) {
        this.classementAFTactuel = classementAFTactuel;
    }

    public ClassementCorpo getClassementCorpoActuel() {
        return classementCorpoActuel;
    }

    public void setClassementCorpoActuel(ClassementCorpo classementCorpoActuel) {
        this.classementCorpoActuel = classementCorpoActuel;
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
