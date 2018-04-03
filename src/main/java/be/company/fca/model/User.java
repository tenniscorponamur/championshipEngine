package be.company.fca.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name="UTILISATEUR")
public class User implements UserDetails {

    @Id
    @SequenceGenerator(name = "utilisateurSeqGenerator", sequenceName = "utilisateurSeq", initialValue = 5, allocationSize = 100)
    @GeneratedValue(generator = "utilisateurSeqGenerator")
    private Long id;

    @Column( name =  "username", length = 500, nullable = false, unique = true)
    private String username;

    @Column( name =  "password", length = 500, nullable = false)
    private String password;

    @Column( name =  "prenom", length = 500, nullable = false)
    private String prenom;

    @Column( name =  "nom", length = 500, nullable = false)
    private String nom;

    //TODO :
    // ForeignKey Membre

    @Transient
    List<GrantedAuthority> authorities = new ArrayList<>();

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

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
