package be.company.fca.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="HORAIRE_TERRAIN")
public class HoraireTerrain {

    @Id
    @GenericGenerator(
            name = "horaire-terrain-sequence",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator"
    )
    @GeneratedValue(generator = "horaire-terrain-sequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "terrain_fk", nullable = false)
    private Terrain terrain;

    @Column ( name = "type_championnat", nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeChampionnat typeChampionnat;

    @Column( name = "jour_semaine", nullable = false)
    private Integer jourSemaine;

    @Column( name = "heures", nullable = false)
    private Integer heures;

    @Column( name = "minutes", nullable = false)
    private Integer minutes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    public TypeChampionnat getTypeChampionnat() {
        return typeChampionnat;
    }

    public void setTypeChampionnat(TypeChampionnat typeChampionnat) {
        this.typeChampionnat = typeChampionnat;
    }

    public Integer getJourSemaine() {
        return jourSemaine;
    }

    public void setJourSemaine(Integer jourSemaine) {
        this.jourSemaine = jourSemaine;
    }

    public Integer getHeures() {
        return heures;
    }

    public void setHeures(Integer heures) {
        this.heures = heures;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoraireTerrain that = (HoraireTerrain) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
