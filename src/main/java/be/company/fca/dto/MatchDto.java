package be.company.fca.dto;

import be.company.fca.model.Match;
import be.company.fca.model.TypeMatch;

public class MatchDto {

    private Long id;
    private Integer ordre;
    private TypeMatch type;
    private MembreDto joueurVisites1;
    private MembreDto joueurVisites2;
    private MembreDto joueurVisiteurs1;
    private MembreDto joueurVisiteurs2;
    private Integer pointsVisites;
    private Integer pointsVisiteurs;
    private RencontreDto rencontre;

    public MatchDto(Match match) {
        this.id = match.getId();
        this.ordre = match.getOrdre();
        this.type = match.getType();
        if (match.getJoueurVisites1()!=null){
            this.joueurVisites1 = new MembreDto(match.getJoueurVisites1(),false);
        }
        if (match.getJoueurVisites2()!=null){
            this.joueurVisites2 = new MembreDto(match.getJoueurVisites2(),false);
        }
        if (match.getJoueurVisiteurs1()!=null){
            this.joueurVisiteurs1 = new MembreDto(match.getJoueurVisiteurs1(),false);
        }
        if (match.getJoueurVisiteurs2()!=null){
            this.joueurVisiteurs2 = new MembreDto(match.getJoueurVisiteurs2(),false);
        }
        this.pointsVisites = match.getPointsVisites();
        this.pointsVisiteurs = match.getPointsVisiteurs();
        if (match.getRencontre()!=null){
            this.rencontre = new RencontreDto(match.getRencontre());
        }
    }

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

    public MembreDto getJoueurVisites1() {
        return joueurVisites1;
    }

    public void setJoueurVisites1(MembreDto joueurVisites1) {
        this.joueurVisites1 = joueurVisites1;
    }

    public MembreDto getJoueurVisites2() {
        return joueurVisites2;
    }

    public void setJoueurVisites2(MembreDto joueurVisites2) {
        this.joueurVisites2 = joueurVisites2;
    }

    public MembreDto getJoueurVisiteurs1() {
        return joueurVisiteurs1;
    }

    public void setJoueurVisiteurs1(MembreDto joueurVisiteurs1) {
        this.joueurVisiteurs1 = joueurVisiteurs1;
    }

    public MembreDto getJoueurVisiteurs2() {
        return joueurVisiteurs2;
    }

    public void setJoueurVisiteurs2(MembreDto joueurVisiteurs2) {
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

    public RencontreDto getRencontre() {
        return rencontre;
    }

    public void setRencontre(RencontreDto rencontre) {
        this.rencontre = rencontre;
    }
}
