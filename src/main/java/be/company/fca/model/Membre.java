package be.company.fca.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="MEMBRE")
public class Membre {

    @Id
    @SequenceGenerator(name = "membreSeqGenerator", sequenceName = "membreSeq", initialValue = 5, allocationSize = 100)
    @GeneratedValue(generator = "membreSeqGenerator")
    private Long id;

    @Column( name =  "numero", length = 500, nullable = false, unique = true)
    private String numero;

    @Column( name =  "prenom", length = 500, nullable = false)
    private String prenom;

    @Column( name =  "nom", length = 500, nullable = false)
    private String nom;

    @Temporal(TemporalType.DATE)
    @Column( name = "dateNaissance", nullable = false)
    private Date dateNaissance;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Date getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(Date dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Membre membre = (Membre) o;

        return id != null ? id.equals(membre.id) : membre.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
