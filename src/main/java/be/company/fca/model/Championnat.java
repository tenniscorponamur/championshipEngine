package be.company.fca.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="CHAMPIONNAT")
public class Championnat {

    @Id
    @GenericGenerator(
            name = "championnat-sequence",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name="sequence_name",value="hibernate_sequence"),
                    @org.hibernate.annotations.Parameter(name="increment_size",value="1")
            }
    )
    @GeneratedValue(generator = "championnat-sequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column( name =  "annee")
    private String annee;

    @Column ( name = "type" )
    @Enumerated(EnumType.STRING)
    private TypeChampionnat type;

    @Column ( name = "categorie" )
    @Enumerated(EnumType.STRING)
    private CategorieChampionnat categorie;

    @Column( name = "calendrier_a_rafraichir", nullable = false)
    private boolean calendrierARafraichir=false;

    @Column( name = "calendrier_valide", nullable = false)
    private boolean calendrierValide=false;

    @Column( name = "cloture", nullable = false)
    private boolean cloture=false;

    @Column( name =  "ordre")
    private Long ordre;

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

    public boolean isCalendrierARafraichir() {
        return calendrierARafraichir;
    }

    public void setCalendrierARafraichir(boolean calendrierARafraichir) {
        this.calendrierARafraichir = calendrierARafraichir;
    }

    public boolean isCalendrierValide() {
        return calendrierValide;
    }

    public void setCalendrierValide(boolean calendrierValide) {
        this.calendrierValide = calendrierValide;
    }

    public boolean isCloture() {
        return cloture;
    }

    public void setCloture(boolean cloture) {
        this.cloture = cloture;
    }

    public Long getOrdre() {
        return ordre;
    }

    public void setOrdre(Long ordre) {
        this.ordre = ordre;
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
