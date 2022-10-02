package io.github.etases.edublock.rs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private long id;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Subject subject;
    @Column(nullable = false)
    private float firstHalfScore;
    @Column(nullable = false)
    private float secondHalfScore;
    @Column(nullable = false)
    private float finalScore;
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
