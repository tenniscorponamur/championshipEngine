package be.company.fca.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "CLASSEMENTAFT")
public class ClassementAFT {

    // Liste des codes AFT et points associes disponibles en //
    // afin de pouvoir gerer une adaptation du reglement sans impacter les anciens codes

    @Id
    @GeneratedValue
    private Long id;

    @Column( name =  "codeclassement", nullable = false)
    private String codeClassement;

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

    public String getCodeClassement() {
        return codeClassement;
    }

    public void setCodeClassement(String codeClassement) {
        this.codeClassement = codeClassement;
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

        ClassementAFT that = (ClassementAFT) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
