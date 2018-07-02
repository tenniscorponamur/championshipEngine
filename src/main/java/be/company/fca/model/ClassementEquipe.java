package be.company.fca.model;

import java.util.*;

public class ClassementEquipe {

    private Equipe equipe;
    private int points;
    private int matchsJoues;
    private int setsGagnes;
    private int setsPerdus;
    private int jeuxGagnes;
    private int jeuxPerdus;
    private boolean gagnantInterseries;
    private HashSet<Equipe> equipesBattues=new HashSet<>();

    public ClassementEquipe() {
    }

    public ClassementEquipe(Equipe equipe) {
        this.equipe = equipe;
    }

    public Equipe getEquipe() {
        return equipe;
    }

    public void setEquipe(Equipe equipe) {
        this.equipe = equipe;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getMatchsJoues() {
        return matchsJoues;
    }

    public void setMatchsJoues(int matchsJoues) {
        this.matchsJoues = matchsJoues;
    }

    public int getSetsGagnes() {
        return setsGagnes;
    }

    public void setSetsGagnes(int setsGagnes) {
        this.setsGagnes = setsGagnes;
    }

    public int getSetsPerdus() {
        return setsPerdus;
    }

    public void setSetsPerdus(int setsPerdus) {
        this.setsPerdus = setsPerdus;
    }

    public int getJeuxGagnes() {
        return jeuxGagnes;
    }

    public void setJeuxGagnes(int jeuxGagnes) {
        this.jeuxGagnes = jeuxGagnes;
    }

    public int getJeuxPerdus() {
        return jeuxPerdus;
    }

    public void setJeuxPerdus(int jeuxPerdus) {
        this.jeuxPerdus = jeuxPerdus;
    }

    public boolean isGagnantInterseries() {
        return gagnantInterseries;
    }

    public void setGagnantInterseries(boolean gagnantInterseries) {
        this.gagnantInterseries = gagnantInterseries;
    }

    public void ajoutMatchJoue(){
        this.matchsJoues++;
    }

    public void ajoutPoints(int points){
        this.points += points;
    }

    public void ajoutSetsGagnes(int setsGagnes){
        this.setsGagnes += setsGagnes;
    }

    public void ajoutSetsPerdus(int setsPerdus){
        this.setsPerdus += setsPerdus;
    }

    public void ajoutJeuxGagnes(int jeuxGagnes){
        this.jeuxGagnes += jeuxGagnes;
    }

    public void ajoutJeuxPerdus(int jeuxPerdus){
        this.jeuxPerdus += jeuxPerdus;
    }

    public HashSet<Equipe> getEquipesBattues() {
        return equipesBattues;
    }

    public void setEquipesBattues(HashSet<Equipe> equipesBattues) {
        this.equipesBattues = equipesBattues;
    }
}
