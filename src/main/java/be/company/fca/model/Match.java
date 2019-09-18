package be.company.fca.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="MATCH")
public class Match {

    @Id
    @GenericGenerator(
            name = "match-sequence",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name="sequence_name",value="hibernate_sequence"),
                    @org.hibernate.annotations.Parameter(name="increment_size",value="1")
            }
    )
    @GeneratedValue(generator = "match-sequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column( name =  "ordre", nullable = false)
    private Integer ordre;

    @Column ( name = "type" )
    @Enumerated(EnumType.STRING)
    private TypeMatch type;

    @ManyToOne
    @JoinColumn(name = "joueurvisites1_fk")
    private Membre joueurVisites1;

    @ManyToOne
    @JoinColumn(name = "joueurvisites2_fk")
    private Membre joueurVisites2;

    @ManyToOne
    @JoinColumn(name = "joueurvisiteurs1_fk")
    private Membre joueurVisiteurs1;

    @ManyToOne
    @JoinColumn(name = "joueurvisiteurs2_fk")
    private Membre joueurVisiteurs2;

    @Column( name =  "pointsvisites")
    private Integer pointsVisites;

    @Column( name =  "pointsvisiteurs")
    private Integer pointsVisiteurs;

    @Column( name = "set_unique", nullable = false)
    private boolean setUnique=false;

    @ManyToOne
    @JoinColumn(name = "rencontre_fk")
    private Rencontre rencontre;

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

    public TypeMatch getType() {
        return type;
    }

    public void setType(TypeMatch type) {
        this.type = type;
    }

    public Membre getJoueurVisites1() {
        return joueurVisites1;
    }

    public void setJoueurVisites1(Membre joueurVisites1) {
        this.joueurVisites1 = joueurVisites1;
    }

    public Membre getJoueurVisites2() {
        return joueurVisites2;
    }

    public void setJoueurVisites2(Membre joueurVisites2) {
        this.joueurVisites2 = joueurVisites2;
    }

    public Membre getJoueurVisiteurs1() {
        return joueurVisiteurs1;
    }

    public void setJoueurVisiteurs1(Membre joueurVisiteurs1) {
        this.joueurVisiteurs1 = joueurVisiteurs1;
    }

    public Membre getJoueurVisiteurs2() {
        return joueurVisiteurs2;
    }

    public void setJoueurVisiteurs2(Membre joueurVisiteurs2) {
        this.joueurVisiteurs2 = joueurVisiteurs2;
    }

    public Integer getPointsVisites() {
        return pointsVisites;
    }

    public void setPointsVisites(Integer pointsVisites) {
        this.pointsVisites = pointsVisites;
    }

    public Integer getPointsVisiteurs() {
        return pointsVisiteurs;
    }

    public void setPointsVisiteurs(Integer pointsVisiteurs) {
        this.pointsVisiteurs = pointsVisiteurs;
    }

    public boolean isSetUnique() {
        return setUnique;
    }

    public void setSetUnique(boolean setUnique) {
        this.setUnique = setUnique;
    }

    public Rencontre getRencontre() {
        return rencontre;
    }

    public void setRencontre(Rencontre rencontre) {
        this.rencontre = rencontre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return Objects.equals(id, match.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
