package be.company.fca.model;

public enum CategorieChampionnat {

    MESSIEURS ("Messieurs"),
    DAMES ("Dames"),
    MIXTES ("Mixtes");

    private String libelle = "";

    CategorieChampionnat(String libelle){
        this.libelle = libelle;
    }

    public String toString(){
        return libelle;
    }

}
