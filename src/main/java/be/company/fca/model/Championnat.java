package be.company.fca.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="CHAMPIONNAT")
public class Championnat {

    @Id
    @GeneratedValue
    private Long id;

    @Column( name =  "annee")
    private String annee;

    @Column ( name = "type" )
    @Enumerated(EnumType.STRING)
    private TypeChampionnat type;

    @Column ( name = "categorie" )
    @Enumerated(EnumType.STRING)
    private CategorieChampionnat categorie;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAnnee() {
        return annee;
    }

    public void setAnnee(String annee) {
        this.annee = annee;
    }

    public TypeChampionnat getType() {
        return type;
    }

    public void setType(TypeChampionnat type) {
        this.type = type;
    }

    public CategorieChampionnat getCategorie() {
        return categorie;
    }

    public void setCategorie(CategorieChampionnat categorie) {
        this.categorie = categorie;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Championnat that = (Championnat) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
