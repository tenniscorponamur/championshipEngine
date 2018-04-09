package be.company.fca.model;

public enum Genre  {
    HOMME ("Homme"),
    FEMME ("Femme");

    private String libelle = "";

    Genre(String libelle){
        this.libelle = libelle;
    }

    public String toString(){
        return libelle;
    }

}