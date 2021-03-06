package be.company.fca.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="TERRAIN")
public class Terrain {

    @Id
    @GenericGenerator(
            name = "terrain-sequence",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name="sequence_name",value="hibernate_sequence"),
                    @org.hibernate.annotations.Parameter(name="increment_size",value="1")
            }
    )
    @GeneratedValue(generator = "terrain-sequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column( name =  "nom", nullable = false)
    private String nom;

    @Column( name =  "description")
    private String description;

    @Column( name =  "adresse")
    private String adresse;

    @Column( name =  "nom_contact")
    private String nomContact;

    @Column( name =  "mail")
    private String mail;

    @Column( name =  "telephone")
    private String telephone;

    @Column( name =  "infos_restauration")
    private String infosRestauration;

    @Column( name = "show_drink_details", nullable = false)
    private boolean showDrinkDetails=false;

    @Column( name = "presence_buvette", nullable = false)
    private boolean presenceBuvette=false;

    @Column( name = "presence_bancontact", nullable = false)
    private boolean presenceBancontact=false;

    @Column( name = "presence_payconiq", nullable = false)
    private boolean presencePayconiq=false;

    @Column( name = "terrain_criterium_par_defaut", nullable = false)
    private boolean terrainCriteriumParDefaut=false;

    @Column( name = "actif", nullable = false)
    private boolean actif=true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getNomContact() {
        return nomContact;
    }

    public void setNomContact(String nomContact) {
        this.nomContact = nomContact;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getInfosRestauration() {
        return infosRestauration;
    }

    public void setInfosRestauration(String infosRestauration) {
        this.infosRestauration = infosRestauration;
    }

    public boolean isShowDrinkDetails() {
        return showDrinkDetails;
    }

    public void setShowDrinkDetails(boolean showDrinkDetails) {
        this.showDrinkDetails = showDrinkDetails;
    }

    public boolean isPresenceBuvette() {
        return presenceBuvette;
    }

    public void setPresenceBuvette(boolean presenceBuvette) {
        this.presenceBuvette = presenceBuvette;
    }

    public boolean isPresenceBancontact() {
        return presenceBancontact;
    }

    public void setPresenceBancontact(boolean presenceBancontact) {
        this.presenceBancontact = presenceBancontact;
    }

    public boolean isPresencePayconiq() {
        return presencePayconiq;
    }

    public void setPresencePayconiq(boolean presencePayconiq) {
        this.presencePayconiq = presencePayconiq;
    }

    public boolean isTerrainCriteriumParDefaut() {
        return terrainCriteriumParDefaut;
    }

    public void setTerrainCriteriumParDefaut(boolean terrainCriteriumParDefaut) {
        this.terrainCriteriumParDefaut = terrainCriteriumParDefaut;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Terrain terrain = (Terrain) o;
        return Objects.equals(id, terrain.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
