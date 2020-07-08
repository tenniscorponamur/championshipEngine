package be.company.fca.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="EQUIPE")
public class Equipe {

    @Id
    @GenericGenerator(
            name = "equipe-sequence",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name="sequence_name",value="hibernate_sequence"),
                    @org.hibernate.annotations.Parameter(name="increment_size",value="1")
            }
    )
    @GeneratedValue(generator = "equipe-sequence", strategy = GenerationType.SEQUENCE)
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

    @ManyToOne
    @JoinColumn(name = "terrain_fk")
    private Terrain terrain;

    @Column( name = "hybride", nullable = false)
    private boolean hybride=false;

    @Column( name =  "commentaires")
    private String commentaires;

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

    public Terrain getTerrain() {
        return terrain;
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    public boolean isHybride() {
        return hybride;
    }

    public void setHybride(boolean hybride) {
        this.hybride = hybride;
    }

    public String getCommentaires() {
        return commentaires;
    }

    public void setCommentaires(String commentaires) {
        this.commentaires = commentaires;
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

    @Override
    public String toString() {
        return "Equipe{" +
                "codeAlphabetique='" + codeAlphabetique + '\'' +
                '}';
    }
}
