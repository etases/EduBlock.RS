package io.github.etases.edublock.rs.entity;

import io.github.etases.edublock.rs.entity.generator.UseExistOrIncrementGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@NamedQuery(name = "ClassTeacher.findByClassroomAndSubject", query = "FROM ClassTeacher WHERE classroom.id = :classroomId and subjectId = :subjectId")
@NamedQuery(name = "ClassTeacher.findByClassroomAndTeacherAndSubject", query = "FROM ClassTeacher WHERE classroom.id = :classroomId and subjectId = :subjectId and teacher.id = :teacherId")
@NamedQuery(name = "ClassTeacher.findByTeacher", query = "FROM ClassTeacher WHERE teacher.id = :teacherId")
public class ClassTeacher implements Serializable {
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
    private Account teacher;
    @Column(nullable = false)
    private long subjectId;
}
