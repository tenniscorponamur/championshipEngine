package be.company.fca.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "CLASSEMENTCORPO")
public class ClassementCorpo {

    @Id
    @GenericGenerator(
            name = "classement-corpo-sequence",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name="sequence_name",value="hibernate_sequence"),
                    @org.hibernate.annotations.Parameter(name="increment_size",value="1")
            }
    )
    @GeneratedValue(generator = "classement-corpo-sequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column( name =  "points", nullable = false)
    private Integer points;

    @Temporal(TemporalType.DATE)
    @Column( name = "dateclassement", nullable = false )
    private Date dateClassement;

    @Column( name =  "membre_fk", nullable = false)
    private Long membreFk; // Pas de chargement de l'arbre relatif au membre pour les classements

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Date getDateClassement() {
        return dateClassement;
    }

    public void setDateClassement(Date dateClassement) {
        this.dateClassement = dateClassement;
    }

    public Long getMembreFk() {
        return membreFk;
    }

    public void setMembreFk(Long membreFk) {
        this.membreFk = membreFk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassementCorpo that = (ClassementCorpo) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
