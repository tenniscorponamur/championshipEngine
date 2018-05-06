package be.company.fca.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="EQUIPE")
public class Equipe {

    @Id
    @GeneratedValue
    private Long id;

    // Code alphabetique pour preciser le "numero" de l'equipe. Ex : UNAMUR B

    @Column( name =  "code_alphabetique")
    private String codeAlphabetique;

    @ManyToOne
    @JoinColumn(name = "division_fk", nullable = false)
    private Division division;

    @ManyToOne
    @JoinColumn(name = "club_fk", nullable = false)
    private Club club;

    @ManyToOne
    @JoinColumn(name = "capitaine_fk")
    private Membre capitaine;
//
    @ManyToOne
    @JoinColumn(name = "poule_fk")
    private Poule poule;


    //    private Terrain terrainDomicile;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeAlphabetique() {
        return codeAlphabetique;
    }

    public void setCodeAlphabetique(String codeAlphabetique) {
        this.codeAlphabetique = codeAlphabetique;
    }

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    public Membre getCapitaine() {
        return capitaine;
    }

    public void setCapitaine(Membre capitaine) {
        this.capitaine = capitaine;
    }

    public Poule getPoule() {
        return poule;
    }

    public void setPoule(Poule poule) {
        this.poule = poule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipe equipe = (Equipe) o;
        return Objects.equals(id, equipe.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}