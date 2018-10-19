package be.company.fca.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="COURT")
public class Court {

    @Id
    @GeneratedValue
    private Long id;

    @Column( name = "code", nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(name = "terrain_fk", nullable = false)
    private Terrain terrain;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Court court = (Court) o;
        return Objects.equals(id, court.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
