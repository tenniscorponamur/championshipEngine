package be.company.fca.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "CLUB")
public class Club {

    @Id
    @SequenceGenerator(name = "clubSeqGenerator", sequenceName = "clubSeq", initialValue = 5, allocationSize = 100)
    @GeneratedValue(generator = "clubSeqGenerator")
    private long id;

    @Column
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Club club = (Club) o;
        return id == club.id;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
