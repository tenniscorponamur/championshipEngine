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
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator"
    )
    @GeneratedValue(generator = "terrain-sequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column( name =  "nom", nullable = false)
    private String nom;

    @Column( name =  "description")
    private String description;

    @Column( name =  "adresse")
    private String adresse;

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
