package be.company.fca.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "CLASSEMENT_JOB_TRACE")
public class ClassementJobTrace {

    @Id
    @GenericGenerator(
            name = "classement-job-trace-sequence",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name="sequence_name",value="hibernate_sequence"),
                    @org.hibernate.annotations.Parameter(name="increment_size",value="1")
            }
    )
    @GeneratedValue(generator = "classement-job-trace-sequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column( name = "message" )
    private String message;

    @ManyToOne
    @JoinColumn(name = "classement_job_fk", nullable = false)
    private ClassementJob classementJob;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ClassementJob getClassementJob() {
        return classementJob;
    }

    public void setClassementJob(ClassementJob classementJob) {
        this.classementJob = classementJob;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassementJobTrace that = (ClassementJobTrace) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ClassementJobTrace{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", classementJob=" + classementJob +
                '}';
    }
}
