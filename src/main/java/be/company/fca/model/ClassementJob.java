package be.company.fca.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "CLASSEMENT_JOB")
public class ClassementJob {

    @Id
    @GenericGenerator(
            name = "classement-job-sequence",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator"
    )
    @GeneratedValue(generator = "classement-job-sequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Temporal(TemporalType.DATE)
    @Column( name = "start_date", nullable = false )
    private Date startDate;

    @Temporal(TemporalType.DATE)
    @Column( name = "end_date", nullable = false )
    private Date endDate;

    @Column( name = "status", nullable = false )
    @Enumerated(EnumType.STRING)
    private ClassementJobStatus status;

    @Column (name = "avec_sauvegarde", nullable = false)
    private boolean avecSauvegarde=false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public ClassementJobStatus getStatus() {
        return status;
    }

    public void setStatus(ClassementJobStatus status) {
        this.status = status;
    }

    public boolean isAvecSauvegarde() {
        return avecSauvegarde;
    }

    public void setAvecSauvegarde(boolean avecSauvegarde) {
        this.avecSauvegarde = avecSauvegarde;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassementJob that = (ClassementJob) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ClassementJob{" +
                "id=" + id +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status=" + status +
                '}';
    }
}
