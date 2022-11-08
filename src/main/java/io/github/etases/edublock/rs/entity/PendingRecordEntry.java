package io.github.etases.edublock.rs.entity;

import io.github.etases.edublock.rs.entity.generator.UseExistOrIncrementGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@NamedQuery(name = "PendingRecordEntry.findAll", query = "FROM PendingRecordEntry")
@NamedQuery(name = "PendingRecordEntry.findByHomeroomTeacher", query = "FROM PendingRecordEntry WHERE record.classroom.homeroomTeacher.id = :teacherId")
@NamedQuery(name = "PendingRecordEntry.findByHomeroomTeacherAndStudent", query = "FROM PendingRecordEntry WHERE record.classroom.homeroomTeacher.id = :teacherId and record.student.id = :studentId")
public class PendingRecordEntry implements Serializable {
    @Id
    @GenericGenerator(name = "ExistOrGenerate", strategy = UseExistOrIncrementGenerator.CLASS_PATH)
    @GeneratedValue(generator = "ExistOrGenerate")
    @Column(unique = true, nullable = false)
    private Long id;
    @Column(nullable = false)
    private long subjectId;
    @Column(nullable = false)
    private float firstHalfScore;
    @Column(nullable = false)
    private float secondHalfScore;
    @Column(nullable = false)
    private float finalScore;
    @Column(nullable = false)
    private Date requestDate;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Account teacher;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Account requester;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Record record;
}
