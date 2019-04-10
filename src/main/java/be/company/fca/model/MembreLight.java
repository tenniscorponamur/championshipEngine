package be.company.fca.model;

public class MembreLight {

    private Long id;
    private String prenom;
    private String nom;
    private Genre genre;
    private Club club;
    private boolean actif=true;
    private String numeroAft;

    public MembreLight() {
    }

    public MembreLight(Membre membre) {
        this.id = membre.getId();
        this.prenom = membre.getPrenom();
        this.nom = membre.getNom();
        this.genre = membre.getGenre();
        this.club = membre.getClub();
        this.actif = membre.isActif();
        this.numeroAft = membre.getNumeroAft();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public String getNumeroAft() {
        return numeroAft;
    }

    public void setNumeroAft(String numeroAft) {
        this.numeroAft = numeroAft;
    }
}
