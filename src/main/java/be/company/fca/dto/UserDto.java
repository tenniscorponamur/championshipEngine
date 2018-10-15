package be.company.fca.dto;

import be.company.fca.model.User;

import java.util.List;

public class UserDto {

    private Long id;
    private String username;
    private String prenom;
    private String nom;
    private boolean admin;
    private MembreDto membre;
    private List<String> roles;

    public UserDto() {
    }

    public UserDto(User user, List<String> roles) {
        this.id=user.getId();
        this.username=user.getUsername();
        this.prenom=user.getPrenom();
        this.nom=user.getNom();
        this.admin=user.isAdmin();
        if (user.getMembre()!=null){
            this.membre = new MembreDto(user.getMembre(),false);
        }
        this.roles=roles;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public MembreDto getMembre() {
        return membre;
    }

    public void setMembre(MembreDto membre) {
        this.membre = membre;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
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
