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
@NamedQuery(name = "ClassTeacher.findByClassroomAndSubject", query = "FROM ClassTeacher WHERE classroom.id = :classroomId and subject.id = :subjectId")
@NamedQuery(name = "ClassTeacher.findByClassroomAndTeacherAndSubject", query = "FROM ClassTeacher WHERE classroom.id = :classroomId and subject.id = :subjectId and teacher.id = :teacherId")
@NamedQuery(name = "ClassTeacher.findByTeacher", query = "FROM ClassTeacher WHERE teacher.id = :teacherId")
public class ClassTeacher implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private long id;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Classroom classroom;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Account teacher;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Subject subject;
}
