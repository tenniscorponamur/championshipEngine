package be.company.fca.model;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name="RENCONTRE")
public class Rencontre {

    @Id
    @GeneratedValue
    private Long id;

    @Column( name =  "numeroJournee")
    private Integer numeroJournee;

    @Column( name =  "dateRencontre")
    private Date dateRencontre;

    @ManyToOne
    @JoinColumn(name = "division_fk", nullable = false)
    private Division division;

    // La poule doit etre une poule de la division mais elle peut etre nulle quand il s'agit d'une rencontre interserie

    @ManyToOne
    @JoinColumn(name = "poule_fk")
    private Poule poule;

    @ManyToOne
    @JoinColumn(name = "visites_fk", nullable = false)
    private Equipe equipeVisites;

    @ManyToOne
    @JoinColumn(name = "visiteurs_fk", nullable = false)
    private Equipe equipeVisiteurs;

    @ManyToOne
    @JoinColumn(name = "terrain_fk")
    private Terrain terrain;

    //TODO : liste des matchs - quid stockage resultat rencontre ?? Redondance pour simplifier le calcul des points --> gerer au niveau transactionnel lors
    // de l'enregistrement du resultat d'un des matchs de la rencontre ??? methode pour connaitre l'equipe gagnante ?? --> structure differente ??

    // --> prevoir une redondance des informations pour les championnats clotures

//    private Integer pointsVisites;
//    private Integer pointsVisiteurs;
//    private Integer setsVisites;
//    private Integer setsVisiteurs;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumeroJournee() {
        return numeroJournee;
    }

    public void setNumeroJournee(Integer numeroJournee) {
        this.numeroJournee = numeroJournee;
    }

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    public Poule getPoule() {
        return poule;
    }

    public void setPoule(Poule poule) {
        this.poule = poule;
    }

    public Equipe getEquipeVisites() {
        return equipeVisites;
    }

    public void setEquipeVisites(Equipe equipeVisites) {
        this.equipeVisites = equipeVisites;
    }

    public Equipe getEquipeVisiteurs() {
        return equipeVisiteurs;
    }

    public void setEquipeVisiteurs(Equipe equipeVisiteurs) {
        this.equipeVisiteurs = equipeVisiteurs;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    public Date getDateRencontre() {
        return dateRencontre;
    }

    public void setDateRencontre(Date dateRencontre) {
        this.dateRencontre = dateRencontre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rencontre rencontre = (Rencontre) o;
        return Objects.equals(id, rencontre.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Rencontre{" +
                "numeroJournee=" + numeroJournee +
                ", equipeVisites=" + equipeVisites +
                ", equipeVisiteurs=" + equipeVisiteurs +
                '}';
    }
}
