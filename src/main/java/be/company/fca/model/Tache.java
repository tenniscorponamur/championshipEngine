package be.company.fca.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name="TACHE")
public class Tache {

    @Id
    @GenericGenerator(
            name = "tache-sequence",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name="sequence_name",value="hibernate_sequence"),
                    @org.hibernate.annotations.Parameter(name="increment_size",value="1")
            }
    )
    @GeneratedValue(generator = "tache-sequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column( name =  "date_demande")
    private Date dateDemande;

    @Column ( name = "type_tache")
    @Enumerated(EnumType.STRING)
    private TypeTache typeTache;

    @ManyToOne
    @JoinColumn( name = "demandeur_fk")
    private Membre demandeur;

    @ManyToOne
    @JoinColumn( name = "membre_fk")
    private Membre membre;

    @Column( name =  "code_classement_aft")
    private String codeClassementAft;

    @Column( name =  "points_corpo")
    private Integer pointsCorpo;

    @Column( name = "desactivation_membre", nullable = false)
    private boolean desactivationMembre;

    @Column( name = "reactivation_membre", nullable = false)
    private boolean reactivationMembre;

    @Column( name =  "commentaires_demande")
    private String commentairesDemande;

    @Column( name =  "date_traitement")
    private Date dateTraitement;

    @Column( name =  "agent_traitant")
    private String agentTraitant;

    @Column( name = "validation_traitement", nullable = false)
    private boolean validationTraitement;

    @Column( name = "refus_traitement", nullable = false)
    private boolean refusTraitement;

    @Column( name =  "commentaires_refus")
    private String commentairesRefus;

    @Column( name = "mark_as_read", nullable = false)
    private boolean markAsRead;

    @Column( name = "archived", nullable = false)
    private boolean archived;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(Date dateDemande) {
        this.dateDemande = dateDemande;
    }

    public TypeTache getTypeTache() {
        return typeTache;
    }

    public void setTypeTache(TypeTache typeTache) {
        this.typeTache = typeTache;
    }

    public Membre getDemandeur() {
        return demandeur;
    }

    public void setDemandeur(Membre demandeur) {
        this.demandeur = demandeur;
    }

    public Membre getMembre() {
        return membre;
    }

    public void setMembre(Membre membre) {
        this.membre = membre;
    }

    public String getCodeClassementAft() {
        return codeClassementAft;
    }

    public void setCodeClassementAft(String codeClassementAft) {
        this.codeClassementAft = codeClassementAft;
    }

    public Integer getPointsCorpo() {
        return pointsCorpo;
    }

    public void setPointsCorpo(Integer pointsCorpo) {
        this.pointsCorpo = pointsCorpo;
    }

    public boolean isReactivationMembre() {
        return reactivationMembre;
    }

    public void setReactivationMembre(boolean reactivationMembre) {
        this.reactivationMembre = reactivationMembre;
    }

    public boolean isDesactivationMembre() {
        return desactivationMembre;
    }

    public void setDesactivationMembre(boolean desactivationMembre) {
        this.desactivationMembre = desactivationMembre;
    }

    public String getCommentairesDemande() {
        return commentairesDemande;
    }

    public void setCommentairesDemande(String commentairesDemande) {
        this.commentairesDemande = commentairesDemande;
    }

    public Date getDateTraitement() {
        return dateTraitement;
    }

    public void setDateTraitement(Date dateTraitement) {
        this.dateTraitement = dateTraitement;
    }

    public String getAgentTraitant() {
        return agentTraitant;
    }

    public void setAgentTraitant(String agentTraitant) {
        this.agentTraitant = agentTraitant;
    }

    public boolean isValidationTraitement() {
        return validationTraitement;
    }

    public void setValidationTraitement(boolean validationTraitement) {
        this.validationTraitement = validationTraitement;
    }

    public boolean isRefusTraitement() {
        return refusTraitement;
    }

    public void setRefusTraitement(boolean refusTraitement) {
        this.refusTraitement = refusTraitement;
    }

    public String getCommentairesRefus() {
        return commentairesRefus;
    }

    public void setCommentairesRefus(String commentairesRefus) {
        this.commentairesRefus = commentairesRefus;
    }

    public boolean isMarkAsRead() {
        return markAsRead;
    }

    public void setMarkAsRead(boolean markAsRead) {
        this.markAsRead = markAsRead;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tache tache = (Tache) o;
        return Objects.equals(id, tache.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Tache{" +
                "dateDemande=" + dateDemande +
                ", typeTache=" + typeTache +
                ", demandeur=" + demandeur +
                '}';
    }
}
