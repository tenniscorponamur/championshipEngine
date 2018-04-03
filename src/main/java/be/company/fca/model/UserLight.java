package be.company.fca.model;

public class UserLight {

    private Long id;
    private String username;
    private String prenom;
    private String nom;

    public UserLight(User user) {
        this.id=user.getId();
        this.username=user.getUsername();
        this.prenom=user.getPrenom();
        this.nom=user.getNom();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}
