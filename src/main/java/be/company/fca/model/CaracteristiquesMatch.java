package be.company.fca.model;

import be.company.fca.dto.MembreDto;

import java.util.Date;

public class CaracteristiquesMatch {

    private TypeMatch typeMatch;
    private Date date;
    private MembreLight joueur;
    private Integer pointsJoueur;
    private MembreLight adversaire;
    private Integer pointsAdversaire;
    private MembreLight partenaire;
    private Integer pointsPartenaire;
    private MembreLight partenaireAdversaire;
    private Integer pointsPartenaireAdversaire;
    private Integer differencePoints;
    private ResultatMatch resultatMatch;
    private Integer pointsGagnesOuPerdus;

    public TypeMatch getTypeMatch() {
        return typeMatch;
    }

    public void setTypeMatch(TypeMatch typeMatch) {
        this.typeMatch = typeMatch;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public MembreLight getJoueur() {
        return joueur;
    }

    public void setJoueur(MembreLight joueur) {
        this.joueur = joueur;
    }

    public Integer getPointsJoueur() {
        return pointsJoueur;
    }

    public void setPointsJoueur(Integer pointsJoueur) {
        this.pointsJoueur = pointsJoueur;
    }

    public MembreLight getAdversaire() {
        return adversaire;
    }

    public void setAdversaire(MembreLight adversaire) {
        this.adversaire = adversaire;
    }

    public Integer getPointsAdversaire() {
        return pointsAdversaire;
    }

    public void setPointsAdversaire(Integer pointsAdversaire) {
        this.pointsAdversaire = pointsAdversaire;
    }

    public MembreLight getPartenaire() {
        return partenaire;
    }

    public void setPartenaire(MembreLight partenaire) {
        this.partenaire = partenaire;
    }

    public Integer getPointsPartenaire() {
        return pointsPartenaire;
    }

    public void setPointsPartenaire(Integer pointsPartenaire) {
        this.pointsPartenaire = pointsPartenaire;
    }

    public MembreLight getPartenaireAdversaire() {
        return partenaireAdversaire;
    }

    public void setPartenaireAdversaire(MembreLight partenaireAdversaire) {
        this.partenaireAdversaire = partenaireAdversaire;
    }

    public Integer getPointsPartenaireAdversaire() {
        return pointsPartenaireAdversaire;
    }

    public void setPointsPartenaireAdversaire(Integer pointsPartenaireAdversaire) {
        this.pointsPartenaireAdversaire = pointsPartenaireAdversaire;
    }

    public Integer getDifferencePoints() {
        return differencePoints;
    }

    public void setDifferencePoints(Integer differencePoints) {
        this.differencePoints = differencePoints;
    }

    public ResultatMatch getResultatMatch() {
        return resultatMatch;
    }

    public void setResultatMatch(ResultatMatch resultatMatch) {
        this.resultatMatch = resultatMatch;
    }

    public Integer getPointsGagnesOuPerdus() {
        return pointsGagnesOuPerdus;
    }

    public void setPointsGagnesOuPerdus(Integer pointsGagnesOuPerdus) {
        this.pointsGagnesOuPerdus = pointsGagnesOuPerdus;
    }
}
