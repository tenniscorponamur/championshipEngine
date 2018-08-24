package be.company.fca.model;

public class Localite {

    private String codePostal;
    private String nomLocalite;
    private String nomCommune;

    public Localite() {
    }

    public Localite(String codePostal, String nomLocalite, String nomCommune) {
        this.codePostal = codePostal;
        this.nomLocalite = nomLocalite;
        this.nomCommune = nomCommune;
    }

    public String getNomCommune() {
        return nomCommune;
    }

    public void setNomCommune(String nomCommune) {
        this.nomCommune = nomCommune;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public String getNomLocalite() {
        return nomLocalite;
    }

    public void setNomLocalite(String nomLocalite) {
        this.nomLocalite = nomLocalite;
    }
}
