package be.company.fca.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="POULE")
public class Poule {

    @Id
    @GenericGenerator(
            name = "poule-sequence",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator"
    )
    @GeneratedValue(generator = "poule-sequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column( name =  "numero", nullable = false)
    private Integer numero;

    @ManyToOne
    @JoinColumn(name = "division_fk", nullable = false)
    private Division division;

    @Column( name = "allerretour", nullable = false)
    private boolean allerRetour=false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    public boolean isAllerRetour() {
        return allerRetour;
    }

    public void setAllerRetour(boolean allerRetour) {
        this.allerRetour = allerRetour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Poule poule = (Poule) o;
        return Objects.equals(id, poule.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
