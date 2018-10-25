package be.company.fca.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name="TRACE")
public class Trace {

    @Id
    @GenericGenerator(
            name = "trace-sequence",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator"
    )
    @GeneratedValue(generator = "trace-sequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column( name =  "dateheure")
    private Date dateHeure;

    @Column( name =  "utilisateur")
    private String utilisateur;

    @Column( name =  "type")
    private String type;

    @Column( name =  "foreign_key")
    private String foreignKey;

    @Column( name =  "message")
    private String message;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(Date dateHeure) {
        this.dateHeure = dateHeure;
    }

    public String getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(String utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(String foreignKey) {
        this.foreignKey = foreignKey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trace trace = (Trace) o;
        return Objects.equals(id, trace.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
