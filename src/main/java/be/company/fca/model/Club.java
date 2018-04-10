package be.company.fca.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "CLUB")
public class Club {

    @Id
    @GeneratedValue
    private long id;

    @Column
    private String numero;

    @Column
    private String nom;

    @Column
    private String description;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Club club = (Club) o;
        return id == club.id;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
