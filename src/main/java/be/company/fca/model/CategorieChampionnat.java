package be.company.fca.model;

public enum CategorieChampionnat {

    MESSIEURS ("Messieurs"),
    DAMES ("Dames"),
    SIMPLE_MESSIEURS ("Simples Messieurs"),
    DOUBLE_MESSIEURS ("Doubles Messieurs"),
    SIMPLE_DAMES ("Simples Dames"),
    DOUBLE_DAMES ("Doubles Dames"),
    MIXTES ("Mixtes");

    private String libelle = "";

    CategorieChampionnat(String libelle){
        this.libelle = libelle;
    }

    public String toString(){
        return libelle;
    }

}
