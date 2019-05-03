package be.company.fca.model;


import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.*;

@Entity
@Table(name="AUTORISATION_RENCONTRE")
public class AutorisationRencontre {

    @Id
    @GenericGenerator(
            name = "autorisation-rencontre-sequence",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name="sequence_name",value="hibernate_sequence"),
                    @org.hibernate.annotations.Parameter(name="increment_size",value="1")
            }
    )
    @GeneratedValue(generator = "autorisation-rencontre-sequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column( name =  "type", nullable = false)
    private TypeAutorisation type;

    @Column( name =  "rencontre_fk", nullable = false)
    private Long rencontreFk;

    @ManyToOne
    @JoinColumn(name = "membre_fk", nullable = false)
    private Membre membre;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TypeAutorisation getType() {
        return type;
    }

    public void setType(TypeAutorisation type) {
        this.type = type;
    }

    public Long getRencontreFk() {
        return rencontreFk;
    }

    public void setRencontreFk(Long rencontreFk) {
        this.rencontreFk = rencontreFk;
    }

    public Membre getMembre() {
        return membre;
    }

    public void setMembre(Membre membre) {
        this.membre = membre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AutorisationRencontre that = (AutorisationRencontre) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
