package io.github.etases.edublock.rs.entity;

import io.github.etases.edublock.rs.entity.generator.UseExistOrIncrementGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@NamedQuery(name = "Record.findByStudentAndClassroom", query = "FROM Record WHERE student.id = :studentId and classroom.id = :classroomId")
@NamedQuery(name = "Record.findByGradeAndYear", query = "FROM Record WHERE classroom.grade = :grade and classroom.year = :year")
@NamedQuery(name = "Record.findByClassroom", query = "FROM Record WHERE classroom.id = :classroomId")
public class Record implements Serializable {
    @Id
    @GenericGenerator(name = "ExistOrGenerate", strategy = UseExistOrIncrementGenerator.CLASS_PATH)
    @GeneratedValue(generator = "ExistOrGenerate")
    @Column(unique = true, nullable = false)
    private Long id;
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
