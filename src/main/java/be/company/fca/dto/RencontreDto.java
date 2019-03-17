package be.company.fca.dto;

import be.company.fca.model.*;

import java.util.Date;

public class RencontreDto {

    private Long id;
    private Integer numeroJournee;
    private Date dateHeureRencontre;
    private Division division;
    private Poule poule;
    private EquipeDto equipeVisites;
    private EquipeDto equipeVisiteurs;
    private Terrain terrain;
    private Court court;
    private Integer pointsVisites;
    private Integer pointsVisiteurs;
    private String informationsInterserie;
    private String commentairesEncodeur;
    private boolean resultatsEncodes;
    private boolean valide;

    public RencontreDto(Rencontre rencontre) {
        this.id = rencontre.getId();
        this.numeroJournee = rencontre.getNumeroJournee();
        this.dateHeureRencontre = rencontre.getDateHeureRencontre();
        this.division = rencontre.getDivision();
        this.poule = rencontre.getPoule();
        if (rencontre.getEquipeVisites()!=null){
            this.equipeVisites = new EquipeDto(rencontre.getEquipeVisites());
        }
        if (rencontre.getEquipeVisiteurs()!=null){
            this.equipeVisiteurs = new EquipeDto(rencontre.getEquipeVisiteurs());
        }
        this.terrain = rencontre.getTerrain();
        this.court = rencontre.getCourt();
        this.pointsVisites = rencontre.getPointsVisites();
        this.pointsVisiteurs = rencontre.getPointsVisiteurs();
        this.informationsInterserie = rencontre.getInformationsInterserie();
        this.commentairesEncodeur = rencontre.getCommentairesEncodeur();
        this.resultatsEncodes = rencontre.isResultatsEncodes();
        this.valide = rencontre.isValide();
    }

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

    public Date getDateHeureRencontre() {
        return dateHeureRencontre;
    }

    public void setDateHeureRencontre(Date dateHeureRencontre) {
        this.dateHeureRencontre = dateHeureRencontre;
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

    public EquipeDto getEquipeVisites() {
        return equipeVisites;
    }

    public void setEquipeVisites(EquipeDto equipeVisites) {
        this.equipeVisites = equipeVisites;
    }

    public EquipeDto getEquipeVisiteurs() {
        return equipeVisiteurs;
    }

    public void setEquipeVisiteurs(EquipeDto equipeVisiteurs) {
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

    public String getInformationsInterserie() {
        return informationsInterserie;
    }

    public void setInformationsInterserie(String informationsInterserie) {
        this.informationsInterserie = informationsInterserie;
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
}
