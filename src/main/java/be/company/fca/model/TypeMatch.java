package be.company.fca.model;

public enum TypeMatch {
    SIMPLE ("Simple"),
    DOUBLE ("Double");

    private String libelle = "";

    TypeMatch(String libelle){
        this.libelle = libelle;
    }

    public String toString(){
        return libelle;
    }

}