package io.github.etases.edublock.rs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@NamedQuery(name = "Record.findByStudentAndClassroom", query = "FROM Record WHERE student.id = :studentId and classroom.id = :classroomId")
public class Record implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private long id;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Classroom classroom;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Student student;
    @OneToMany(mappedBy = "record")
    private List<RecordEntry> recordEntry;
    @OneToMany(mappedBy = "record")
    private List<PendingRecordEntry> pendingRecordEntry;
}
