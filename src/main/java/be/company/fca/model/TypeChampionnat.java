package be.company.fca.model;

public enum TypeChampionnat {

    HIVER ("Hiver"),
    ETE ("Eté"),
    CRITERIUM ("Critérium");

    private String libelle = "";

    TypeChampionnat(String libelle){
        this.libelle = libelle;
    }

    public String toString(){
        return libelle;
    }
}
