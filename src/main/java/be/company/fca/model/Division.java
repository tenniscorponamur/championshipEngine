package be.company.fca.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="DIVISION")
public class Division {

    @Id
    @GenericGenerator(
            name = "division-sequence",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name="sequence_name",value="hibernate_sequence"),
                    @org.hibernate.annotations.Parameter(name="increment_size",value="1")
            }
    )
    @GeneratedValue(generator = "division-sequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column( name =  "numero", nullable = false)
    private Integer numero;

    @Column( name =  "points_min", nullable = false)
    private Integer pointsMinimum;

    @Column( name =  "points_max", nullable = false)
    private Integer pointsMaximum;

    @ManyToOne
    @JoinColumn(name = "championnat_fk", nullable = false)
    private Championnat championnat;

    @Column( name = "multi_is", nullable = false)
    private boolean multiIS=false;

    @Column( name = "with_finales", nullable = false)
    private boolean withFinales=false;

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

    public boolean isMultiIS() {
        return multiIS;
    }

    public void setMultiIS(boolean multiIS) {
        this.multiIS = multiIS;
    }

    public boolean isWithFinales() {
        return withFinales;
    }

    public void setWithFinales(boolean withFinales) {
        this.withFinales = withFinales;
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
