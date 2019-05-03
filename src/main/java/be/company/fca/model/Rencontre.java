package be.company.fca.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name="RENCONTRE")
public class Rencontre {

    @Id
    @GenericGenerator(
            name = "rencontre-sequence",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name="sequence_name",value="hibernate_sequence"),
                    @org.hibernate.annotations.Parameter(name="increment_size",value="1")
            }
    )
    @GeneratedValue(generator = "rencontre-sequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column( name =  "numerojournee")
    private Integer numeroJournee;

    @Column( name =  "dateheurerencontre")
    private Date dateHeureRencontre;

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

    @ManyToOne
    @JoinColumn(name = "court_fk")
    private Court court;

    @Column( name =  "pointsvisites")
    private Integer pointsVisites;

    @Column( name =  "pointsvisiteurs")
    private Integer pointsVisiteurs;

    @Column (name = "informations_interserie")
    private String informationsInterserie;

    @Column (name = "finale_interserie", nullable = false)
    private boolean finaleInterserie=false;

    @Column (name = "commentaires_encodeur")
    private String commentairesEncodeur;

    @Column( name = "resultats_encodes", nullable = false)
    private boolean resultatsEncodes=false;

    @Column( name = "valide", nullable = false)
    private boolean valide=false;

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

    public Court getCourt() {
        return court;
    }

    public void setCourt(Court court) {
        this.court = court;
    }

    public Date getDateHeureRencontre() {
        return dateHeureRencontre;
    }

    public void setDateHeureRencontre(Date dateHeureRencontre) {
        this.dateHeureRencontre = dateHeureRencontre;
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

    public String getCommentairesEncodeur() {
        return commentairesEncodeur;
    }

    public void setCommentairesEncodeur(String commentairesEncodeur) {
        this.commentairesEncodeur = commentairesEncodeur;
    }

    public boolean isValide() {
        return valide;
    }

    public void setValide(boolean valide) {
        this.valide = valide;
    }

    public boolean isResultatsEncodes() {
        return resultatsEncodes;
    }

    public void setResultatsEncodes(boolean resultatsEncodes) {
        this.resultatsEncodes = resultatsEncodes;
    }

    public String getInformationsInterserie() {
        return informationsInterserie;
    }

    public void setInformationsInterserie(String informationsInterserie) {
        this.informationsInterserie = informationsInterserie;
    }

    public boolean isFinaleInterserie() {
        return finaleInterserie;
    }

    public void setFinaleInterserie(boolean finaleInterserie) {
        this.finaleInterserie = finaleInterserie;
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
