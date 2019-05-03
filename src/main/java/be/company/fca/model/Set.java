package be.company.fca.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="SET")
public class Set {

    @Id
    @GenericGenerator(
            name = "set-sequence",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name="sequence_name",value="hibernate_sequence"),
                    @org.hibernate.annotations.Parameter(name="increment_size",value="1")
            }
    )
    @GeneratedValue(generator = "set-sequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column( name =  "ordre", nullable = false)
    private Integer ordre;

    @Column( name = "jeuxvisites", nullable = false)
    private Integer jeuxVisites;

    @Column( name = "jeuxvisiteurs", nullable = false)
    private Integer jeuxVisiteurs;

    @Column( name = "visitesgagnant")
    private Boolean visitesGagnant;

    @ManyToOne
    @JoinColumn(name = "match_fk")
    private Match match;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getOrdre() {
        return ordre;
    }

    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }

    public Integer getJeuxVisites() {
        return jeuxVisites;
    }

    public void setJeuxVisites(Integer jeuxVisites) {
        this.jeuxVisites = jeuxVisites;
    }

    public Integer getJeuxVisiteurs() {
        return jeuxVisiteurs;
    }

    public void setJeuxVisiteurs(Integer jeuxVisiteurs) {
        this.jeuxVisiteurs = jeuxVisiteurs;
    }

    public Boolean getVisitesGagnant() {
        return visitesGagnant;
    }

    public void setVisitesGagnant(Boolean visitesGagnant) {
        this.visitesGagnant = visitesGagnant;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Set set = (Set) o;
        return Objects.equals(id, set.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
