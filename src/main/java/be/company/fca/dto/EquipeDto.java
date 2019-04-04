package be.company.fca.dto;

import be.company.fca.model.*;

public class EquipeDto {

    private Long id;
    private String codeAlphabetique;
    private Division division;
    private Club club;
    private MembreDto capitaine;
    private Poule poule;
    private Terrain terrain;
    private boolean hybride;

    public EquipeDto(Equipe equipe) {
        this.id = equipe.getId();
        this.codeAlphabetique = equipe.getCodeAlphabetique();
        this.division = equipe.getDivision();
        this.club = equipe.getClub();
        if (equipe.getCapitaine()!=null){
            this.capitaine = new MembreDto(equipe.getCapitaine(),false,true);
        }
        this.poule = equipe.getPoule();
        this.terrain = equipe.getTerrain();
        this.hybride = equipe.isHybride();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeAlphabetique() {
        return codeAlphabetique;
    }

    public void setCodeAlphabetique(String codeAlphabetique) {
        this.codeAlphabetique = codeAlphabetique;
    }

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    public MembreDto getCapitaine() {
        return capitaine;
    }

    public void setCapitaine(MembreDto capitaine) {
        this.capitaine = capitaine;
    }

    public Poule getPoule() {
        return poule;
    }

    public void setPoule(Poule poule) {
        this.poule = poule;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    public boolean isHybride() {
        return hybride;
    }

    public void setHybride(boolean hybride) {
        this.hybride = hybride;
    }
}
