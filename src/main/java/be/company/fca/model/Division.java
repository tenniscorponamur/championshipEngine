package be.company.fca.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="DIVISION")
public class Division {

    @Id
    @GeneratedValue
    private Long id;

    @Column( name =  "numero")
    private Integer numero;

    @Column( name =  "points_min")
    private Integer pointsMinimum;

    @Column( name =  "points_max")
    private Integer pointsMaximum;

    @ManyToOne
    @JoinColumn(name = "championnat_fk")
    private Championnat championnat;

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

    public Integer getPointsMinimum() {
        return pointsMinimum;
    }

    public void setPointsMinimum(Integer pointsMinimum) {
        this.pointsMinimum = pointsMinimum;
    }

    public Integer getPointsMaximum() {
        return pointsMaximum;
    }

    public void setPointsMaximum(Integer pointsMaximum) {
        this.pointsMaximum = pointsMaximum;
    }

    public Championnat getChampionnat() {
        return championnat;
    }

    public void setChampionnat(Championnat championnat) {
        this.championnat = championnat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Division division = (Division) o;
        return Objects.equals(id, division.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
