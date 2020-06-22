package be.company.fca.model;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "MEMBRE_EQUIPE")
public class MembreEquipe {

    @Id
    @GenericGenerator(
            name = "membre-equipe-sequence",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name="sequence_name",value="hibernate_sequence"),
                    @org.hibernate.annotations.Parameter(name="increment_size",value="1")
            }
    )
    @GeneratedValue(generator = "membre-equipe-sequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column( name =  "equipe_fk", nullable = false)
    private Long equipeFk; // Pas de chargement de l'arbre relatif a l'equipe

    @Column( name =  "membre_fk", nullable = false)
    private Long membreFk; // Pas de chargement de l'arbre relatif au membre

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEquipeFk() {
        return equipeFk;
    }

    public void setEquipeFk(Long equipeFk) {
        this.equipeFk = equipeFk;
    }

    public Long getMembreFk() {
        return membreFk;
    }

    public void setMembreFk(Long membreFk) {
        this.membreFk = membreFk;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MembreEquipe that = (MembreEquipe) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(equipeFk, that.equipeFk) &&
                Objects.equals(membreFk, that.membreFk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, equipeFk, membreFk);
    }
}
