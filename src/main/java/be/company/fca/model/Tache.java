package be.company.fca.model;

import java.util.Date;

public class Tache {

    private Long id;
    private Date dateDemande;
    private TypeTache typeTache;
    private Membre demandeur;
    private Membre membre;

    //TODO : ajouter dans Membre : adhesion politique confidentialite ET membre fictif durant periode de creation

    private Integer pointsCorpo;
    private boolean desactivationMembre;
    private boolean reactivationMembre;
    private String commentairesDemande;

    private Date dateTraitement;
    private User agentTraitant;
    private boolean validationTraitement;
    private boolean refusTraitement;
    private String commentairesRefus;

    private boolean markAsRead;
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

    public User getAgentTraitant() {
        return agentTraitant;
    }

    public void setAgentTraitant(User agentTraitant) {
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
}
